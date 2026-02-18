package com.villeret.polyhome_sidre_villeret

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // verification token existant
        val sharedPref = getSharedPreferences("PolyhomePrefs", Context.MODE_PRIVATE)
        val savedToken = sharedPref.getString("user_token", null)
        val intent = Intent(this, HouseActivity::class.java)

        if (savedToken != null) {
            // affichage de l'écran des maisons si token existant
            intent.putExtra("token", savedToken)
            startActivity(intent)
            finish()
            return // arret de l'exécution de onCreate
        }

        // affichage de l'écran de connexion si pas de token
        setContentView(R.layout.activity_login)
        val button = findViewById<Button>(R.id.button_connexion)
        button.setOnClickListener {
            login(button)
        }
    }

    fun login(view: View) {
        val email = findViewById<EditText>(R.id.txtMail).text.toString()
        val password = findViewById<EditText>(R.id.txtPassword).text.toString()
        val data = LoginData(email, password)

        Api().post("https://polyhome.lesmoulinsdudev.com/api/users/auth", data, ::loginSuccess)
    }

    fun loginSuccess(responseCode: Int, response: LoginToken?) {
        val intent = Intent(this, HouseActivity::class.java)
        if (responseCode == 200 && response?.token != null) {
            // sauvegarde du token dans les préférences
            val sharedPref = getSharedPreferences("PolyhomePrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("user_token", response.token)
                apply()
            }
            intent.putExtra("token", response.token)
            startActivity(intent)
            finish()
        } else {
            println("Échec de connexion : code = $responseCode")
        }
    }
    public fun registerNewAccount(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}