package com.example.listingapp.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.listingapp.response.ModelResult

@Dao
interface EmployeeDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertEmployees(list: List<ModelResult>)

        @Query("SELECT * FROM ModelResult ")
        fun getAllEmployees(): PagingSource<Int, ModelResult>

        @Query("DELETE FROM ModelResult")
        suspend fun deleteAllEmployees()

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAllRemoteKeys(list: List<ArticleRemoteKey>)

        @Query("SELECT * FROM ArticleRemoteKey WHERE id = :id")
        suspend fun getRemoteKeyByModelResult(id: String): ArticleRemoteKey?

        @Query("DELETE FROM ArticleRemoteKey")
        suspend fun deleteAllRemoteKeys()

        @Query("SELECT * FROM ModelResult WHERE name LIKE '%' || :searchQuery || '%'")
        fun searchEmployees(searchQuery: String): PagingSource<Int, ModelResult>

}