package com.android.personal.kilojouletracker

import android.content.Context
import android.util.Log
import com.android.personal.kilojouletracker.api.NutritionixApi
import com.android.personal.kilojouletracker.model.Meal
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.HttpException

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
    suspend fun getMealFromAPI(queryString: String): Meal?
    {
        var meal: Meal? = null
        try
        {
            var foodRequestBody = NutritionixApi.formatQueryStringToJsonBody(queryString)
            var foodJson = nutritionixApi.getFoodData(foodRequestBody)
            Log.d("Response JSON", foodJson) //Just to see the contents
            val json = Json { ignoreUnknownKeys = false }
            var outerJsonObject = json.parseToJsonElement(foodJson).jsonObject
            var jsonArray = checkNotNull(outerJsonObject["foods"]?.jsonArray)
            val mealName = checkNotNull(jsonArray[0].jsonObject["food_name"]?.jsonPrimitive?.content) { "Failed to extract food_name from Json response" }
            val servingWeight = checkNotNull(jsonArray[0].jsonObject["serving_weight_grams"]?.jsonPrimitive?.double) { "Failed to extract serving_weight_grams from Json response" }
            val numKilojoule = caloriesToKilojoules(checkNotNull(jsonArray[0].jsonObject["nf_calories"]?.jsonPrimitive?.double) { "Failed to extract nf_calories from Json response" })
            val fatWeight = checkNotNull(jsonArray[0].jsonObject["nf_total_fat"]?.jsonPrimitive?.double) { "Failed to extract nf_total_fat from Json response" }
            val carbohydrateWeight = checkNotNull(jsonArray[0].jsonObject["nf_total_carbohydrate"]?.jsonPrimitive?.double) { "Failed to extract nf_total_carbohydrate from Json response" }
            val proteinWeight = checkNotNull(jsonArray[0].jsonObject["nf_protein"]?.jsonPrimitive?.double) { "Failed to extract nf_protein from Json response" }

            meal = Meal(mealName, servingWeight, numKilojoule, fatWeight, carbohydrateWeight, proteinWeight)
        }
        catch(e: HttpException)
        {
            Log.d("HTTP Exception", e.toString())
        }

        return meal
    }

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

        fun caloriesToKilojoules(calories: Double) = calories * 4.184
    }
}