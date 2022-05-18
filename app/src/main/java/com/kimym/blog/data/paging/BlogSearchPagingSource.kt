package com.kimym.blog.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kimym.blog.data.api.BlogSearchService
import com.kimym.blog.data.entity.BlogSearchResult
import com.kimym.blog.data.repository.BlogSearchRepositoryImpl.Companion.NETWORK_PAGE_SIZE

class BlogSearchPagingSource(
    private val service: BlogSearchService,
    private val query: String
) : PagingSource<Int, BlogSearchResult.Blog>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BlogSearchResult.Blog> {
        return try {
            val page = params.key ?: 1
            val response =
                service.getSearchResult(query = query, page = page, size = params.loadSize)
            LoadResult.Page(
                data = response.documents,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.documents.isEmpty()) null else page + (params.loadSize / NETWORK_PAGE_SIZE),
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, BlogSearchResult.Blog>): Int {
        return 1
    }
}
