package com.android.personal.kilojouletracker.api

import retrofit2.http.GET

interface FatSecretApi
{
    @GET("/")
    suspend fun fetchContents(): String
}