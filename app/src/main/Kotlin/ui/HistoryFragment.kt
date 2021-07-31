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
import io.terameteo.actionlist.valueOrZero

class HistoryFragment:Fragment() {
    private val mViewModel: MainViewModel by activityViewModels()
    private lateinit var mAdaptor : HistoryAdaptor
    private lateinit var mBinding:FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // bind view
        mBinding = FragmentHistoryBinding.inflate(inflater, container, false)
        mBinding.historyGrid.layoutManager = GridLayoutManager(mBinding.root.context,
            NUMBER_OF_ITEMS,GridLayoutManager.HORIZONTAL,false)
        mAdaptor = HistoryAdaptor(mViewModel)
        mBinding.historyGrid.adapter = mAdaptor

        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // コマンド処理
        mBinding.toMainButton.setOnClickListener {
            findNavController( ).navigate(R.id.action_historyFragment_to_mainFragment)
        }
        mBinding.toPast.setOnClickListener {
            val page = mViewModel.currentPage.valueOrZero()
            if( page< 93 ) mViewModel.currentPage.postValue(page + 7 )
        }
        // データー更新時
        mViewModel.allItemList.observe(viewLifecycleOwner){
            mAdaptor.submitList(it)
        }
        mViewModel.currentPage.observe(this.viewLifecycleOwner){

        }

    }
    override fun onPause() {
        mViewModel.saveListToRoom( mAdaptor.currentList)
        super.onPause()
    }
}