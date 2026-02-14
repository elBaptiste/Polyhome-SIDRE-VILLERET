package com.villeret.polyhome_sidre_villeret

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

class HouseAdapter(private val context: Context, private val houses: ArrayList<String>) : BaseAdapter() {
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

        accessBtn.setOnClickListener {
            Log.d("AccessButtonClick", "A ete clique")
        }

        return rowView
    }

}