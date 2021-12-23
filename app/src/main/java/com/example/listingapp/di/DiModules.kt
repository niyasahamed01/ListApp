package com.example.listingapp.di

import android.content.Context
import com.example.listingapp.other.Constants.BASE_URL_LIST
import com.example.listingapp.other.Constants.BASE_URL_WEATHER
import com.example.listingapp.preference.PreferenceManager
import com.example.listingapp.repo.EmployeeRepository
import com.example.listingapp.retrofit.*

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

import com.example.listingapp.room.EmployeeDao
import com.example.listingapp.room.EmployeeDatabase
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
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
        return Retrofit.Builder().baseUrl(BASE_URL_LIST)
            .client(getOkHttpClient().build())
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    private fun getOkHttpClient(): OkHttpClient.Builder {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client: OkHttpClient.Builder = OkHttpClient.Builder()
        client.readTimeout(60, TimeUnit.SECONDS)
        client.writeTimeout(60, TimeUnit.SECONDS)
        client.connectTimeout(60, TimeUnit.SECONDS)
        client.addInterceptor(interceptor)
        client.addInterceptor(Interceptor { chain ->
            val request = chain.request()
            chain.proceed(request)
        })

        client.addInterceptor { chain ->
            val builder = chain.request().newBuilder()
            chain.proceed(builder.build())
        }

        return client
    }

    @Singleton
    @Provides
    fun provideInterface(retrofit: Retrofit): EmployeeInterface {
        return retrofit.create(EmployeeInterface::class.java)
    }

    @Singleton
    @Provides
    fun provideRepository(
        employeeDao: EmployeeDao
    ): EmployeeRepository {
        return EmployeeRepository(employeeDao)
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


