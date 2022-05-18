package com.kimym.blog.presentation

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.kimym.blog.databinding.ActivityMainBinding
import com.kimym.blog.presentation.adapter.BlogAdapter
import com.kimym.blog.presentation.adapter.BlogLoadAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initAdapter()
        initEtInputTextListener()
    }

    private fun initAdapter() {
        val adapter = BlogAdapter()
        val adapterWithLoadState = adapter.withLoadStateHeaderAndFooter(
            header = BlogLoadAdapter { adapter.retry() },
            footer = BlogLoadAdapter { adapter.retry() }
        )
        initRvDocument(adapterWithLoadState)
        initListener(adapter)
        initFlowCollect(adapter)
    }

    private fun initRvDocument(adapterWithLoadState: ConcatAdapter) {
        with(binding.rvDocument) {
            adapter = adapterWithLoadState
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(this@MainActivity, RecyclerView.VERTICAL))
        }
    }

    private fun initListener(adapter: BlogAdapter) {
        binding.layoutSwipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        binding.btnRetry.setOnClickListener {
            adapter.retry()
        }
    }

    private fun initFlowCollect(adapter: BlogAdapter) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchResult.collectLatest {
                        adapter.submitData(it)
                    }
                }

                launch {
                    adapter.loadStateFlow
                        .distinctUntilChangedBy { it.refresh }
                        .filter { it.refresh is LoadState.NotLoading }
                        .collect {
                            binding.layoutSwipeRefresh.isRefreshing = false
                            binding.rvDocument.scrollToPosition(0)
                        }
                }

                launch {
                    adapter.loadStateFlow.collect { loadState ->
                        binding.loadState = loadState
                        binding.isNotLoadingAndListEmpty =
                            loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0
                        binding.executePendingBindings()
                    }
                }
            }
        }
    }

    private fun initEtInputTextListener() {
        binding.etInputText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    viewModel.search(binding.etInputText.text.toString())
                    hideKeyBoard()
                    true
                }
                else -> false
            }
        }
    }

    private fun hideKeyBoard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etInputText.windowToken, 0)
        binding.etInputText.clearFocus()
    }
}
