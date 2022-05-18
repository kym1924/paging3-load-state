package com.kimym.blog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kimym.blog.data.entity.BlogSearchResult.Blog
import com.kimym.blog.data.repository.BlogSearchRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: BlogSearchRepositoryImpl
) : ViewModel() {
    private val query = MutableStateFlow<String?>(null)

    @ExperimentalCoroutinesApi
    val searchResult: Flow<PagingData<Blog>> = query.filterNotNull().flatMapLatest { query ->
        repository.getSearchResult(query).cachedIn(viewModelScope)
    }

    fun search(input: String) {
        if (input.isBlank() || input == query.value) {
            return
        }
        query.value = input
    }
}
