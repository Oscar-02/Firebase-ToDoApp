package com.udb.desafio2dsm

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest

private lateinit var auth: FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginButton)
        val infoBar = findViewById<TextView>(R.id.infoBar)

        val registerViewActivity = findViewById<TextView>(R.id.toRegister)

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                mostrarMensaje("Por favor complete todos los campos", Color.RED)
            } else {
                loginUsuario(email, cifrarSHA256(password))
            }
        }

        registerViewActivity.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }

    private fun loginUsuario(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    mostrarMensaje("¡Login exitoso!", Color.parseColor("#4CAF50"))

                    // Ir a otra pantalla (MainActivity, por ejemplo)
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    mostrarMensaje("Error: ${task.exception?.message}", Color.RED)
                }
            }
    }

    fun cifrarSHA256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun mostrarMensaje(texto: String, colorFondo: Int) {
        val infoBar = findViewById<TextView>(R.id.infoBar)
        infoBar.text = texto
        infoBar.setBackgroundColor(colorFondo)
        infoBar.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            infoBar.visibility = View.GONE
        }, 3000)
    }
}