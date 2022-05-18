package com.kimym.blog.di

import com.kimym.blog.data.api.BlogSearchService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideBlogSearchService(): BlogSearchService {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BASIC

        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://dapi.kakao.com/v2/search/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(BlogSearchService::class.java)
    }
}
