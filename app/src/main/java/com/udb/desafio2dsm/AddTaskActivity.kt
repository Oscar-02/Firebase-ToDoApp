package com.udb.desafio2dsm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.udb.desafio2dsm.api.ApiClient
import com.udb.desafio2dsm.model.ToDo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnGuardar: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        btnGuardar = findViewById(R.id.btnGuardar)
        auth = FirebaseAuth.getInstance()

        btnGuardar.setOnClickListener {
            guardarTarea()
        }
    }

    private fun guardarTarea() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val user = auth.currentUser

        if (title.isEmpty() || description.isEmpty() || user == null) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            .format(Date())

        val createdBy = user.displayName ?: user.email ?: "Anonimo"

        val tarea = ToDo(
            title = title,
            description = description,
            done = false,
            createdAt = createdAt,
            createdBy = createdBy
        )

        ApiClient.toDoApiService.createToDo(tarea).enqueue(object : Callback<ToDo> {
            override fun onResponse(call: Call<ToDo>, response: Response<ToDo>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AddTaskActivity, "Tarea guardada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AddTaskActivity, "Error al guardar tarea", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ToDo>, t: Throwable) {
                Toast.makeText(this@AddTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
