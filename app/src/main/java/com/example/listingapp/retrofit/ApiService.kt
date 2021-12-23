package com.example.listingapp.retrofit

import com.example.listingapp.other.Constants.BASE_URL_WEATHER
import com.example.listingapp.response.WeatherResponse
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("v2.0/current")
    suspend fun getWeather(
        @Query("lat") lat: Float?,
        @Query("lon") lon: Float?,
        @Query("key") key: String?
    ): Response<WeatherResponse?>

    companion object {

        fun createService(): ApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }

}