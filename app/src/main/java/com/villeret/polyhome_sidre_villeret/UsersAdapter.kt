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

        // Si la valeur du owner du User est 1 c'est le propriétaire sinon c'est une personne tiers auquel on a donné l'accès
        if (users[position].owner.toString() == "1") {
            ownerValue.text = "Propriétaire"
            // Par sécurité pour éviter de supprimer le propriétaire d'une maison, on cache le bouton de l'activité
            removeBtn.visibility = View.GONE
        } else {
            ownerValue.text = "Personne tiers"
            removeBtn.visibility = View.VISIBLE
        }

        removeBtn.setOnClickListener { view ->
            Log.d("RemoveButtonClick", "A ete clique")
            val url = "https://polyhome.lesmoulinsdudev.com/api/houses/${houseID}/users"
            val data = UserLogin(users[position].userLogin)

            // Deuxième sécurité pour éviter de supprimer un propriétaire
            // Cette sécurité peut être superflu mais imaginons que le bouton supprimer soit toujours clickable, cela évite la suppression
            if (users[position].owner.toString() != "1") {
                // Appel API pour supprimer un utilisateur
                api.delete(
                    url, data,{code ->
                        if (code == 200) {
                            // On enlève l'utilisateur de notre liste d'utilisateur
                            users.removeAt(position)

                            // On force l'execution sur le thread principal
                            // car notifyDataSetChanged() ne peut pas être appelé depuis un thread secondaire
                            // hors comme un adapter n'est pas une activity, il ne connait pas runOnUiThread, il faut donc l'appliquer au context de ce dernier
                            // mais comme le context n'est pas forcément une activité, on doit s'assurer que s'en est une pour le bon fonctionnement de runOnUiThread
                            (context as? Activity)?.runOnUiThread {
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
        }

        return rowView
    }

}