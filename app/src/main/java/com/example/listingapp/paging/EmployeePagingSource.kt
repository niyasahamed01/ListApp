/*
package com.example.listingapp.paging

import androidx.paging.PagingSource
import com.example.listingapp.response.ModelResult
import com.example.listingapp.retrofit.EmployeeInterface
import retrofit2.HttpException
import java.io.IOException

const val STARTING_INDEX = 1

class EmployeePagingSource(private val newsInterface: EmployeeInterface) : PagingSource<Int, ModelResult>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ModelResult> {

        val position = params.key ?: STARTING_INDEX

        return try {
            val data = newsInterface.getAllEmployee(
                position,
                params.loadSize
            )
            LoadResult.Page(
                data = data.results,
                prevKey = if (params.key == STARTING_INDEX) null else position - 1,
                nextKey = if (data.results.isEmpty()) null else position + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }

    }
}*/
