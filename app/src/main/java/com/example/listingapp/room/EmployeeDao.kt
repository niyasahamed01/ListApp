package com.example.listingapp.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.listingapp.response.ModelResult
import com.example.listingapp.response.Name

import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(list: List<ModelResult>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticleSingle(article: ModelResult)

    @Query("SELECT * FROM ModelResult")
    fun getAllEmployees(): PagingSource<Int, ModelResult>

    @Query("DELETE FROM ModelResult")
    suspend fun deleteAllEmployees()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRemoteKeys(list: List<ArticleRemoteKey>)

    @Query("SELECT * FROM ArticleRemoteKey WHERE id = :id")
    suspend fun getAllREmoteKey(id: String): ArticleRemoteKey?

    @Query("DELETE FROM ArticleRemoteKey")
    suspend fun deleteAllRemoteKeys()

    @Query("SELECT * FROM name WHERE first LIKE :searchQuery OR last LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): Flow<Name>


}