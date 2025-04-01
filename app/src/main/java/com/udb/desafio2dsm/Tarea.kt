package com.udb.desafio2dsm

data class Tarea(
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var timestamp: Long = System.currentTimeMillis()
)