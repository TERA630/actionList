package io.terameteo.actionlist.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.*
import io.terameteo.actionlist.MAIN_WINDOW
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.FragmentMainBinding
import io.terameteo.actionlist.valueOrZero

@SuppressLint("ClickableViewAccessibility")
class MainFragment : Fragment() {
    private val mViewModel: MainViewModel by activityViewModels()
    private lateinit var mBinding: FragmentMainBinding
    private lateinit var  mGestureDetector:GestureDetector
    private lateinit var mAdaptor: MainListAdaptor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        val flexBoxLayoutManager = FlexboxLayoutManager(this.context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }

        // bind View
        mBinding.firstPageList.layoutManager = flexBoxLayoutManager
        mAdaptor = MainListAdaptor(viewModel = mViewModel,mViewModel.dateEnList[mViewModel.currentPage.valueOrZero()])
        mBinding.firstPageList.adapter = mAdaptor

        val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item)
        arrayAdapter.addAll(mViewModel.currentCategories)
        mBinding.spinner.adapter = arrayAdapter

        // コマンド処理
        mBinding.toHistoryButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_historyFragment)
        }
        val listener = object :GestureDetector.SimpleOnGestureListener(){
            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val moveX = (e2.x - e1.x).toInt()
                val moveY = (e2.y - e1.y).toInt()
                Log.i(MAIN_WINDOW,"vector $moveX , $moveY, velocity is  $velocityX, $velocityY")
                if(moveX >= 100) swipeRight()
                if(moveX <= -100) swipeLeft()
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        }
        mGestureDetector = GestureDetector(context,listener)
        mBinding.dateShowing.setOnTouchListener { v, event ->
           mGestureDetector.onTouchEvent(event)
            true
        }

        //　データ更新時のUI更新設定
        mViewModel.currentPage.observe(this.viewLifecycleOwner){
            mBinding.dateShowing.text = mViewModel.dateJpList[it]
            mAdaptor.dateStrChange(mViewModel.dateEnList[it])
        }
        mViewModel.allItemList.observe(viewLifecycleOwner){
            val selectedCategory = mBinding.spinner.selectedItem as String
            val list= if(selectedCategory.isBlank()) { it } else {
                it.filter { itemEntity ->  itemEntity.category == selectedCategory }
            }
            mAdaptor.submitList(list)
        }
        return mBinding.root
    }

    override fun onPause() {
        val list = mAdaptor.currentList
        mViewModel.saveListToRoom(list)
        Log.i(MAIN_WINDOW,"mainFragment was paused.")
        super.onPause()
    }
    private fun swipeLeft(){
        val page = mViewModel.currentPage.valueOrZero()
        if (page < 9) mViewModel.currentPage.postValue(page+1)
    }
    private fun swipeRight(){
        val page = mViewModel.currentPage.valueOrZero()
        if (page >= 1) mViewModel.currentPage.postValue(page-1)
    }
    //  e1 Scrollの起点 e2 現在の場所
}