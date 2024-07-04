package com.example.listingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.listingapp.other.Constants.KEY
import com.example.listingapp.paging.EmployeeRemoteMediator
import com.example.listingapp.response.ModelResult
import com.example.listingapp.response.WeatherResponse
import com.example.listingapp.retrofit.EmployeeInterface
import com.example.listingapp.retrofit.Repository
import com.example.listingapp.room.EmployeeDao
import com.example.listingapp.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NETWORK_PAGE_SIZE = 25

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val employeeInterface: EmployeeInterface,
    private val repository: Repository,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    // Flow of PagingData that emits whenever the searchQuery or data changes
    @OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    val pagingData: Flow<PagingData<ModelResult>> = searchQuery.flatMapLatest { query ->
        Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false // Ensure placeholders are disabled for accurate page handling
            ),
            remoteMediator = EmployeeRemoteMediator(
                employeeDao,
                employeeInterface
            )
        ) {
            if (query.isBlank()) {
                employeeDao.getAllEmployees()
            } else {
                employeeDao.searchEmployees("%$query%")
            }
        }.flow.cachedIn(viewModelScope)
    }

    // Function to set search query and trigger data refresh
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }


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