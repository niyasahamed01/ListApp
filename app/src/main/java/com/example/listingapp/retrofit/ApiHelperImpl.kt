package com.example.listingapp.retrofit

import com.example.listingapp.response.WeatherResponse
import retrofit2.Response
import javax.inject.Inject


class ApiHelperImpl @Inject constructor(private val apiService: ApiService) {

    suspend fun getWeather(
        lat: Float?,
        long: Float?,
        key: String?
    ): Response<WeatherResponse?> = apiService.getWeather(lat, long, key)


}
