package com.example.listingapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.listingapp.databinding.ListItemBinding
import com.example.listingapp.listener.AdapterClickListeners
import com.example.listingapp.response.ModelResult

class EmployeePagingAdapter(
    private val listener: AdapterClickListeners
) : PagingDataAdapter<ModelResult, EmployeePagingAdapter.MyViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ModelResult>() {
            override fun areItemsTheSame(oldItem: ModelResult, newItem: ModelResult): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ModelResult, newItem: ModelResult): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        try {
            val itemCount = itemCount // Cache the item count to avoid calling it multiple times
            if (position in 0 until itemCount) {
                val item = getItem(position)
                item?.let { holder.bind(it, listener) }
            } else {
                Log.e("EmployeePagingAdapter", "Attempted to bind item at invalid position $position. Item count: $itemCount")
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e("EmployeePagingAdapter", "IndexOutOfBoundsException at position $position: ${e.message}")
        }

    }


    inner class MyViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(employee: ModelResult, clickListener: AdapterClickListeners) {
            binding.result = employee
            binding.root.setOnClickListener {
                clickListener.onClickListeners(employee)
            }
            employee.picture?.large?.let { largeUrl ->
                if (largeUrl.isNotEmpty()) {
                    Glide.with(binding.root)
                        .load(largeUrl)
                        .into(binding.imgSource)
                }
            }
            binding.executePendingBindings()
        }
    }


}