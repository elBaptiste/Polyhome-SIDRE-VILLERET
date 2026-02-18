package com.villeret.polyhome_sidre_villeret

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ListView
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.collections.isNotEmpty

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

import java.time.LocalDate
import java.time.LocalDateTime


class UsersActivity : AppCompatActivity() {

    private val api = Api()
    private val usersArray = ArrayList<UsersData>()
    private lateinit var userAdapter: UsersAdapter

    private var expirationDate: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_users)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val id = intent.getStringExtra("houseId").toString()
        val token = intent.getStringExtra("token")
        if (token != null) {
            fetchUsers(token, id)
        }
        val calendar = findViewById<CalendarView>(R.id.calendarViewTempUserUsersActivity)
        calendar.visibility = View.GONE

        initializeAddUserButton(id, token.toString())
        initializeBackButton()
        setupTemporarySwitch()
    }

    private fun fetchUsers(token: String, houseId: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/${houseId}/users"

        api.get<List<UsersData>>(url, { code, users ->
            if (code == 200 && users != null && users.isNotEmpty()) {
                for (element in users) {
                    usersArray.add(element)
                }
                Log.d("POLYHOME_USERS_LIST", "Users List : $usersArray")

                if(usersArray != emptyArray<UsersData>()) {
                    runOnUiThread {
                        userAdapter = UsersAdapter(this, usersArray, houseId, token)
                        initializeUsersList()
                    }
                }

            } else {
                Log.e("POLYHOME_ID", "Erreur lors de la récupération de l'ID")
            }
        }, token)
    }

    private fun initializeUsersList() {
        val listView = findViewById<ListView>(R.id.usersListViewUsersActivity)
        Log.d("POLYHOME_HOUSE_ADAPTER", "House Adapter list  : test")
        Log.d("POLYHOME_HOUSE_ADAPTER", "House Adapter list  : $userAdapter")
        listView.adapter = userAdapter

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeAddUserButton(houseId: String, securityToken: String) {
        val addBtn = findViewById<Button>(R.id.addNewUserButtonUsersActivity)

        addBtn.setOnClickListener {
            Log.d("POLYHOME_USERS", "Bouton click")
            val urlToAddUser =  "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users"
            val data = UserLogin(findViewById<EditText>(R.id.newUserEditTextUsersActivity).text.toString())

            api.post<UserLogin>(
                urlToAddUser,
                data,
                onSuccess = { responseUserCode: Int ->
                    if (responseUserCode == 200) {
                        Log.d("POLYHOME_USERS", "Ajout d'un utlisateur")
                        resetUserList(securityToken, houseId)
                    }
                    else {
                        Log.d("POLYHOME_USERS", "${responseUserCode}")
                    }
                },
                securityToken

            )

            if (expirationDate != null) {
                Log.d("POLYHOME_DATE", "scheduleUserDeletion")
                scheduleUserDeletion(findViewById<EditText>(R.id.newUserEditTextUsersActivity).text.toString(), houseId, securityToken,
                    expirationDate!!
                )
            }

            val tempSwitch = findViewById<Switch>(R.id.switchTempUserUsersActivity)
            val calendar = findViewById<CalendarView>(R.id.calendarViewTempUserUsersActivity)
            tempSwitch.isChecked = false
            calendar.visibility = View.GONE
            expirationDate = null
        }
    }

    fun resetUserList(token: String, houseId: String) {
        usersArray.clear()
        fetchUsers(token, houseId)
    }

    fun initializeBackButton() {
        val backButton = findViewById<Button>(R.id.backButtonUsersActivity)

        backButton.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun setupTemporarySwitch() {
        val tempSwitch = findViewById<Switch>(R.id.switchTempUserUsersActivity)
        val calendar = findViewById<CalendarView>(R.id.calendarViewTempUserUsersActivity)

        tempSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                expirationDate = "1970-01-01"
                calendar.setOnDateChangeListener(object: CalendarView.OnDateChangeListener {
                    override fun onSelectedDayChange(view: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
                        val monthFormated = month + 1
                        var monthString = "$monthFormated"
                        var dayString = "$dayOfMonth"
                        if (monthFormated < 10) {
                            monthString = "0$monthFormated"

                        }
                        if (dayOfMonth < 10 ) {
                            dayString = "0$dayOfMonth"
                        }

                        expirationDate = "$year-$monthString-$dayString"
                        Log.d("POLYHOME_DATE", "$expirationDate")

                    }
                })
                calendar.visibility = View.VISIBLE
            } else {
                calendar.visibility = View.GONE
                expirationDate = null
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleUserDeletion(userName: String, houseId: String, token: String, targetDate: String) {
        val targetLocalDate = LocalDate.parse(targetDate)
        val targetDateTime = targetLocalDate.atStartOfDay()

        val now = LocalDateTime.now()
        var delay = java.time.Duration.between(now, targetDateTime).toMillis()

        Log.d("POLYHOME_DATE", "delay : $delay")

        if (delay < 0) {
            delay = 0
        }

        // val testDelay : Long = 10

        val data = workDataOf(
            "userName" to userName,
            "houseId" to houseId,
            "token" to token
        )

        Log.d("POLYHOME_DATE", "$data")

        val deleteRequest = OneTimeWorkRequestBuilder<DeleteUserWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("delete_$userName")
            .build()

        Log.d("POLYHOME_DATE", "$deleteRequest")

        WorkManager.getInstance(this).enqueue(deleteRequest)

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(deleteRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            resetUserList(token, houseId)
                        }
                        else -> {
                            Log.d("POLYHOME_DATE", "ça a pas supp")
                        }
                    }
                }
            }

        Log.d("POLYHOME_DATE", "ça a envoyé dans le workManager")
    }

}