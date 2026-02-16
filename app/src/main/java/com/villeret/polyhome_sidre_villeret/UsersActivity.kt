package com.villeret.polyhome_sidre_villeret

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.collections.isNotEmpty

class UsersActivity : AppCompatActivity() {

    private val api = Api()
    private val usersArray = ArrayList<UsersData>()
    private lateinit var userAdapter: UsersAdapter

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

        initializeAddUserButton(id, token.toString())
        initializeBackButton()
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
}