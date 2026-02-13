package com.villeret.polyhome_sidre_villeret

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        Api().post("https://polyhome.lesmoulinsdudev.com/api/users/auth", data, ::loginSucces)
    }
    fun loginSucces(responseCode: Int, response: LoginToken?) {
        if (responseCode == 200 && response?.token != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("token", response.token)
            startActivity(intent)

            println("ca marche ! Token : ${response.token}")
            finish()
        } else {
            println("ca marche pas : code = $responseCode, token est null")
        }
    }
    public fun registerNewAccount(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }
}