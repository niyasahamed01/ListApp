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
            // Judging the page number
            val page = when (loadType) {
                LoadType.APPEND -> {
                    val remoteKey =
                        getLastRemoteKey(state) ?: throw InvalidObjectException("Invalid")
                    remoteKey.next ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                LoadType.REFRESH -> {
                    val remoteKey = getClosestRemoteKeys(state)
                    remoteKey?.next?.minus(1) ?: initialPage
                }
            }

            // make network request
            val response = employeeInterface.getAllEmployee(
                page,
                state.config.pageSize
            )
            val endOfPagination = response.body()?.results?.size!! < state.config.pageSize

            if (response.isSuccessful) {
                response.body()?.let {
                    // flush our data
                    if (loadType == LoadType.REFRESH) {
                        employeeDao.deleteAllEmployees()
                        employeeDao.deleteAllRemoteKeys()
                    }
                    val prev = if (page == initialPage) null else page - 1
                    val next = if (endOfPagination) null else page + 1

                    val list = response.body()?.results?.map {
                        ArticleRemoteKey(it.gender, prev, next)
                    }

                    // make list of remote keys
                    if (list != null) {
                        employeeDao.insertAllRemoteKeys(list)
                    }
                    // insert to the room
                    employeeDao.insertEmployees(it.results)

                }
                MediatorResult.Success(endOfPagination)
            } else {
                MediatorResult.Success(endOfPaginationReached = true)
            }

        } catch (e: Exception) {
            MediatorResult.Error(e)
        }

    }

    private suspend fun getClosestRemoteKeys(state: PagingState<Int, ModelResult>): ArticleRemoteKey? {
        return state.anchorPosition?.let {
            state.closestItemToPosition(it)?.let {
                employeeDao.getAllREmoteKey(it.gender)
            }
        }

    }

    private suspend fun getLastRemoteKey(state: PagingState<Int, ModelResult>): ArticleRemoteKey? {
        return state.lastItemOrNull()?.let {
            employeeDao.getAllREmoteKey(it.gender)
        }
    }

}