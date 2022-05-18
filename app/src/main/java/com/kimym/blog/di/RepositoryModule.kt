package com.kimym.blog.di

import com.kimym.blog.data.api.BlogSearchService
import com.kimym.blog.data.repository.BlogSearchRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    @Provides
    @ViewModelScoped
    fun provideSearchRepository(searchService: BlogSearchService): BlogSearchRepositoryImpl {
        return BlogSearchRepositoryImpl(searchService)
    }
}
