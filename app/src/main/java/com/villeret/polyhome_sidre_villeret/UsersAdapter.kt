package com.villeret.polyhome_sidre_villeret

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.security.Security

class UsersAdapter(private val context: Context, private val users: ArrayList<UsersData>, private val houseID: String, private val securityToken: String) : BaseAdapter() {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val api = Api()

    override fun getCount(): Int {
        return users.size
    }

    override fun getItem(position: Int): Any? {
        return users[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = convertView ?: inflater.inflate(R.layout.user_list_item, parent, false)

        val userLogin = rowView.findViewById<TextView>(R.id.userNameTextVIewUserList)
        val ownerValue = rowView.findViewById<TextView>(R.id.ownerValueTextViewUserList)
        val removeBtn = rowView.findViewById<Button>(R.id.deleteUserButtonUserList)

        userLogin.text = users[position].userLogin

        Log.d("POLYHOME_USER", "owner ? ${users[position].owner}")

        if (users[position].owner.toString() == "1") {
            ownerValue.text = "Propriétaire"
            removeBtn.visibility = View.GONE
        } else {
            ownerValue.text = "Personne tiers"
            removeBtn.visibility = View.VISIBLE
        }

        removeBtn.setOnClickListener { view ->
            Log.d("RemoveButtonClick", "A ete clique")
            val url = "https://polyhome.lesmoulinsdudev.com/api/houses/${houseID}/users"
            val data = UserLogin(users[position].userLogin)

            api.delete(
                url, data,{code ->
                    if (code == 200 && users[position].owner.toString() != "1") {
                        users.removeAt(position)
                        Log.d("REMOVE_USER", "ça va faire notifyDataSetChanged")
                        (context as? Activity)?.runOnUiThread {
                            Log.d("REMOVE_USER", "ça va faire notifyDataSetChanged")
                            notifyDataSetChanged()
                        }
                    }
                    else {
                        Log.d("RemoveButtonClick", "$code")
                    }
                },
                securityToken
            )

        }

        return rowView
    }

}