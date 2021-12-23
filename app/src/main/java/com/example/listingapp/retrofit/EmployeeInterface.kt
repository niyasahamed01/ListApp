package com.example.listingapp.retrofit

import com.example.listingapp.response.EmployeesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface EmployeeInterface {

    @GET("api/")
    suspend fun getAllEmployee(
        @Query("page") page: Int,
        @Query("results") pageSize: Int
    ): Response<EmployeesResponse>

}