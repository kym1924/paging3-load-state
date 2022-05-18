package com.kimym.blog.binding

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout

@BindingAdapter("visibleOrNot")
fun View.visibleOrNot(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("setImage")
fun ImageView.setImage(url: String) {
    if (url.isBlank()) {
        visibility = View.GONE
        return
    }
    Glide.with(context)
        .load(url)
        .into(this)
}

@BindingAdapter("startShimmer")
fun ShimmerFrameLayout.startShimmer(isLoading: Boolean) {
    if (isLoading) startShimmer() else stopShimmer()
}
