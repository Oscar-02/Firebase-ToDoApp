package com.udb.desafio2dsm.api

import com.udb.desafio2dsm.model.ToDo

import retrofit2.Call
import retrofit2.http.*

interface ToDoApiService {

    @GET("to-do")
    fun getAllToDos(): Call<List<ToDo>>

    @POST("to-do")
    fun createToDo(@Body toDo: ToDo): Call<ToDo>

    @PUT("to-do/{id}")
    fun updateToDo(@Path("id") id: String, @Body toDo: ToDo): Call<ToDo>

    @DELETE("to-do/{id}")
    fun deleteToDo(@Path("id") id: String): Call<Void>
}