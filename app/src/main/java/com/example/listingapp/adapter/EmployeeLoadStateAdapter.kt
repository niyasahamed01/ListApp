package com.example.listingapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.listingapp.databinding.ItemEmployeeLoadStateBinding

class EmployeeLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<EmployeeLoadStateAdapter.LoadStateViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
            val binding = ItemEmployeeLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return LoadStateViewHolder(binding, retry)
        }

        override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
            holder.bind(loadState)
        }

        class LoadStateViewHolder(private val binding: ItemEmployeeLoadStateBinding, retry: () -> Unit) : RecyclerView.ViewHolder(binding.root) {

            init {
                binding.retryButton.setOnClickListener { retry.invoke() }
            }

            fun bind(loadState: LoadState) {
                if (loadState is LoadState.Error) {
                    binding.errorMsg.text = loadState.error.localizedMessage
                }
                binding.progressBar.isVisible = loadState is LoadState.Loading
                binding.retryButton.isVisible = loadState !is LoadState.Loading
                binding.errorMsg.isVisible = loadState !is LoadState.Loading
            }
        }
    }