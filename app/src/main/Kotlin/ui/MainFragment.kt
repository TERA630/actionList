package io.terameteo.actionlist.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import com.google.android.flexbox.*
import io.terameteo.actionlist.HISTORY_WINDOW
import io.terameteo.actionlist.MAIN_WINDOW
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.FragmentMainBinding
import io.terameteo.actionlist.model.ERROR_CATEGORY

// TODO Category選択時のPost
const val ARG_POSITION = "positionOfThisFragment"
class MainFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentMainBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val flexBoxLayoutManager = FlexboxLayoutManager(this.context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        val page = this.arguments?.getInt(ARG_POSITION) ?:0
        binding.dateShowing.text = viewModel.dateJpList[page]
        binding.firstPageList.layoutManager = flexBoxLayoutManager
        val adapter = MainListAdaptor(viewModel = viewModel,page)
        binding.firstPageList.adapter = adapter


        // イベントハンドラ

        binding.dateShowing.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            val fragmentOrNull = parentFragmentManager.findFragmentByTag(HISTORY_WINDOW) as HistoryFragment?
            fragmentOrNull ?.let {
                transaction.show(it)
            }?: run {
                val fragment = HistoryFragment.newInstance()
                transaction.add(fragment, HISTORY_WINDOW)
                transaction.replace(R.id.baseFrame,fragment)
            }
            transaction.addToBackStack(null)
            transaction.commit()
        }


        //　データ更新時の応答設定
        viewModel.liveList.observe(viewLifecycleOwner){
            adapter.submitList(it)
            binding.firstPageList.adapter = adapter
        }

        viewModel.currentCategories.observe(viewLifecycleOwner){
            val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item)
            val categoryList = viewModel.currentCategories.value ?: listOf(ERROR_CATEGORY)
            for(i in categoryList.indices){
                arrayAdapter.add(categoryList[i])
            }
            binding.spinner.adapter = arrayAdapter
        }
        return binding.root
    }
    companion object {
        @JvmStatic
        fun newInstance(position: Int): MainFragment {
            val newFragment = MainFragment()
            newFragment.arguments = Bundle().apply {
                putInt(ARG_POSITION,position)
            }
            return newFragment
        }
    }
}