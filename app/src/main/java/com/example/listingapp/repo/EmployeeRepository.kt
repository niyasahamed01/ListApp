package com.example.listingapp.repo


import com.example.listingapp.response.Name
import com.example.listingapp.room.EmployeeDao
import kotlinx.coroutines.flow.Flow

class EmployeeRepository(
    private val dao: EmployeeDao
) {

    fun searchDatabase(searchQuery: String): Flow<Name> {
        return dao.searchDatabase(searchQuery)
    }

}