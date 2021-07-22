package ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.databinding.FragmentCategoryBinding
import io.terameteo.actionlist.safetyGetList
import io.terameteo.actionlist.ui.CategoryListAdaptor

// Fragmentではon ViewCreatedでViewのBindをするように推奨されているが､
// ViewBindingがあると､Bindをそのまま使えるのでonCreteViewでやってしまう

class CategoryFragment : Fragment() {
    private val mViewModel: MainViewModel by activityViewModels()
    private lateinit var mBinding:FragmentCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle? ): View {
        mBinding = FragmentCategoryBinding.inflate(inflater, container, false)
        val categoryFromItems = mViewModel.makeCategoryFromList(mViewModel.liveList.safetyGetList())
        mBinding.categoryList.adapter =CategoryListAdaptor(mViewModel)

        return mBinding.root
    }



}