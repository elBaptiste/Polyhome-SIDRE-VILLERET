package com.villeret.polyhome_sidre_villeret

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

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

    /// fonction recuperation des maisons
    private fun fetchHouses(token: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses"

        api.get<List<HouseResponse>>(url, { code, houses ->
            if (code == 200 && houses != null && houses.isNotEmpty()) {
                // On vide la liste avant d'ajouter pour éviter les doublons en cas de refresh
                housesArray.clear()
                for (element in houses) {
                    housesArray.add(element.houseId.toString())
                }
                Log.d("POLYHOME_ID", "Houses List ID : $housesArray")

                // Passage sur le thread principal pour l'UI
                runOnUiThread {
                    houseAdapter = HouseAdapter(this@HouseActivity, housesArray, token)
                    initializeHouseList()
                }

            } else {
                Log.e("POLYHOME_ID", "Erreur $code lors de la récupération des maisons")
            }
        }, token)
    }

    private fun initializeHouseList() {
        val listView = findViewById<ListView>(R.id.houseListViewHouseActivity)
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