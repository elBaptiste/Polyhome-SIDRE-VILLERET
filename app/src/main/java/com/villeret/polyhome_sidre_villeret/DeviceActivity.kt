package com.villeret.polyhome_sidre_villeret

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
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
        initAccessUsersButton(token.toString(), id)
        initBackButton()
    }

    // Fonction pour appeler l'api et récupérer la liste de tous les appareils connectés à la maison
    private fun loadDevices(token: String, houseId: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices"

        // Appel API pour obtenir la liste de tous les devices d'une maison
        api.get<DevicesContainer>(url, { code, container ->
            if (code == 200 && container != null) {
                Log.d("POLYHOME_DEVICE", "Connexion réussie à la maison $houseId")
                for (device in container.devices) {
                    Log.d("POLYHOME_DEVICE", "Appareil présent dans la maison : ${device.id}")
                }
                // On utilise runOnUiThread car cela permet de lancer initializeDeviceList() sur le thread principal
                // Sinon on a l'erreur Only the original thread that created a view hierarchy can touch its views
                // Avec runOnUiThread on s'assure que ce soit le bon thread qui execute l'initialisation des devices
                runOnUiThread {
                    deviceAdapter = DeviceAdapter(this@DeviceActivity, container,token,houseId)
                    Log.d("POLYHOME_DEVICES", "device Adapter : ${deviceAdapter}")
                    initializeDeviceList()
                }
            }
            else {
                Log.e("POLYHOME_ID", "Erreur lors de la récupération de l'ID")
            }
        }, token)
    }

    // Initialisation de la liste des devices d'une maison
    private fun initializeDeviceList() {
        // Récupère la vue de la liste dans le layout
        val listView = findViewById<ListView>(R.id.deviceListViewDeviceActivity)
        Log.d("POLYHOME_DEVICE_ADAPTER", "Device Adapter list  : $deviceAdapter")
        // Lie l'adapter à la ListView pour afficher les données des appareils
        listView.adapter = deviceAdapter

    }

    // Initialisation du bouton d'accès aux utilisateurs, on va le chercher et on utilise un setOnClickListener pour aller sur l'activité suivante
    private fun initAccessUsersButton(token: String, id: String){
        val accessUsersButton = findViewById<Button>(R.id.accessUsersButtonDeviceActivity)

        accessUsersButton.setOnClickListener {
            val intentToGoToUsersActivity = Intent(this, UsersActivity::class.java)
            // On ajoute en paramètres le token et l'id de la maison pour que l'activité suivante les connaissent
            intentToGoToUsersActivity.putExtra("token", token)
            intentToGoToUsersActivity.putExtra("houseId", id)
            startActivity(intentToGoToUsersActivity)
        }
    }

    // Initialisation du bouton Retour, on va le chercher et on utilise un setOnClickListener qui va fermer l'activité
    private fun initBackButton() {
        val backButton = findViewById<Button>(R.id.backButtonDeviceActivity)
        backButton.setOnClickListener {
            finish()
        }

    }
}

