package com.example.listingapp.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.listingapp.response.ModelResult
import com.example.listingapp.retrofit.EmployeeInterface
import com.example.listingapp.room.ArticleRemoteKey
import com.example.listingapp.room.EmployeeDao


@OptIn(ExperimentalPagingApi::class)
class EmployeeRemoteMediator(
    private val employeeDao: EmployeeDao,
    private val employeeInterface: EmployeeInterface
) : RemoteMediator<Int, ModelResult>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ModelResult>
    ): MediatorResult {
        try {
            val page = when (loadType) {
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    val remoteKey = lastItem?.let { employeeDao.getRemoteKeyByModelResult(it.id.toString()) }
                    remoteKey?.next ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                }
                LoadType.PREPEND -> {
                    val firstItem = state.firstItemOrNull()
                    val remoteKey = firstItem?.let { employeeDao.getRemoteKeyByModelResult(it.id.toString()) }
                    remoteKey?.prev ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                }
                LoadType.REFRESH -> 1
            }

            val response = employeeInterface.getAllEmployee(page, state.config.pageSize)
            if (response.isSuccessful) {
                val employees = response.body()?.results ?: emptyList()
                val endOfPaginationReached = employees.isEmpty()

                if (loadType == LoadType.REFRESH) {
                    employeeDao.deleteAllEmployees()
                    employeeDao.deleteAllRemoteKeys()
                }

                employeeDao.insertEmployees(employees)

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = employees.map {
                    ArticleRemoteKey(it.id.toString(), prevKey, nextKey)
                }
                employeeDao.insertAllRemoteKeys(keys)

                return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
            } else {
                return MediatorResult.Error(Exception("Network request failed"))
            }
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

}