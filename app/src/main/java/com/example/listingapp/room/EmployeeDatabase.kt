package com.example.listingapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.listingapp.paging.RoomTypeConvertor
import com.example.listingapp.response.ModelResult
import com.example.listingapp.response.Name

@Database(entities = [ModelResult::class, ArticleRemoteKey::class, Name::class], version = 1, exportSchema = false)
@TypeConverters(RoomTypeConvertor::class)
abstract class EmployeeDatabase : RoomDatabase() {

    companion object {
        fun getInstance(context: Context): EmployeeDatabase {
            return Room.databaseBuilder(context, EmployeeDatabase::class.java, "MyDatabase")
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun getDao(): EmployeeDao

}