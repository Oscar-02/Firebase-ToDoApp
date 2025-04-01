package com.udb.desafio2dsm

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var tareaAdapter: TareaAdapter
    private lateinit var recyclerView: RecyclerView
    private val listaTareas = mutableListOf<Tarea>()
    private var ultimoTituloEscrito = ""
    private var ultimaDescripcionEscrita = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            cerrarSesion()
        }

        val prefs = getSharedPreferences("TareaDraft", MODE_PRIVATE)
        ultimoTituloEscrito = prefs.getString("titulo", "") ?: ""
        ultimaDescripcionEscrita = prefs.getString("descripcion", "") ?: ""

// Limpiar SharedPreferences una vez que se restaura
        prefs.edit().clear().apply()

        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.tareasRecyclerView)

        tareaAdapter = TareaAdapter(listaTareas) { tarea ->
            mostrarDialogoEditarTarea(tarea)
        }

        recyclerView.adapter = tareaAdapter

        val uid = auth.currentUser?.uid
        if (uid != null) {
            databaseRef = FirebaseDatabase.getInstance().getReference("Tareas").child(uid)
            cargarTareasDesdeFirebase()
        } else {
            irAlLogin("Sesión expirada")
        }

        // Botón para agregar tarea
        val addButton = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.addTaskButton)
        addButton.setOnClickListener {
            mostrarDialogoAgregarTarea()
        }
    }

    private fun cargarTareasDesdeFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaTareas.clear()
                for (tareaSnapshot in snapshot.children) {
                    val tarea = tareaSnapshot.getValue(Tarea::class.java)
                    if (tarea != null) {
                        tarea.id = tareaSnapshot.key ?: ""
                        listaTareas.add(tarea)
                    }
                }
                tareaAdapter.actualizarLista(listaTareas)
            }

            override fun onCancelled(error: DatabaseError) {
                irAlLogin("Error de conexión: ${error.message}")
            }
        })
    }

    private fun irAlLogin(mensaje: String) {
        // Guardar datos si hay algo en el formulario
        val prefs = getSharedPreferences("TareaDraft", MODE_PRIVATE)
        val editor = prefs.edit()

        // Supongamos que tenés acceso a los campos del diálogo (si está abierto)
        // O podrías pasar los valores directamente a esta función
        editor.putString("titulo", ultimoTituloEscrito)
        editor.putString("descripcion", ultimaDescripcionEscrita)
        editor.apply()

        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun mostrarDialogoAgregarTarea() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_tarea, null)
        val inputTitulo = dialogView.findViewById<EditText>(R.id.inputTitulo)
        val inputDescripcion = dialogView.findViewById<EditText>(R.id.inputDescripcion)

        inputTitulo.setText(ultimoTituloEscrito)
        inputDescripcion.setText(ultimaDescripcionEscrita)

        inputTitulo.addTextChangedListener {
            ultimoTituloEscrito = it.toString()
        }

        inputDescripcion.addTextChangedListener {
            ultimaDescripcionEscrita = it.toString()
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Nueva Tarea")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = inputTitulo.text.toString().trim()
                val descripcion = inputDescripcion.text.toString().trim()

                if (titulo.isNotEmpty()) {
                    guardarTareaEnFirebase(titulo, descripcion)
                } else {
                    Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun guardarTareaEnFirebase(titulo: String, descripcion: String) {
        val nuevaId = databaseRef.push().key
        if (nuevaId != null) {
            val tarea = Tarea(
                id = nuevaId,
                titulo = titulo,
                descripcion = descripcion,
                timestamp = System.currentTimeMillis()
            )
            databaseRef.child(nuevaId).setValue(tarea)
                .addOnSuccessListener {
                    Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mostrarDialogoEditarTarea(tarea: Tarea) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_tarea, null)
        val inputTitulo = dialogView.findViewById<EditText>(R.id.inputTitulo)
        val inputDescripcion = dialogView.findViewById<EditText>(R.id.inputDescripcion)

        inputTitulo.addTextChangedListener {
            ultimoTituloEscrito = it.toString()
        }

        inputDescripcion.addTextChangedListener {
            ultimaDescripcionEscrita = it.toString()
        }

        // Cargar datos existentes
        inputTitulo.setText(tarea.titulo)
        inputDescripcion.setText(tarea.descripcion)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Editar Tarea")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoTitulo = inputTitulo.text.toString().trim()
                val nuevaDescripcion = inputDescripcion.text.toString().trim()

                if (nuevoTitulo.isNotEmpty()) {
                    val tareaActualizada = tarea.copy(
                        titulo = nuevoTitulo,
                        descripcion = nuevaDescripcion
                    )
                    actualizarTareaEnFirebase(tareaActualizada)
                } else {
                    Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Eliminar") { _, _ ->
                eliminarTareaDeFirebase(tarea)
            }
            .create()

        dialog.show()
    }

    private fun actualizarTareaEnFirebase(tarea: Tarea) {
        databaseRef.child(tarea.id).setValue(tarea)
            .addOnSuccessListener {
                Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarTareaDeFirebase(tarea: Tarea) {
        databaseRef.child(tarea.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cerrarSesion() {
        // Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut()

        // Limpiar datos temporales
        getSharedPreferences("TareaDraft", MODE_PRIVATE).edit().clear().apply()

        // Ir al Login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

}