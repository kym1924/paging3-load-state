package com.kimym.blog.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kimym.blog.databinding.ItemLoadStateBinding

class BlogLoadAdapter(private val retry: View.OnClickListener) :
    LoadStateAdapter<BlogLoadAdapter.BlogLoadViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): BlogLoadViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLoadStateBinding.inflate(inflater, parent, false)
        return BlogLoadViewHolder(binding, retry)
    }

    override fun onBindViewHolder(holder: BlogLoadViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class BlogLoadViewHolder(
        private val binding: ItemLoadStateBinding,
        private val retry: View.OnClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(loadState: LoadState) {
            binding.loadState = loadState
            binding.retry = retry
            binding.executePendingBindings()
        }
    }
}
