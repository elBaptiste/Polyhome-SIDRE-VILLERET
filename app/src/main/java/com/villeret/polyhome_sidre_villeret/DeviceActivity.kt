package com.villeret.polyhome_sidre_villeret

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class DeviceActivity : AppCompatActivity() {

    private val api = Api()
    /// adapter pour la liste des appareils qui sera initialisé dans loadDevices
    private lateinit var deviceAdapter: DeviceAdapter
    private var devicesList: List<DeviceData> = listOf()

    // variable programmation automatique stockés dans les préférences
    private var openHour = 8
    private var openMinute = 0
    private var closeHour = 20
    private var closeMinute = 0
    /// gestion programmation automatique avec un handler pour la mise à jour de l'affichage
    /// planifie une vérification récursive chaque minute sans bloquer le thread principal.
    private val handler = Handler(Looper.getMainLooper())
    /// variable vérification heure
    private lateinit var checkTimeRunnable: Runnable

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

        loadSchedules(id)
        loadDevices(token, id)
        initAccessUsersButton(token, id)
        initBackButton()
        initGlobalActions(token, id)
        initCalendarButtons(id)
        startAutoCheck(token, id)
    }

    /// fonction chargement des horaires enregistrées dans les préférences
    private fun loadSchedules(houseId: String) {
        val sharedPref = getSharedPreferences("PolyhomePrefs", Context.MODE_PRIVATE)
        // chargement des horaires depuis les préférences
        openHour = sharedPref.getInt("openHour_$houseId", 8)
        openMinute = sharedPref.getInt("openMinute_$houseId", 0)
        closeHour = sharedPref.getInt("closeHour_$houseId", 20)
        closeMinute = sharedPref.getInt("closeMinute_$houseId", 0)

        // Maj l'affichage
        findViewById<TextView>(R.id.txtHourOpen).text = String.format("%02d", openHour)
        findViewById<TextView>(R.id.txtMinOpen).text = String.format("%02d", openMinute)
        findViewById<TextView>(R.id.txtHourClose).text = String.format("%02d", closeHour)
        findViewById<TextView>(R.id.txtMinClose).text = String.format("%02d", closeMinute)
    }
    /// fonction sauvegarde des horaires dans les préférences
    private fun saveSchedules(houseId: String) {
        val sharedPref = getSharedPreferences("PolyhomePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            // On crée des clés uniques comme "openHour_IDdelamaison"
            putInt("openHour_$houseId", openHour)
            putInt("openMinute_$houseId", openMinute)
            putInt("closeHour_$houseId", closeHour)
            putInt("closeMinute_$houseId", closeMinute)
            apply()
        }
    }
    /// fonction initialisation des boutons de programmation automatique
    private fun initCalendarButtons(houseId: String) {
        val hOpenTxt = findViewById<TextView>(R.id.txtHourOpen)
        val mOpenTxt = findViewById<TextView>(R.id.txtMinOpen)
        val hCloseTxt = findViewById<TextView>(R.id.txtHourClose)
        val mCloseTxt = findViewById<TextView>(R.id.txtMinClose)

        /// on fait en sorte que l'affichage et le choix de l'heure soit optimisé
        /// gestion du passage de 59 a 0 et de 23 a 0
        findViewById<Button>(R.id.btnPlusHourOpen).setOnClickListener {
            openHour = (openHour + 1) % 24
            hOpenTxt.text = String.format("%02d", openHour)
            saveSchedules(houseId)
        }
        findViewById<Button>(R.id.btnMinusHourOpen).setOnClickListener {
            openHour = if (openHour > 0) openHour - 1 else 23
            hOpenTxt.text = String.format("%02d", openHour)
            saveSchedules(houseId)
        }
        findViewById<Button>(R.id.btnPlusMinOpen).setOnClickListener {
            openMinute = (openMinute + 1) % 60
            mOpenTxt.text = String.format("%02d", openMinute)
            saveSchedules(houseId)
        }
        findViewById<Button>(R.id.btnMinusMinOpen).setOnClickListener {
            openMinute = if (openMinute > 0) openMinute - 1 else 59
            mOpenTxt.text = String.format("%02d", openMinute)
            saveSchedules(houseId)
        }
        findViewById<Button>(R.id.btnPlusHourClose).setOnClickListener {
            closeHour = (closeHour + 1) % 24
            hCloseTxt.text = String.format("%02d", closeHour)
            saveSchedules(houseId)
        }
        findViewById<Button>(R.id.btnMinusHourClose).setOnClickListener {
            closeHour = if (closeHour > 0) closeHour - 1 else 23
            hCloseTxt.text = String.format("%02d", closeHour)
            saveSchedules(houseId)
        }
        findViewById<Button>(R.id.btnPlusMinClose).setOnClickListener {
            closeMinute = (closeMinute + 1) % 60
            mCloseTxt.text = String.format("%02d", closeMinute)
            saveSchedules(houseId)
        }
        findViewById<Button>(R.id.btnMinusMinClose).setOnClickListener {
            closeMinute = if (closeMinute > 0) closeMinute - 1 else 59
            mCloseTxt.text = String.format("%02d", closeMinute)
            saveSchedules(houseId)
        }
    }
    /// fonction de programmation automatique avec un handler
    private fun startAutoCheck(token: String, houseId: String) {
        checkTimeRunnable = object : Runnable {
            override fun run() {
                val nowH = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val nowM = Calendar.getInstance().get(Calendar.MINUTE)
                if (nowH == openHour && nowM == openMinute) {
                    devicesList.filter { it.type == "rolling shutter" }.forEach {
                        sendCommandGlobal(it.id, "OPEN", token, houseId)
                    }
                }
                if (nowH == closeHour && nowM == closeMinute) {
                    devicesList.filter { it.type == "rolling shutter" }.forEach {
                        sendCommandGlobal(it.id, "CLOSE", token, houseId)
                    }
                }
                handler.postDelayed(this, 60000) /// vérification toutes les minutes
            }
        }
        handler.post(checkTimeRunnable) /// lancement de la boucle
    }
    /// fonction recuperer tous les appareils
    private fun loadDevices(token: String, houseId: String) {
        api.get<DevicesContainer>("https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices", { code, container ->
            if (code == 200 && container != null) {
                devicesList = container.devices
                runOnUiThread {
                    deviceAdapter = DeviceAdapter(this@DeviceActivity, container, token, houseId)
                    findViewById<ListView>(R.id.deviceListViewDeviceActivity).adapter = deviceAdapter
                }
            }
        }, token)
    }
    /// fonction de gestion des ouverture/fermeture et allumage/extinction globale
    private fun initGlobalActions(token: String, houseId: String) {
        findViewById<Button>(R.id.btnOpenAllShutters).setOnClickListener {
            devicesList.filter { it.type == "rolling shutter" }.forEach { sendCommandGlobal(it.id, "OPEN", token, houseId) }
        }
        findViewById<Button>(R.id.btnCloseAllShutters).setOnClickListener {
            devicesList.filter { it.type == "rolling shutter" }.forEach { sendCommandGlobal(it.id, "CLOSE", token, houseId) }
        }
        findViewById<Button>(R.id.btnOnAllLights).setOnClickListener {
            devicesList.filter { it.type == "light" }.forEach { sendCommandGlobal(it.id, "TURN ON", token, houseId) }
        }
        findViewById<Button>(R.id.btnOffAllLights).setOnClickListener {
            devicesList.filter { it.type == "light" }.forEach { sendCommandGlobal(it.id, "TURN OFF", token, houseId) }
        }
    }
    /// fonction de gestion des commandes globales
    private fun sendCommandGlobal(deviceId: String, command: String, token: String, houseId: String) {
        api.post<CommandData>("https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command", CommandData(command), { code ->
            Log.d("AUTO_COMMAND", "Device $deviceId : $command (Code $code)")
        }, token)
    }
    /// fonction d'accès aux utilisateurs de la maison
    private fun initAccessUsersButton(token: String, id: String) {
        findViewById<Button>(R.id.accessUsersButtonDeviceActivity).setOnClickListener {
            val intent = Intent(this, UsersActivity::class.java).apply {
                putExtra("token", token)
                putExtra("houseId", id)
            }
            startActivity(intent)
        }
    }

    // Initialisation du bouton Retour, on va le chercher et on utilise un setOnClickListener qui va fermer l'activité
    private fun initBackButton() {
        findViewById<Button>(R.id.backButtonDeviceActivity).setOnClickListener { finish() }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkTimeRunnable) // Arrêt de la boucle de vérification
    }
}