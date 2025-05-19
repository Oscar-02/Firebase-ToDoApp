package com.udb.desafio2dsm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.udb.desafio2dsm.adapter.ToDoAdapter
import com.udb.desafio2dsm.api.ApiClient
import com.udb.desafio2dsm.model.ToDo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var toDoAdapter: ToDoAdapter
    private lateinit var logoutButton: Button

    override fun onResume() {
        super.onResume()
        obtenerTareas()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.logoutButton)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        toDoAdapter = ToDoAdapter(
            emptyList(),
            onEditClick = { tarea ->
                val intent = Intent(this, EditTaskActivity::class.java)
                intent.putExtra("tarea_id", tarea.id)
                intent.putExtra("tarea_title", tarea.title)
                intent.putExtra("tarea_description", tarea.description)
                startActivity(intent)
            },
            onDeleteClick = { tarea ->
                eliminarTarea(tarea.id)
            },
            onToggleDone = { tarea ->
                toggleEstadoTarea(tarea)
            }
        )
        recyclerView.adapter = toDoAdapter

        logoutButton.setOnClickListener {
            cerrarSesion()
        }

        val addTaskButton = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        obtenerTareas()
    }

    private fun cerrarSesion() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun obtenerTareas() {
        val user = auth.currentUser
        if (user == null) {
            irAlLogin("Sesión expirada. Inicia sesión nuevamente.")
            return
        }

        val nombreUsuario = user.displayName ?: user.email ?: "SinNombre"

        ApiClient.toDoApiService.getAllToDos().enqueue(object : Callback<List<ToDo>> {
            override fun onResponse(call: Call<List<ToDo>>, response: Response<List<ToDo>>) {
                if (response.isSuccessful) {
                    val todasLasTareas = response.body() ?: emptyList()
                    val tareasUsuario = todasLasTareas.filter { it.createdBy == nombreUsuario }
                    toDoAdapter.updateData(tareasUsuario)
                } else {
                    mostrarError("Error al obtener tareas: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ToDo>>, t: Throwable) {
                mostrarError("Fallo en la conexión: ${t.message}")
            }
        })
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        Log.e("HomeActivity", mensaje)
    }

    private fun irAlLogin(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun eliminarTarea(id: String) {
        ApiClient.toDoApiService.deleteToDo(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@HomeActivity, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                    obtenerTareas() // recargar
                } else {
                    mostrarError("Error al eliminar tarea")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                mostrarError("Fallo: ${t.message}")
            }
        })
    }

    private fun toggleEstadoTarea(tarea: ToDo) {
        val nuevaTarea = tarea.copy(done = !tarea.done)
        ApiClient.toDoApiService.updateToDo(tarea.id, nuevaTarea)
            .enqueue(object : Callback<ToDo> {
                override fun onResponse(call: Call<ToDo>, response: Response<ToDo>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@HomeActivity, "Tarea actualizada", Toast.LENGTH_SHORT).show()
                        obtenerTareas()
                    } else {
                        mostrarError("Error al actualizar estado")
                    }
                }

                override fun onFailure(call: Call<ToDo>, t: Throwable) {
                    mostrarError("Fallo: ${t.message}")
                }
            })
    }

}
