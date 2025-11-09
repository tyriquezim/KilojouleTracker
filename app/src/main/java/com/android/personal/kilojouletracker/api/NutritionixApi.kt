package com.android.personal.kilojouletracker.api

import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NutritionixApi
{
    @POST("/v2/natural/nutrients")
    suspend fun getFoodData(@Body foodQuery: RequestBody): String

    companion object
    {
        const val CONTENT_TYPE = "application/json"
        const val APP_ID = "d2680f3a"
        const val API_KEY = "d82204750cb5892e81d926b6f54bc605"

        fun formatQueryStringToJsonBody(query: String) = "{\"query\":\"$query\"}".toRequestBody()

        fun formatNameAndWeightForNLP(foodName: String, servingWeight: Double) = "$servingWeight grams of $foodName"
    }
}