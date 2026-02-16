package com.villeret.polyhome_sidre_villeret

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DeviceActivity : AppCompatActivity() {

    // RAJOUTER UNE INTENT DANS HOUSE ACTIVITY QUI VIENT PUSH LID DE LA MAISON ET POUVOIR LA LIRE ICI
    // RAJOUTER UNE OPTION QUAND ON CLIQUE POUR OPEN CETTE ACTIVITY
    private val api = Api()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_device)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val token = intent.getStringExtra("token")
        val id = intent.getStringExtra("id").toString()
        if (token != null) {
            loadDevices(token, id)
        }
    }

    private fun loadDevices(token: String, houseId: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices"

        api.get<DevicesContainer>(url, { code, container ->
            if (code == 200 && container != null) {
                Log.d("POLYHOME_DEVICE", "Connexion réussie à la maison $houseId")
                for (device in container.devices) {
                    Log.d("POLYHOME_DEVICE", "Appareil présent dans la maison : ${device.id}")
                }
                runOnUiThread {
                    deviceAdapter = DeviceAdapter(this@DeviceActivity, container)
                    Log.d("POLYHOME_DEVICES", "device Adapter : ${deviceAdapter}")
                    initializeDeviceList()
                }
            }
            else {
                Log.e("POLYHOME_ID", "Erreur lors de la récupération de l'ID")
            }
        }, token)
    }

    private fun initializeDeviceList() {
        val listView = findViewById<ListView>(R.id.deviceListViewDeviceActivity)
        Log.d("POLYHOME_DEVICE_ADAPTER", "Device Adapter list  : $deviceAdapter")
        listView.adapter = deviceAdapter

    }
}
