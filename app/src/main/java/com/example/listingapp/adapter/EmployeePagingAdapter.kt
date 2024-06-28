package com.example.listingapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listingapp.R
import com.example.listingapp.databinding.ListItemBinding
import com.example.listingapp.listener.AdapterClickListeners
import com.example.listingapp.response.ModelResult
import com.example.listingapp.BR


class EmployeePagingAdapter(private val adapterClickListeners: AdapterClickListeners) :
    PagingDataAdapter<ModelResult, EmployeePagingAdapter.MyViewHolder>(DIFF_UTIL) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    inner class MyViewHolder(val viewDataBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root)

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val item = getItem(position)

        holder.viewDataBinding.setVariable(BR.result, item)

        if(item?.picture?.large!=null && item.picture.large.isNotEmpty())

        Glide.with(holder.viewDataBinding.root).load(item.picture.large)
            .into(holder.viewDataBinding.root.findViewById(R.id.imgSource))

        holder.viewDataBinding.root.setOnClickListener {
            if (item != null) {
                adapterClickListeners.onClickListeners(item)
            }
        }

    }

    companion object {

        val DIFF_UTIL = object : DiffUtil.ItemCallback<ModelResult>() {
            override fun areItemsTheSame(oldItem: ModelResult, newItem: ModelResult): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: ModelResult, newItem: ModelResult): Boolean {
                return oldItem.gender == newItem.gender
            }
        }
    }

}