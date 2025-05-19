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

class EditTaskActivity : AppCompatActivity() {

    private lateinit var etEditTitle: EditText
    private lateinit var etEditDescription: EditText
    private lateinit var btnActualizar: Button
    private lateinit var auth: FirebaseAuth

    private var tareaId: String? = null
    private var originalCreatedAt: String = ""
    private var originalCreatedBy: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        etEditTitle = findViewById(R.id.etEditTitle)
        etEditDescription = findViewById(R.id.etEditDescription)
        btnActualizar = findViewById(R.id.btnActualizar)
        auth = FirebaseAuth.getInstance()

        // Cargar datos de la tarea desde el intent
        tareaId = intent.getStringExtra("tarea_id")
        val title = intent.getStringExtra("tarea_title")
        val description = intent.getStringExtra("tarea_description")
        val user = auth.currentUser

        if (tareaId == null || user == null) {
            Toast.makeText(this, "Error cargando tarea", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        etEditTitle.setText(title)
        etEditDescription.setText(description)

        // Recuperamos valores que no deben cambiar
        originalCreatedBy = user.displayName ?: user.email ?: "Anonimo"
        originalCreatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())

        btnActualizar.setOnClickListener {
            actualizarTarea()
        }
    }

    private fun actualizarTarea() {
        val newTitle = etEditTitle.text.toString().trim()
        val newDescription = etEditDescription.text.toString().trim()

        if (newTitle.isEmpty() || newDescription.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val tareaActualizada = ToDo(
            id = tareaId!!,
            title = newTitle,
            description = newDescription,
            done = false,
            createdAt = originalCreatedAt,
            createdBy = originalCreatedBy
        )

        ApiClient.toDoApiService.updateToDo(tareaId!!, tareaActualizada)
            .enqueue(object : Callback<ToDo> {
                override fun onResponse(call: Call<ToDo>, response: Response<ToDo>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditTaskActivity, "Tarea actualizada", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@EditTaskActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ToDo>, t: Throwable) {
                    Toast.makeText(this@EditTaskActivity, "Fallo: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
