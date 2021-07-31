package io.terameteo.actionlist.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.FragmentDetailBinding
import io.terameteo.actionlist.safetyGetList

class DetailFragment : Fragment() {
    private val mViewModel: MainViewModel by activityViewModels()
    private val args:DetailFragmentArgs by navArgs()
    lateinit var mBinding:FragmentDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentDetailBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val list = mViewModel.allItemList.safetyGetList()
        val item = list.find { itemEntity -> itemEntity.id == args.idToEdit  }
        item?.let {
            mBinding.editTitle.setText(item.title)
        } ?: run {
            Log.w("detailFragment","item ${args.idToEdit} was not faund")
        }


        mBinding.detailCancelButton.setOnClickListener { v ->
            findNavController().navigate(R.id.action_detailFragment_to_mainFragment)
        }
        mBinding.detailOkButton.setOnClickListener { v ->

            findNavController().navigate(R.id.action_detailFragment_to_mainFragment)
        }
        super.onViewCreated(view, savedInstanceState)
    }

}