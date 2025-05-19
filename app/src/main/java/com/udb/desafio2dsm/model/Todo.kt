package com.udb.desafio2dsm.model

data class ToDo(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val done: Boolean = false,
    val createdAt: String = "",
    val createdBy: String = ""
)
