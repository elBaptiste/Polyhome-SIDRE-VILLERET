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
        api.get<List<HouseResponse>>("https://polyhome.lesmoulinsdudev.com/api/houses", { code, houses ->
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
        api.get<DevicesContainer>("https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices", { code, container ->
            if (code == 200 && container != null) {
                Log.d("POLYHOME_DEVICE", "Connexion réussie à la maison $houseId")
                for (device in container.devices) {
                    Log.d("POLYHOME_DEVICE", "Appareil présent dans la maison : ${device.id}")

                    ///if (device.id == "GarageDoor 1.1") {
                    ///        Log.d("POLYHOME_DEVICE", "Tentative d'ouverture du portail...")
                    ///        sendCommand(device.id, "OPEN", token, houseId)
                    ///}
                }
            }
            else {
                Log.e("POLYHOME_DEVICE", "Pensez à ouvrir le navigateur")
            }
        }, token)
    }

    private fun sendCommand(deviceId: String, commandName: String, token: String, houseId: String) {
        api.post<CommandData>("https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command", CommandData(commandName), { code ->
            if (code == 200) {
                Log.d("POLYHOME_COMMAND", "Succès : Commande $commandName envoyée à $deviceId")
            } else {
                Log.e("POLYHOME_COMMAND", "Erreur $code : Vérifiez si la maison est ouverte dans le navigateur")
            }
        }, token)
    }
}