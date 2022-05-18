package com.kimym.blog.data.entity

import com.google.gson.annotations.SerializedName

data class BlogSearchResult(
    @SerializedName("documents")
    val documents: List<Blog>,
) {
    data class Blog(
        @SerializedName("title")
        val title: String,
        @SerializedName("contents")
        val contents: String,
        @SerializedName("blogname")
        val blogName: String,
        @SerializedName("thumbnail")
        val thumbnail: String,
        @SerializedName("datetime")
        val dateTime: String
    ) {
        fun splitDate(): String {
            return dateTime.split("T")[0]
        }
    }
}
