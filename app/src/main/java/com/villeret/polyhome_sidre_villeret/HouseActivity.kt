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

    private fun fetchHouses(token: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses"

        api.get<List<HouseResponse>>(url, { code, houses ->
            if (code == 200 && houses != null && houses.isNotEmpty()) {
                for (element in houses) {
                    housesArray.add(element.houseId.toString())
                }
                Log.d("POLYHOME_HOUSE_LIST_ID", "Houses List ID : $housesArray")
                houseAdapter = HouseAdapter(this, housesArray, token)


                val houseId = houses[0].houseId.toString()
                Log.d("POLYHOME_ID", "ID maison : $houseId")
                initializeHouseList()

            } else {
                Log.e("POLYHOME_ID", "Erreur lors de la récupération de l'ID")
            }
        }, token)
    }

    private fun initializeHouseList() {
        val listView = findViewById<ListView>(R.id.houseListViewHouseActivity)
        Log.d("POLYHOME_HOUSE_ADAPTER", "House Adapter list  : test")
        Log.d("POLYHOME_HOUSE_ADAPTER", "House Adapter list  : $houseAdapter")
        listView.adapter = houseAdapter

    }
}