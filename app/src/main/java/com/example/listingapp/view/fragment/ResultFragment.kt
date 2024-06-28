package com.example.listingapp.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.filter
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.listingapp.R
import com.example.listingapp.response.ModelResult
import dagger.hilt.android.AndroidEntryPoint
import com.example.listingapp.listener.AdapterClickListeners
import com.example.listingapp.adapter.EmployeePagingAdapter
import com.example.listingapp.databinding.FragmentResultBinding
import com.example.listingapp.viewmodel.EmployeeViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ResultFragment : Fragment(), AdapterClickListeners {

    private lateinit var binding: FragmentResultBinding

    private val viewModel by viewModels<EmployeeViewModel>()

    private val employeePagingAdapter = EmployeePagingAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_result,
            container,
            false
        )
        return binding.root

    }

    @ExperimentalPagingApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initStartView()
        setObserver()
        searchData()
    }

    private fun initStartView() {
        binding.rvItem.run {
            adapter = employeePagingAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
                gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            }
            setHasFixedSize(true)
        }
    }

    @ExperimentalPagingApi
    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.paging.collectLatest {
                employeePagingAdapter.submitData(it)
            }
        }
    }

    @ExperimentalPagingApi
    private fun searchData() {
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (binding.editText.length() > 0) {
                    filter(s.toString())
                }
            }
        })
    }


    @ExperimentalPagingApi
    fun filter(query: String?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.paging.collectLatest { it->
                val data = it.filter { query?.let { it1 -> it.name?.first?.contains(it1) }!! }
                employeePagingAdapter.submitData(data)
            }
        }
    }


    override fun onClickListeners(result: ModelResult) {
        findNavController().navigate(
            R.id.action_resultFragment_to_detailsFragment,
            bundleOf("result" to result)
        )
    }

}