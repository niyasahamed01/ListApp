package com.example.listingapp.di

import android.content.Context
import com.example.listingapp.other.Constants.BASE_URL_LIST
import com.example.listingapp.preference.PreferenceManager
import com.example.listingapp.retrofit.*
import com.example.listingapp.room.EmployeeDao
import com.example.listingapp.room.EmployeeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DiModules {

    @Provides
    @Singleton
    fun providePreferenceManager(@ApplicationContext context: Context): PreferenceManager =
        PreferenceManager(context)

    @Singleton
    @Provides
    fun providesApiService(): ApiService = ApiService.createService()

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {

        // Create an OkHttpClient with logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder().baseUrl(BASE_URL_LIST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    @Singleton
    @Provides
    fun provideInterface(retrofit: Retrofit): EmployeeInterface {
        return retrofit.create(EmployeeInterface::class.java)
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): EmployeeDatabase {
        return EmployeeDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideDao(employeeDatabase: EmployeeDatabase): EmployeeDao {
        return employeeDatabase.getDao()
    }



}


