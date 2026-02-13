package com.villeret.polyhome_sidre_villeret

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class HouseActivity : AppCompatActivity() {

    private val api = Api()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_house)

        val token = intent.getStringExtra("token")
        if (token != null) {
            fetchHouseId(token)
        }
    }

    private fun fetchHouseId(token: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses"

        api.get<List<HouseResponse>>(url, { code, houses ->
            if (code == 200 && houses != null && houses.isNotEmpty()) {
                val houseId = houses[0].houseId.toString()
                Log.d("POLYHOME_ID", "ID maison : $houseId")
                loadDevices(token, houseId)
            } else {
                Log.e("POLYHOME_ID", "Erreur lors de la récupération de l'ID")
            }
        }, token)
    }

    private fun loadDevices(token: String, houseId: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices"

        api.get<DevicesContainer>(url, { code, container ->
            if (code == 200 && container != null) {
                Log.d("POLYHOME_DEVICE", "Connexion réussie à la maison $houseId")
                for (device in container.devices) {
                    Log.d("POLYHOME_DEVICE", "Appareil présent dans la maison : ${device.id}")
                }
            }
        }, token)
    }
}