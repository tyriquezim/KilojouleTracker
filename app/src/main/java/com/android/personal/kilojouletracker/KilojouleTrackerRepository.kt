package com.android.personal.kilojouletracker

import android.content.Context
import android.util.Log
import com.android.personal.kilojouletracker.api.NutritionixApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create

class KilojouleTrackerRepository private  constructor(context: Context)
{
    private val nutritionixApi: NutritionixApi

    init
    {
        val okHttpClient = OkHttpClient().newBuilder().addInterceptor(AuthorisationInterceptor).build()
        val retrofit: Retrofit = Retrofit.Builder().client(okHttpClient).baseUrl(BASE_URL).addConverterFactory(ScalarsConverterFactory.create()).build()
        nutritionixApi = retrofit.create<NutritionixApi>()
    }

    //Network functions
    suspend fun fetchFood(foodName: String) = nutritionixApi.fetchFood("{\"query\":\"$foodName\"}".toRequestBody())

    private object AuthorisationInterceptor: Interceptor
    {
        override fun intercept(chain: Interceptor.Chain): Response
        {
            val requestWithHeader = chain.request().newBuilder().addHeader("Content-Type", NutritionixApi.CONTENT_TYPE).addHeader("x-app-id", NutritionixApi.APP_ID).addHeader("x-app-key", NutritionixApi.API_KEY).build()
            Log.d("Outgoing HTTP Request", requestWithHeader.url.toString())
            return chain.proceed(requestWithHeader)
        }
    }

    companion object
    {
        private const val BASE_URL = "https://trackapi.nutritionix.com/"

        private var repositoryInstance: KilojouleTrackerRepository? = null

        fun initialise(context: Context)
        {
            if(repositoryInstance == null)
            {
                repositoryInstance = KilojouleTrackerRepository(context)
            }
        }

        fun get(): KilojouleTrackerRepository
        {
            return repositoryInstance ?: throw IllegalStateException("The KilojouleTrackerRepository has not been initialised")
        }
    }
}