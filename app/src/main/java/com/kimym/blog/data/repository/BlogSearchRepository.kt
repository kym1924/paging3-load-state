package com.kimym.blog.data.repository

import androidx.paging.PagingData
import com.kimym.blog.data.entity.BlogSearchResult
import kotlinx.coroutines.flow.Flow

interface BlogSearchRepository {
    fun getSearchResult(query: String): Flow<PagingData<BlogSearchResult.Blog>>
}
