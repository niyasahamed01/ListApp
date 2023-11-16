package com.example.listingapp.viewmodel

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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NETWORK_PAGE_SIZE = 25

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val newsDao: EmployeeDao,
    private val newsInterface: EmployeeInterface,
    private val repository: Repository
) : ViewModel() {

    @ExperimentalPagingApi
    val paging = Pager(
        PagingConfig(pageSize = NETWORK_PAGE_SIZE),
        remoteMediator = EmployeeRemoteMediator(newsDao, newsInterface, 1)
    ) {
        newsDao.getAllEmployees()
    }.flow


    fun searchDatabase(searchQuery: String): LiveData<Name> {
        return employeeRepository.searchDatabase(searchQuery).asLiveData()
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

}