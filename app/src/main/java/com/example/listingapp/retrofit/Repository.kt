package com.example.listingapp.retrofit

import com.example.listingapp.response.WeatherResponse
import com.example.listingapp.util.BaseApiResponse
import com.example.listingapp.util.NetworkResult
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ActivityRetainedScoped
class Repository @Inject constructor(
    private val apiHelperImpl: ApiHelperImpl
) : BaseApiResponse() {

    suspend fun getWeather(
        lat: Double?,
        long: Double?,
        key: String?
    ): Flow<NetworkResult<WeatherResponse?>> {
        return flow<NetworkResult<WeatherResponse?>> {
            emit(safeApiCall { apiHelperImpl.getWeather(lat, long, key) })
        }.flowOn(Dispatchers.IO)
    }

}
