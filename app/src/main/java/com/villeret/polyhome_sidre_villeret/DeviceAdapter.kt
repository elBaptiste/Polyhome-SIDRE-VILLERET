package com.villeret.polyhome_sidre_villeret

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class DeviceAdapter(private val context: Context, private val devicesList: DevicesContainer, private val token: String, private val houseId: String) : BaseAdapter() {
    private val api = Api()
    override fun getCount(): Int = devicesList.devices.size
    override fun getItem(position: Int): Any = devicesList.devices[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = convertView ?: inflater.inflate(R.layout.device_list_item, parent, false)

        val targetedDevice = devicesList.devices[position]

        val idTxt = rowView.findViewById<TextView>(R.id.idDeviceListItem)
        val typeTxt = rowView.findViewById<TextView>(R.id.typeDeviceListItem)
        val container = rowView.findViewById<LinearLayout>(R.id.layoutButtonsContainer)

        idTxt.text = "ID: ${targetedDevice.id}"
        typeTxt.text = "Type: ${targetedDevice.type}"

        container.removeAllViews()
        targetedDevice.availableCommands.forEach { command ->
            val btn = Button(context)
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            params.setMargins(8, 0, 8, 0) // Un peu d'espace entre les boutons
            btn.layoutParams = params
            btn.text = command
            btn.textSize = 12f
            btn.setTextColor(Color.WHITE)
            btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Ton vert

            btn.setOnClickListener {
                Log.d("POLYHOME_ACTION", "Envoi de $command pour l'appareil ${targetedDevice.id}")
                sendCommand(targetedDevice.id, command, token, houseId)
            }

            // On ajoute le bouton au conteneur
            container.addView(btn)
        }

        return rowView
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