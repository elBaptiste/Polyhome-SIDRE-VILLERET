package com.villeret.polyhome_sidre_villeret

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class DeviceAdapter(private val context: Context, private val devicesList: DevicesContainer) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int {
        return devicesList.devices.size
    }

    override fun getItem(position: Int): Any? {
        return devicesList.devices[position]
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
        val rowView = convertView ?: inflater.inflate(R.layout.device_list_item, parent, false)
        val targetedDevice = devicesList.devices[position]

        val id = rowView.findViewById<TextView>(R.id.idDeviceListItem)
        val type = rowView.findViewById<TextView>(R.id.typeDeviceListItem)
        val availableCommands = rowView.findViewById<TextView>(R.id.availableCommandsDeviceListItem)

        id.text = "ID: ${targetedDevice.id}"
        type.text = "Type: ${targetedDevice.type}"
        availableCommands.text = "${targetedDevice.availableCommands}"

        return rowView
    }

}