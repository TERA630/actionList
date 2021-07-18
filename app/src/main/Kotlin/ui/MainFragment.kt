package io.terameteo.actionlist.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.flexbox.*
import io.terameteo.actionlist.*
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.FragmentMainBinding
import io.terameteo.actionlist.model.ERROR_CATEGORY

// TODO Category選択時のPost
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
        mBinding.firstPageList.layoutManager = flexBoxLayoutManager
        mAdaptor = MainListAdaptor(viewModel = mViewModel,mViewModel.dateEnList[mViewModel.currentPage.valueOrZero()])
        mBinding.firstPageList.adapter = mAdaptor
        // コマンド処理
        mBinding.imageButton.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            val fragmentOrNull = parentFragmentManager.findFragmentByTag(HISTORY_WINDOW) as HistoryFragment?
            if(fragmentOrNull == null){
                val fragment = HistoryFragment.newInstance()
                transaction.replace(R.id.baseFrame,fragment)
            } else {
                transaction.replace(R.id.baseFrame,fragmentOrNull)
            }
            transaction.addToBackStack(null)
            transaction.commit()
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
        mBinding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val category = parent.selectedItem.toString()
                mViewModel.filterItemBy(category)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.i(VIEW_MODEL,"no category selected")
            }

        }
        //　データ更新時のUI更新設定
        mViewModel.currentPage.observe(this.viewLifecycleOwner){
            mBinding.dateShowing.text = mViewModel.dateJpList[it]
            mAdaptor.dateStrChange(mViewModel.dateEnList[it])
        }
        mViewModel.liveList.observe(viewLifecycleOwner){
            mAdaptor.submitList(it)
        }
        mViewModel.currentCategories.observe(this.viewLifecycleOwner){
            val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item)
            val categoryList = mViewModel.currentCategories.value ?: listOf(ERROR_CATEGORY)
            for(i in categoryList.indices){
                arrayAdapter.add(categoryList[i])
            }
            mBinding.spinner.adapter = arrayAdapter
        }
        return mBinding.root
    }
    private fun swipeLeft(){
        val page = mViewModel.currentPage.valueOrZero()
        if (page >= 1) mViewModel.currentPage.postValue(page-1)
    }
    private fun swipeRight(){
        val page = mViewModel.currentPage.valueOrZero()
        if (page < 9) mViewModel.currentPage.postValue(page+1)
    }
    //  e1 Scrollの起点 e2 現在の場所
}