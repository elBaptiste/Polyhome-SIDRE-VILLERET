package com.villeret.polyhome_sidre_villeret

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class HouseActivity : AppCompatActivity() {

    private val api = Api()
    private val housesArray = ArrayList<String>()
    private lateinit var houseAdapter: HouseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house)

        val token = intent.getStringExtra("token")
        if (token != null) {
            fetchHouses(token)
        }
    }
    // Recuperation des maisons de l'utilisateur et ajout dans un tableau
    private fun fetchHouses(token: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses"

        // Appel API pour récupérer la liste des maisons d'un utilisateur
        api.get<List<HouseResponse>>(url, { code, houses ->
            if (code == 200 && houses != null && houses.isNotEmpty()) {
                for (element in houses) {
                    housesArray.add(element.houseId.toString())
                }
                Log.d("POLYHOME_ID", "Houses List ID : $housesArray")

                // On utilise runOnUiThread car cela permet de lancer initializeHouseList() sur le thread principal
                // Sinon on a l'erreur Only the original thread that created a view hierarchy can touch its views
                // Avec runOnUiThread on s'assure que ce soit le bon thread qui execute l'initialisation de la liste des maisons
                runOnUiThread {
                    houseAdapter = HouseAdapter(this@HouseActivity, housesArray, token)
                    initializeHouseList()
                }

            } else {
                Log.e("POLYHOME_ID", "Erreur lors de la récupération de l'ID")
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
}