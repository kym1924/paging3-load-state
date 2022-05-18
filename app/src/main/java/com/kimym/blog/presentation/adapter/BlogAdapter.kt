package com.kimym.blog.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kimym.blog.data.entity.BlogSearchResult.Blog
import com.kimym.blog.databinding.ItemBlogBinding

class BlogAdapter : PagingDataAdapter<Blog, BlogAdapter.BlogViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBlogBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        getItem(position)?.let { blog ->
            holder.bind(blog)
        }
    }

    class BlogViewHolder(private val binding: ItemBlogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blog: Blog) {
            binding.blog = blog
            binding.executePendingBindings()
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Blog>() {
            override fun areItemsTheSame(oldItem: Blog, newItem: Blog): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: Blog, newItem: Blog): Boolean {
                return oldItem == newItem
            }
        }
    }
}
