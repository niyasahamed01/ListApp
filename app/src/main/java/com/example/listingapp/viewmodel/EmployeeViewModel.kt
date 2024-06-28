package com.example.listingapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.listingapp.other.Constants.KEY
import com.example.listingapp.paging.EmployeeRemoteMediator
import com.example.listingapp.repo.EmployeeRepository
import com.example.listingapp.response.Name
import com.example.listingapp.response.WeatherResponse
import com.example.listingapp.retrofit.EmployeeInterface
import com.example.listingapp.retrofit.Repository
import com.example.listingapp.room.EmployeeDao
import com.example.listingapp.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NETWORK_PAGE_SIZE = 25

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val employeeInterface: EmployeeInterface,
    private val repository: Repository
) : ViewModel() {

    @ExperimentalPagingApi
    val paging = Pager(
        PagingConfig(pageSize = NETWORK_PAGE_SIZE),
        remoteMediator = EmployeeRemoteMediator(employeeDao, employeeInterface, 1)
    ) {
        employeeDao.getAllEmployees()
    }.flow



    private val _response: MutableLiveData<NetworkResult<WeatherResponse?>> = MutableLiveData()
    val response: LiveData<NetworkResult<WeatherResponse?>> = _response


    fun getWeather(lat: Double, long: Double) = viewModelScope.launch {
        repository.getWeather(
            lat = lat,
            long = long,
            key = KEY
        ).collect { values ->
            _response.value = values
        }
    }


//    suspend fun function1(): String {
//        delay(1000L)
//        val message = "function1"
//        Log.i("Launch", message)
//        return message
//    }
//
//    fun name1() {
//
//        var resultOne = "Android"
//        var resultTwo = "Kotlin"
//        Log.i("Launch", "Before")
//        viewModelScope.launch(Dispatchers.IO) {
//            resultOne = function1()
//        }
//        viewModelScope.launch(Dispatchers.IO) {
//            resultTwo = function2()
//        }
//        Log.i("Launch", "After")
//        val resultText = resultOne + resultTwo
//        Log.i("Launch", resultText)
//    }
//
//    suspend fun function2(): String {
//        delay(100L)
//        val message = "function2"
//        Log.i("Launch", message)
//        return message
//    }
//
//    fun name() {
//        viewModelScope.launch {
//            delay(1000)
//            "niyas"
//        }
//    }


}