package io.terameteo.actionlist.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.*
import io.terameteo.actionlist.*
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.FragmentMainBinding
import io.terameteo.actionlist.model.ERROR_TITLE

const val ALL_CATEGORY = "全て"

@SuppressLint("ClickableViewAccessibility")
class MainFragment : Fragment() {
    private val mViewModel: MainViewModel by activityViewModels()
    private lateinit var mBinding: FragmentMainBinding
    private lateinit var mGestureDetector: GestureDetector
    private lateinit var mAdaptor: MainListAdaptor
    private lateinit var mCategoryAdaptor:CategoryListAdaptor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        mAdaptor = MainListAdaptor(
            viewModel = mViewModel,
            mViewModel.getDateStr(mViewModel.currentPage.valueOrZero(), DATE_EN)
        )
        mBinding.firstPageList.adapter = mAdaptor
        mCategoryAdaptor = CategoryListAdaptor(mViewModel)

//        mBinding.categoryList.adapter = mCategoryAdaptor
//        mBinding.categoryList.layoutManager = LinearLayoutManager(this.requireContext(),RecyclerView.HORIZONTAL,false)

        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // コマンド処理
        mBinding.toHistoryButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_historyFragment)
        }
        val listener = object :GestureDetector.SimpleOnGestureListener(){
            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                val moveX = (e2.x - e1.x).toInt()

                if(moveX >= 50) swipeRight()
                if(moveX <= -50) swipeLeft()
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
            mBinding.dateShowing.text = mViewModel.getDateStr(it, DATE_JP)
            mAdaptor.dateChange(mViewModel.getDateStr(it, DATE_EN))
        }
        mViewModel.allItemList.observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()) return@observe
            mAdaptor.submitList(it)
        }
        mViewModel.currentCategory.observe(viewLifecycleOwner){
            Log.i("MainFragment","current category $it was observed.")
        }

        mViewModel.usedCategories.observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()) return@observe
            mCategoryAdaptor.submitList(it)
        }

        super.onViewCreated(view, savedInstanceState)
    }
    override fun onPause() {
        mViewModel.saveListToRoom(mAdaptor.currentList)
        super.onPause()
    }
    private fun bindSpinner(binding: FragmentMainBinding){

        val spinnerView = binding.spinnerCategory
        val list = mViewModel.usedCategories.getCategories().toMutableList()
        // カテゴリーが取得出来ない場合は(アイテムのデーターベース所得前など) ALL_CATEGORYのみ｡
        if(list[0] == ERROR_TITLE) list.clear()
        list.add(ALL_CATEGORY)

        var indexSelected =  spinnerView.selectedItemPosition
        Log.i("Mainfrangemt", "spinner is $indexSelected")

        if( indexSelected < 0) {
            indexSelected = 1
        } else {
            spinnerView.selectedItemPosition
        }

        val listAdapter =  ArrayAdapter(this.requireContext(),android.R.layout.simple_spinner_item,list)
        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerView.adapter = listAdapter
        spinnerView.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                Log.i("MainFragment","position $position was selected.")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.i("MainFragment","Nothing seleceted..")
            }
        }
    }
    private fun swipeLeft(){
        val page = mViewModel.currentPage.valueOrZero()
        if (page < 90) mViewModel.currentPage.postValue(page+1)
    }
    private fun swipeRight(){
        val page = mViewModel.currentPage.valueOrZero()
        if (page >= 1) mViewModel.currentPage.postValue(page-1)
    }
    //  e1 Scrollの起点 e2 現在の場所
}