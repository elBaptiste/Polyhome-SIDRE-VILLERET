package com.villeret.polyhome_sidre_villeret

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

class HouseAdapter(private val context: Context, private val houses: ArrayList<String>, private val token: String) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int {
        return houses.size
    }

    override fun getItem(position: Int): Any? {
        return houses[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = convertView ?: inflater.inflate(R.layout.house_list_item, parent, false)

        val houseId = rowView.findViewById<TextView>(R.id.houseNameHouseList)
        val accessBtn = rowView.findViewById<Button>(R.id.houseAccessButtonHouseList)

        houseId.text = "${houseId.text} ${houses[position]} "

        accessBtn.setOnClickListener { view ->
            Log.d("AccessButtonClick", "A ete clique")

            // On récupère le context de la vue (ici ça sera HouseActivity) où le bouton est situé car on veut que ce bouton puisse ouvrir une activité
            // On a besoin du context de la vue pour faire une intent
            // On lance ensuite startActivity qui va nous permette de lancer la vue des appareils avec l'id de la maison et le token en paramètres
            val context = view.context
            val intent = Intent(context, DeviceActivity::class.java)
            intent.putExtra("id", houses[position])
            intent.putExtra("token", token)

            Log.d("POLYHOME_INTENT", houses[position])
            Log.d("POLYHOME_INTENT", token)

            context.startActivity(intent)

        }

        return rowView
    }

}