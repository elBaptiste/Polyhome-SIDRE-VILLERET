package com.villeret.polyhome_sidre_villeret

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Register : AppCompatActivity() {
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
        val login = findViewById<EditText>(R.id.editTextLogin)
        val password = findViewById<EditText>(R.id.editTextPassword)

        val registerData = RegisterData(login.text.toString(), password.text.toString())

        Api().post<RegisterData>(
            "https://polyhome.lesmoulinsdudev.com/api/users/register",
            registerData,
            onSuccess = { status -> successfulRegister(status) }
        )
    }

    fun successfulRegister(statusCode: Int) {
        Log.d("Register", "$statusCode")
        if (statusCode == 200) {
            goToLogin()
        }

        else {
            val message = findViewById<TextView>(R.id.InscriptionRegisterText)
            message.text = "ERREUR DE CONNEXION"
        }
    }

    fun goToLogin() {
        finish()
    }
}