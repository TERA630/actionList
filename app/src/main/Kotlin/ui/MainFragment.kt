package io.terameteo.actionlist.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.flexbox.*
import io.terameteo.actionlist.*
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.FragmentMainBinding
import io.terameteo.actionlist.model.ERROR_CATEGORY

// TODO Category選択時のPost

class MainFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentMainBinding
    lateinit var  detector:GestureDetector
    lateinit var adaptor: MainListAdaptor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val flexBoxLayoutManager = FlexboxLayoutManager(this.context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.firstPageList.layoutManager = flexBoxLayoutManager
        adaptor = MainListAdaptor(viewModel = viewModel,viewModel.dateEnList[viewModel.currentPage.valueOrZero()])
        binding.firstPageList.adapter = adaptor
        // イベントハンドラ
        binding.imageButton.setOnClickListener {
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
        detector = GestureDetector(context,listener)
        binding.dateShowing.setOnTouchListener { v, event ->
           detector.onTouchEvent(event)
            true
        }
        //　データ更新時の応答設定
        viewModel.currentPage.observe(this.viewLifecycleOwner){
            binding.dateShowing.text = viewModel.dateJpList[it]
            adaptor.dateStrChange(viewModel.dateEnList[it])
        }
        viewModel.liveList.observe(viewLifecycleOwner){
            adaptor.submitList(it)
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
    private fun swipeLeft(){
        val page = viewModel.currentPage.valueOrZero()
        if (page >= 1) viewModel.currentPage.postValue(page-1)
    }
    private fun swipeRight(){
        val page = viewModel.currentPage.valueOrZero()
        if (page < 9) viewModel.currentPage.postValue(page+1)
    }
    //  e1 Scrollの起点 e2 現在の場所
}