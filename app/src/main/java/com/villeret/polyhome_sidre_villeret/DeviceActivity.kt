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

    private val api = Api()
    private lateinit var deviceAdapter: DeviceAdapter
    private var devicesList: List<DeviceData> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_device)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val token = intent.getStringExtra("token").toString()
        val id = intent.getStringExtra("id").toString()
        loadDevices(token, id)
        initAccessUsersButton(token, id)
        initBackButton()
        initGlobalActions(token, id)
    }

    private fun loadDevices(token: String, houseId: String) {
        api.get<DevicesContainer>("https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices", { code, container ->
            if (code == 200 && container != null) {
                devicesList = container.devices

                runOnUiThread {
                    deviceAdapter = DeviceAdapter(this@DeviceActivity, container, token, houseId)
                    initializeDeviceList()
                }
            } else {
                Log.e("POLYHOME_ID", "Erreur lors de la récupération : $code")
            }
        }, token)
    }

    private fun initGlobalActions(token: String, houseId: String) {
        val btnOpenAll = findViewById<Button>(R.id.btnOpenAllShutters)
        val btnCloseAll = findViewById<Button>(R.id.btnCloseAllShutters)
        val btnOnAll = findViewById<Button>(R.id.btnOnAllLights)
        val btnOffAll = findViewById<Button>(R.id.btnOffAllLights)
        // ouvrir tout
        btnOpenAll.setOnClickListener {
            devicesList.filter { it.type == "rolling shutter" }.forEach {
                sendCommandGlobal(it.id, "OPEN", token, houseId)
            }
        }
        // fermer tout
        btnCloseAll.setOnClickListener {
            devicesList.filter { it.type == "rolling shutter" }.forEach {
                sendCommandGlobal(it.id, "CLOSE", token, houseId)
            }
        }
        // allumer tout
        btnOnAll.setOnClickListener {
            devicesList.filter { it.type == "light" }.forEach {
                sendCommandGlobal(it.id, "TURN ON", token, houseId)
            }
        }
        // etindre tout
        btnOffAll.setOnClickListener {
            devicesList.filter { it.type == "light" }.forEach {
                sendCommandGlobal(it.id, "TURN OFF", token, houseId)
            }
        }
    }

    private fun sendCommandGlobal(deviceId: String, command: String, token: String, houseId: String) {
        api.post<CommandData>("https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command", CommandData(command), { code ->
            Log.d("POLYHOME_GLOBAL", "Appareil $deviceId : commande $command envoyée (Code $code)")
        }, token)
    }

    private fun initializeDeviceList() {
        val listView = findViewById<ListView>(R.id.deviceListViewDeviceActivity)
        listView.adapter = deviceAdapter
    }

    private fun initAccessUsersButton(token: String, id: String){
        val accessUsersButton = findViewById<Button>(R.id.accessUsersButtonDeviceActivity)
        accessUsersButton.setOnClickListener {
            val intentToGoToUsersActivity = Intent(this, UsersActivity::class.java)
            intentToGoToUsersActivity.putExtra("token", token)
            intentToGoToUsersActivity.putExtra("houseId", id)
            startActivity(intentToGoToUsersActivity)
        }
    }

    private fun initBackButton() {
        val backButton = findViewById<Button>(R.id.backButtonDeviceActivity)
        backButton.setOnClickListener { finish() }
    }
}