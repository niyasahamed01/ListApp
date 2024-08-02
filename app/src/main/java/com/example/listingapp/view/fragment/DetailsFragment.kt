package com.example.listingapp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.listingapp.databinding.FragmentDetailsBinding
import com.example.listingapp.response.ModelResult
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentDetailsBinding.inflate(inflater, container, false)

        val data = arguments?.get("result") as? ModelResult

        binding.result = data

        data?.picture?.large?.let { largeUrl ->
            if (largeUrl.isNotEmpty()) {
                Glide.with(binding.root)
                    .load(largeUrl)
                    .into(binding.detailsImage)
            }
        }

        return binding.root
    }

}