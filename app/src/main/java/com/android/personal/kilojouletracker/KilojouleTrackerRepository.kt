package com.android.personal.kilojouletracker

import android.util.Log
import com.android.personal.kilojouletracker.api.NutrionixApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import java.util.UUID

class KilojouleTrackerRepository
{
    private val nutrionixApi: NutrionixApi

    init
    {
        val okHttpClient = OkHttpClient().newBuilder().addInterceptor(RequestInterceptor).build()
        val retrofit: Retrofit = Retrofit.Builder().client(okHttpClient).baseUrl(BASE_URL).addConverterFactory(ScalarsConverterFactory.create()).addConverterFactory(GsonConverterFactory.create()).build()
        nutrionixApi = retrofit.create<NutrionixApi>()
    }

    //Network functions
    suspend fun fetchFood(food: String) = NutrionixApi.fetchFood(searchExpression = food)

    object RequestInterceptor: Interceptor
    {
        override fun intercept(chain: Interceptor.Chain): Response
        {
            val request = chain.request()
            Log.d("Outgoing HTTP Request", request.url.toString())
            return chain.proceed(request)
        }
    }

    object AuthorisationInterceptor: Interceptor
    {
        override fun intercept(chain: Interceptor.Chain): Response
        {
            val requestWithHeader = chain.request().newBuilder().addHeader("Content-Type", ).addHeader().build()
            return chain.proceed(requestWithHeader)
        }
    }

    companion object
    {
        const val BASE_URL = "https://trackapi.nutritionix.com/"
    }
}