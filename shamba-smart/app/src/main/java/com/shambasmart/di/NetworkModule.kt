package com.shambasmart.di

import com.shambasmart.data.remote.ApiClient
import com.shambasmart.data.remote.ApiService
import com.shambasmart.data.remote.WeatherApiClient
import com.shambasmart.data.remote.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiClient(): ApiClient = ApiClient()

    @Provides
    @Singleton
    fun provideApiService(apiClient: ApiClient): ApiService = apiClient.apiService

    @Provides
    @Singleton
    fun provideWeatherApiClient(): WeatherApiClient = WeatherApiClient()

    @Provides
    @Singleton
    fun provideWeatherApiService(weatherApiClient: WeatherApiClient): WeatherApiService = 
        weatherApiClient.weatherApiService
}