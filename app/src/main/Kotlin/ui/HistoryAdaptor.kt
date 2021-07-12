package io.terameteo.actionlist.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.GridPlainBinding
import io.terameteo.actionlist.model.isDoneAt
import io.terameteo.actionlist.safetyGet

class HistoryAdaptor(val viewModel: MainViewModel):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return 40
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = GridPlainBinding.inflate(layoutInflater, parent, false)
        return  GridViewHolder(binding)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val gridView = (holder as GridViewHolder).grid as TextView

        val row = position % 5
        val column = position /5
        when (row){
            0 -> {
                gridView.setBackgroundColor(ResourcesCompat.getColor(gridView.resources,
                    R.color.colorForestGreen,null))
                bindHeaderDate(column,gridView)
            }
            in 1..5 -> {
                if(column == 0) bindItemTitle(row,gridView) else  bindItemLog(row,column,gridView)
            }

        }
    }
    class GridViewHolder( binding: GridPlainBinding) :RecyclerView.ViewHolder(binding.root){
        val grid = binding.grid
    }

    private fun bindHeaderDate(column:Int,view: TextView){
        if (column >= 1 ) view.text = viewModel.dateShortList[column - 1]  // × =>
    }

    private fun bindItemTitle(row:Int, view: TextView){
        view.text = viewModel.liveList.safetyGet(row).title
    }
    private fun bindItemLog(row:Int, column: Int,view: TextView){
        val item = viewModel.liveList.safetyGet(row)
        var dateStr = "2021/" + viewModel.dateShortList[column-1]
        view.text = if (item.isDoneAt(dateStr)) { "○" } else {"×"}
    }

}
// GridLayout VERTICAL(Span3)    0 1 2   HORIZON 0
//                               3 4 5             1
//                               6 7 8             2
//  除算　/　　%
