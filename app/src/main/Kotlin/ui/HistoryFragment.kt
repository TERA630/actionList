package io.terameteo.actionlist.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.FragmentHistoryBinding

class HistoryFragment:Fragment() {
    private val mViewModel: MainViewModel by activityViewModels()
    private lateinit var mAdaptor : HistoryAdaptor
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // bind view
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)
        binding.historyGrid.layoutManager = GridLayoutManager(binding.root.context,5,GridLayoutManager.HORIZONTAL,false)
        mAdaptor = HistoryAdaptor(mViewModel)
        binding.historyGrid.adapter = mAdaptor
        // コマンド処理
        binding.toMainButton.setOnClickListener {
            findNavController( ).navigate(R.id.action_categoryFragment_to_mainFragment)
        }
        mViewModel.allItemList.observe(viewLifecycleOwner){
            val selectedCategory = binding.spinnerOfHistory.selectedItem as String
            val list= if(selectedCategory.isBlank()) { it } else {
                it.filter { itemEntity ->  itemEntity.category == selectedCategory }
            }
            mAdaptor.submitList(list)
        }
        return binding.root
    }
}