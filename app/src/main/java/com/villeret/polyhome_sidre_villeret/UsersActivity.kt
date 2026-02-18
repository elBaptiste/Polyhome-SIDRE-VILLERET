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

        // On cache directement le calendrier, on l'affichera quand lorsque le switch est coché
        val calendar = findViewById<CalendarView>(R.id.calendarViewTempUserUsersActivity)
        calendar.visibility = View.GONE

        initializeAddUserButton(id, token.toString())
        initializeBackButton()
        setupTemporarySwitch()
    }

    // fonction pour récupérer les utilisateurs avec un appel API
    private fun fetchUsers(token: String, houseId: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/${houseId}/users"

        // Appel API pour récupérer les utilisateurs d'une maison
        api.get<List<UsersData>>(url, { code, users ->
            if (code == 200 && users != null && users.isNotEmpty()) {
                for (element in users) {
                    // On ajoute chaque utlisateur à notre tableau d'utilisateurs
                    usersArray.add(element)
                }
                Log.d("POLYHOME_USERS_LIST", "Users List : $usersArray")

                if(usersArray != emptyArray<UsersData>()) {
                    // On utilise runOnUiThread car cela permet de lancer initializeUsersList() sur le thread principal
                    // Sinon on a l'erreur Only the original thread that created a view hierarchy can touch its views
                    // Avec runOnUiThread on s'assure que ce soit le bon thread qui execute l'initialisation de la liste des utlisateurs
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

    // Initialisation de la liste des utilisateurs d'une maison
    private fun initializeUsersList() {
        // Récupère la vue de la liste dans le layout
        val listView = findViewById<ListView>(R.id.usersListViewUsersActivity)
        Log.d("POLYHOME_HOUSE_ADAPTER", "House Adapter list  : $userAdapter")
        // Lie l'adapter à la ListView pour afficher les maisons
        listView.adapter = userAdapter

    }

    // Initialisation du bouton d'ajout d'utilisateur
    // On a besoin de fonctionnalités qui n'existe pas sur les vieux téléphones pour les utilsateurs temporaires et le CoroutineWorker
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeAddUserButton(houseId: String, securityToken: String) {
        val addBtn = findViewById<Button>(R.id.addNewUserButtonUsersActivity)

        addBtn.setOnClickListener {
            Log.d("POLYHOME_USERS", "Bouton click")
            val urlToAddUser =  "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users"
            val data = UserLogin(findViewById<EditText>(R.id.newUserEditTextUsersActivity).text.toString())

            // Appel API pour ajouter un utlisateur
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

            // LOGIQUE POUR LES UTLISATEURS TEMPORAIRES
            // Si expirationDate a une valeur est est non nulle, ça veut dire que le switch temporaire est checked
            // Donc le calendrier est ouvert et une date est choisi, donc on fait appel à la focntion scheduleUserDeletion, on s'assure que targetDate est non null quand même
            if (expirationDate != null) {
                Log.d("POLYHOME_DATE", "scheduleUserDeletion")
                scheduleUserDeletion(findViewById<EditText>(R.id.newUserEditTextUsersActivity).text.toString(), houseId, securityToken,
                    expirationDate!!
                )
            }

            // On ferme le calendrier, réinitialise expirationDate et unchecked le switch
            val tempSwitch = findViewById<Switch>(R.id.switchTempUserUsersActivity)
            val calendar = findViewById<CalendarView>(R.id.calendarViewTempUserUsersActivity)
            tempSwitch.isChecked = false
            calendar.visibility = View.GONE
            expirationDate = null
        }
    }

    // Fonction pour réinitialiser les utilisateurs
    fun resetUserList(token: String, houseId: String) {
        // On vide le tableau des utilisateur et on récupère la liste des utlisateurs
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
                // on initialise expirationDate pour éviter les problèmes quand un utlisateur ajoute un utilisateur directement sans changer la date
                // car setOnDateChangeListener ne s'effectue qu'aux changements de date
                expirationDate = "1970-01-01"
                calendar.visibility = View.VISIBLE
                calendar.setOnDateChangeListener(object: CalendarView.OnDateChangeListener {
                    override fun onSelectedDayChange(view: CalendarView, year: Int, month: Int, dayOfMonth: Int) {
                        // Le mois du calendrier démarre à 0 pour janvier, donc on doit ajouter 1, et on formate la date car
                        // Pour les DateTime on a besoin du format yyyy-mm-dd, hors on n'a pas des jours et des mois de 2 chiffres pour ceux inférieurs à 10 comme c'est des entiers
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
            } else {
                calendar.visibility = View.GONE
                expirationDate = null
            }
        }


    }

    // Fonction pour planifier la suppresion d'un utilisateur
    // On a besoin de fonctionnalités qui n'existe pas sur les vieux téléphones pour les utilsateurs temporaires et le CoroutineWorker
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleUserDeletion(userName: String, houseId: String, token: String, targetDate: String) {

        // Convertit la String sous format YYYY-MM-DD en Date puis en DateTime avec une heure init à minuit
        val targetLocalDate = LocalDate.parse(targetDate)
        val targetDateTime = targetLocalDate.atStartOfDay()

        val now = LocalDateTime.now()
        // Calcule la différence de temps entre maintenant et la date cible
        var delay = java.time.Duration.between(now, targetDateTime).toMillis()

        Log.d("POLYHOME_DATE", "delay : $delay")

        // Si le délai est négatif, on le met à 0, donc ça ajoutera et supprimera instantanément l'utilisateur
        // La raison de pourquoi on ne sort pas directement de la fonction à ce moment là est que le comportement de la fonction
        // n'était pas le bon et ne faisait jamais la suppression en différée. J'ai cherché pourquoi, et je n'ai pas trouvé la raison.
        // Donc j'ai laissé comme ça, car je n'avais pas de problèmes d'execution de cette manière
        if (delay < 0) {
            delay = 0
        }

        // val testDelay : Long = 10

        // On met les infos nécessaires dans un objet Data pour les transmettre au Worker
        val data = workDataOf(
            "userName" to userName,
            "houseId" to houseId,
            "token" to token
        )

        Log.d("POLYHOME_DATE", "$data")

        // On prépare une tâche unique qui appellera DeleteUserWorker
        val deleteRequest = OneTimeWorkRequestBuilder<DeleteUserWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS) // Executera avec le delai calcule
            .setInputData(data) // On passe les données voulues pour l'api API dans le worker qui a besoin du user, de l'houseId, et du token
            .addTag("delete_$userName") // Le tag permet de retrouver ou annuler la tâche a posteriori, inutilisé actuellement car le cas ou on veut changer la suppression du worker n'a pas été pris en compte
            .build()

        Log.d("POLYHOME_DATE", "$deleteRequest")

        // Envoie la tâche dans la file d'attente du WorkManager pour exécution plus tard
        WorkManager.getInstance(this).enqueue(deleteRequest)

        // DeleteUserWorker ne peut pas mettre à jour visuellement en direct la suppression du user de la liste
        // Pour ça, cela doit être UsersActicity qui s'en occupe, pour cela, on utilise un Observer sur le LiveData du WorkManager
        // pour écouter le changement d'état de la tâche et rafraîchir la liste dès que la suppression est confirmée

        // On vérifie la progression de la reguete de suppression
        WorkManager.getInstance(this)
            // getWorkInfoByIdLiveDate permet de recevoir l'information que le travail est fini
            // ce qui permet à UsersActivity d'executer la mise à jour visuelle une fois qu'on sait le travail fini
            .getWorkInfoByIdLiveData(deleteRequest.id) // on retrouve la requete de suppression grace à son id
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    // Si le Worker a fini son travail avec succès
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            // Rafraîchit la liste des utilisateurs après suppression
                            resetUserList(token, houseId)
                        }
                        else -> {
                            Log.d("POLYHOME_DATE", "Aucune suppression n'a eu lieu")
                        }
                    }
                }
            }

        Log.d("POLYHOME_DATE", "Envoi dans le workManager")
    }

}