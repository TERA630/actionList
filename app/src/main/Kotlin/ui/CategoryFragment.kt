package ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.FragmentCategoryBinding
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
        mBinding.categoryList.adapter =CategoryListAdaptor(mViewModel)
        mBinding.backToMain.setOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_mainFragment)
        }
        return mBinding.root
    }

    override fun onPause() {

        super.onPause()
    }



}