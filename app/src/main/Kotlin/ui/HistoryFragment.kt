package io.terameteo.actionlist.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import io.terameteo.actionlist.MAIN_WINDOW
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.FragmentHistoryBinding

class HistoryFragment:Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // bind view
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)
        binding.historyGrid.layoutManager = GridLayoutManager(binding.root.context,5,GridLayoutManager.HORIZONTAL,false)
        binding.historyGrid.adapter = HistoryAdaptor(viewModel)
        // event handling
        binding.toMainButton.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            val fragmentOrNull = parentFragmentManager.findFragmentByTag(MAIN_WINDOW) as MainFragment?
            if(fragmentOrNull != null) transaction.replace(R.id.baseFrame,fragmentOrNull)
            else {
                val fragment = MainFragment()
                transaction.addToBackStack(null)
                transaction.replace(R.id.baseFrame,fragment)
            }
            transaction.commit()
        }

        return binding.root
    }
    companion object {
        @JvmStatic
        fun newInstance(): HistoryFragment {
            val newFragment = HistoryFragment()
            return newFragment
        }
    }
}