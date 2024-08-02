package com.example.listingapp.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.listingapp.R
import com.example.listingapp.adapter.EmployeeLoadStateAdapter
import com.example.listingapp.adapter.EmployeePagingAdapter
import com.example.listingapp.databinding.FragmentResultBinding
import com.example.listingapp.listener.AdapterClickListeners
import com.example.listingapp.other.SpacesItemDecoration
import com.example.listingapp.response.ModelResult
import com.example.listingapp.viewmodel.EmployeeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ResultFragment : Fragment(), AdapterClickListeners {

    private lateinit var binding: FragmentResultBinding
    private val viewModel by viewModels<EmployeeViewModel>()
    private val employeePagingAdapter by lazy { EmployeePagingAdapter(this) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        setupSearch()
    }

    private fun setupRecyclerView() {
        binding.rvItem.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._5sdp)))
            adapter = employeePagingAdapter.withLoadStateHeaderAndFooter(
                header = EmployeeLoadStateAdapter { employeePagingAdapter.retry() },
                footer = EmployeeLoadStateAdapter { employeePagingAdapter.retry() }
            )
        }

        employeePagingAdapter.addLoadStateListener { loadState ->
            val isListEmpty = loadState.refresh is LoadState.NotLoading && employeePagingAdapter.itemCount == 0
            binding.noDataFound.visibility = if (isListEmpty) View.VISIBLE else View.GONE
            binding.rvItem.visibility = if (isListEmpty) View.GONE else View.VISIBLE

            (loadState.source.refresh as? LoadState.Error)?.let {
                Toast.makeText(requireContext(), "Error: ${it.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pagingData.collectLatest { pagingData ->
                employeePagingAdapter.submitData(pagingData)
            }
        }
    }

    private fun setupSearch() {
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString()?.trim() ?: "")
                if (s.isNullOrEmpty()) hideKeyboard()
            }
        })
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
    }

    override fun onClickListeners(result: ModelResult) {
        findNavController().navigate(
            R.id.action_resultFragment_to_detailsFragment,
            bundleOf("result" to result)
        )
    }
}