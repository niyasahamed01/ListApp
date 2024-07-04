package com.example.listingapp.repo


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.listingapp.response.ModelResult
import com.example.listingapp.response.Name
import com.example.listingapp.room.EmployeeDao
import com.example.listingapp.room.EmployeeDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EmployeeRepository @Inject constructor(
    private val dao: EmployeeDao,
) {



}