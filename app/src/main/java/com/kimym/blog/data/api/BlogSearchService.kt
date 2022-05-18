package com.kimym.blog.data.api

import com.kimym.blog.data.entity.BlogSearchResult
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BlogSearchService {
    @Headers("Authorization: KakaoAK ****")
    @GET("blog")
    suspend fun getSearchResult(
        @Query("query") query: String,
        @Query("sort") sort: String = "recency",
        @Query("page") page: Int,
        @Query("size") size: Int
    ): BlogSearchResult
}
