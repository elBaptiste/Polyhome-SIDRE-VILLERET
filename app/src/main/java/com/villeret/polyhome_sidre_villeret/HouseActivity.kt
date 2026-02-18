package com.villeret.polyhome_sidre_villeret

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import android.widget.Button

class HouseActivity : AppCompatActivity() {

    private val api = Api()
    private val housesArray = ArrayList<String>()
    private lateinit var houseAdapter: HouseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house)

        // Récupération du token passé par l'Intent
        val token = intent.getStringExtra("token")
        if (token != null) {
            fetchHouses(token)
        }

        // Initialisation du bouton de déconnexion
        val btnLogout = findViewById<Button>(R.id.btnLogoutHouseActivity)
        btnLogout.setOnClickListener {
            logout()
        }
    }
    // Recuperation des maisons de l'utilisateur et ajout dans un tableau
    private fun fetchHouses(token: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses"

        // Appel API pour récupérer la liste des maisons d'un utilisateur
        api.get<List<HouseResponse>>(url, { code, houses ->
            if (code == 200 && houses != null && houses.isNotEmpty()) {
                // On vide la liste avant d'ajouter pour éviter les doublons en cas de refresh
                housesArray.clear()
                for (element in houses) {
                    housesArray.add(element.houseId.toString())
                }
                Log.d("POLYHOME_ID", "Houses List ID : $housesArray")

                // On utilise runOnUiThread car cela permet de lancer initializeHouseList() sur le thread principal
                // Sinon on a l'erreur Only the original thread that created a view hierarchy can touch its views
                // Avec runOnUiThread on s'assure que ce soit le bon thread qui execute l'initialisation de la liste des maisons
                runOnUiThread {
                    houseAdapter = HouseAdapter(this@HouseActivity, housesArray, token)
                    val houseId = houses[0].houseId.toString()
                    Log.d("POLYHOME_ID", "ID maison : $houseId")
                    initializeHouseList()
                }

            } else {
                Log.e("POLYHOME_ID", "Erreur $code lors de la récupération des maisons")
            }
        }, token)
    }

    // Initialisation de la liste des maison d'un utilisateur
    private fun initializeHouseList() {
        // Récupère la vue de la liste dans le layout
        val listView = findViewById<ListView>(R.id.houseListViewHouseActivity)
        Log.d("POLYHOME_HOUSE_ADAPTER", "House Adapter list  : $houseAdapter")
        // Lie l'adapter à la ListView pour afficher les maisons
        listView.adapter = houseAdapter
    }

    private fun logout() {
        // supression du token dans les préférences
        val sharedPref = getSharedPreferences("PolyhomePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("user_token")
            apply()
        }

        // redirection vers LoginActivity
        val intentLogout = Intent(this, LoginActivity::class.java)
        intentLogout.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intentLogout)
        finish()
    }
}