package com.mct.mediapicker.adapter;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

abstract class BindingViewHolder<B extends ViewDataBinding> extends RecyclerView.ViewHolder {

    protected final B binding;

    protected BindingViewHolder(@NonNull B binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
