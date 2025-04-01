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

private lateinit var auth : FirebaseAuth

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

        auth = FirebaseAuth.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailRegisterInput)
        val passwordInput = findViewById<EditText>(R.id.passwordRegisterInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                mostrarMensaje("Todos los campos son obligatorios", Color.RED)
            } else if (password != confirmPassword) {
                mostrarMensaje("Las contraseñas no coinciden", Color.RED)
            } else {
                registrarUsuario(email, cifrarSHA256(password))
            }
        }
    }

    private fun registrarUsuario(email: String, passwordCifrada: String) {
        auth.createUserWithEmailAndPassword(email, passwordCifrada)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    mostrarMensaje("¡Registro exitoso!", Color.parseColor("#4CAF50"))

                    // Ir a pantalla principal (o login si preferís)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    mostrarMensaje("Error: ${task.exception?.message}", Color.RED)
                }
            }
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

    private fun cifrarSHA256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
