package com.villeret.polyhome_sidre_villeret

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast // Import ajouté
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initButtonEvent()
    }

    private fun initButtonEvent() {
        val registerButton = findViewById<Button>(R.id.registerButton)
        val connexionButton = findViewById<Button>(R.id.connexionRegisterButton)
        registerButton.setOnClickListener {
            register()
        }

        connexionButton.setOnClickListener {
            goToLogin()
        }
    }

    fun register() {
        val login = findViewById<EditText>(R.id.editTextLogin).text.toString()
        val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
        val registerData = RegisterData(login, password)
        Api().post("https://polyhome.lesmoulinsdudev.com/api/users/register", registerData, ::successfulRegister)
    }
    /// fonction de gestion de l'inscription
    fun successfulRegister(statutCode: Int) {
        if (statutCode == 200) {
            runOnUiThread {
                Toast.makeText(this, "Le compte a bien été créé", Toast.LENGTH_LONG).show()
                goToLogin()
            }
        } else {
            runOnUiThread {
                val message = when (statutCode) {
                    400 -> "Les données fournies sont incorrectes"
                    409 -> "Le login est déjà utilisé par un autre compte"
                    500 -> "Une erreur s’est produite au niveau du serveur"
                    else -> "Erreur inconnue (Code $statutCode)"
                }

                // affichage de l'erreur dans le Toast
                Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun goToLogin() {
        finish()
    }
}