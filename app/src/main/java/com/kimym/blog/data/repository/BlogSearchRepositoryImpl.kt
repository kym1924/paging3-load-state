package com.kimym.blog.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kimym.blog.data.api.BlogSearchService
import com.kimym.blog.data.entity.BlogSearchResult
import com.kimym.blog.data.paging.BlogSearchPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BlogSearchRepositoryImpl @Inject constructor(
    private val service: BlogSearchService
) : BlogSearchRepository {
    override fun getSearchResult(query: String): Flow<PagingData<BlogSearchResult.Blog>> {
        return Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { BlogSearchPagingSource(service, query) }
        ).flow
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 15
    }
}
