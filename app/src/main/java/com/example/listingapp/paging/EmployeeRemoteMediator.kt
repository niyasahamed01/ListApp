package com.example.listingapp.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.listingapp.response.ModelResult
import com.example.listingapp.retrofit.EmployeeInterface

import com.example.listingapp.room.ArticleRemoteKey
import com.example.listingapp.room.EmployeeDao
import java.io.InvalidObjectException

//
//@ExperimentalPagingApi
//class EmployeeRemoteMediator(
//    private val employeeDao: EmployeeDao,
//    private val employeeInterface: EmployeeInterface,
//    private val initialPage: Int = 1
//) : RemoteMediator<Int, ModelResult>() {
//
//    override suspend fun load(
//        loadType: LoadType,
//        state: PagingState<Int, ModelResult>
//    ): MediatorResult {
//
//        return try {
//            // Determine the page to load
//            val page = when (loadType) {
//                LoadType.APPEND -> {
//                    val lastItem = state.lastItemOrNull()
//                    val nextPage = lastItem?.let {
//                        employeeDao.getRemoteKeyByModelResult(it.gender)?.next
//                    } ?: 1
//
//                    nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
//                }
//                LoadType.PREPEND -> {
//                    return MediatorResult.Success(endOfPaginationReached = true)
//                }
//                LoadType.REFRESH -> {
//                    val remoteKey = getClosestRemoteKeys(state)
//                    remoteKey?.next?.minus(1) ?: initialPage
//                }
//            }
//
//            // Make network request
//            val response = employeeInterface.getAllEmployee(
//                page,
//                state.config.pageSize
//            )
//            val endOfPagination = response.body()?.results?.size!! < state.config.pageSize
//
//            if (response.isSuccessful) {
//                response.body()?.let {
//                    if (loadType == LoadType.REFRESH) {
//                        employeeDao.deleteAllEmployees()
//                        employeeDao.deleteAllRemoteKeys()
//                    }
//                    val prev = if (page == initialPage) null else page - 1
//                    val next = if (endOfPagination) null else page + 1
//
//                    val list = response.body()?.results?.map {
//                        ArticleRemoteKey(it.gender, prev, next)
//                    }
//
//                    if (list != null) {
//                        employeeDao.insertAllRemoteKeys(list)
//                    }
//                    employeeDao.insertEmployees(it.results)
//                }
//                MediatorResult.Success(endOfPagination)
//            } else {
//                MediatorResult.Success(endOfPaginationReached = true)
//            }
//
//        } catch (e: Exception) {
//            MediatorResult.Error(e)
//        }
//
//    }
//
//    private suspend fun getClosestRemoteKeys(state: PagingState<Int, ModelResult>): ArticleRemoteKey? {
//        return state.anchorPosition?.let { position ->
//            state.closestItemToPosition(position)?.let { modelResult ->
//                employeeDao.getAllRemoteKey(modelResult.gender)
//            }
//        }
//    }
//
//}


@ExperimentalPagingApi
class EmployeeRemoteMediator(
    private val employeeDao: EmployeeDao,
    private val employeeInterface: EmployeeInterface,
    private val initialPage: Int = 1
) : RemoteMediator<Int, ModelResult>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ModelResult>
    ): MediatorResult {
        return try {
            // Determine the page to load
            val page = when (loadType) {
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    val nextPage = lastItem?.let {
                        val remoteKey = employeeDao.getRemoteKeyByModelResult(it.gender)
                        remoteKey?.next ?: 1
                    } ?: 1

                    nextPage
                }
                LoadType.PREPEND -> {
                    // Not used in this example; can be implemented based on use case
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.REFRESH -> {
                    // Reset everything on refresh, start from the initial page
                    employeeDao.deleteAllEmployees()
                    employeeDao.deleteAllRemoteKeys()
                    initialPage
                }
            }

            // Make network request
            val response = employeeInterface.getAllEmployee(page, state.config.pageSize)
            if (response.isSuccessful) {
                val employees = response.body()?.results ?: emptyList()
                val endOfPaginationReached = employees.isEmpty()

                // Insert data into local database
                employeeDao.insertEmployees(employees)

                // Save remote keys to know the next/prev keys
                if (loadType == LoadType.REFRESH && !endOfPaginationReached) {
                    val prevKey = if (page == initialPage) null else page - 1
                    val nextKey = if (endOfPaginationReached) null else page + 1
                    val keys = employees.map {
                        ArticleRemoteKey(it.gender, prevKey, nextKey)
                    }
                    employeeDao.insertAllRemoteKeys(keys)
                }

                MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            } else {
                MediatorResult.Error(Exception("Network request failed"))
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}