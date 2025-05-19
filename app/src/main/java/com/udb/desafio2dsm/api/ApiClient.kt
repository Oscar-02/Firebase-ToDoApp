package com.udb.desafio2dsm.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://68163b7232debfe95dbdd500.mockapi.io/academic/v1/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val toDoApiService: ToDoApiService = retrofit.create(ToDoApiService::class.java)
}