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

        val data = requireArguments()["result"] as ModelResult

        binding.result = data

        if (data.picture?.large != null && data.picture.large.isNotEmpty())

            Glide.with(binding.root).load(data.picture.large).into(binding.detailsImage)

        return binding.root
    }

}