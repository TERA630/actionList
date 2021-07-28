package io.terameteo.actionlist.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.GridPlainBinding
import io.terameteo.actionlist.model.ItemEntity
import io.terameteo.actionlist.model.isDoneAt

class HistoryAdaptor(private val mViewModel: MainViewModel)
    : ListAdapter<ItemEntity, RecyclerView.ViewHolder>(DiffCallback){
    override fun getItemCount(): Int = 40
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = GridPlainBinding.inflate(layoutInflater, parent, false)
        return  GridViewHolder(binding)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val gridView = holder.itemView as TextView
        val row = position % 5
        val column = position /5
        when (row){
            0 -> {
                gridView.setBackgroundColor(ResourcesCompat.getColor(gridView.resources,
                    R.color.colorForestGreen,null))
                bindHeaderDate(column,gridView)
            }
            in 1..5 -> {
                gridView.setBackgroundColor(ResourcesCompat.getColor(gridView.resources,
                    R.color.white,null))
                if(column == 0) bindItemTitle(row,gridView) else bindItemLog(row,column,gridView)
            }
        }
    }
    class GridViewHolder( binding: GridPlainBinding) :RecyclerView.ViewHolder(binding.root){
    }
    private fun bindHeaderDate(column:Int,view: TextView){
        if (column >= 1 ) view.text = mViewModel.dateShortList[column - 1]  // × =>
    }
    private fun bindItemTitle(row:Int, view: TextView){
        val item = getItem(row)
        view.text = item.title
    }
    private fun bindItemLog(row:Int, column: Int,view: TextView){
        val item = getItem(row)
        val dateStr = "2021/" + mViewModel.dateShortList[column-1]
        view.text = if (item.isDoneAt(dateStr)) {  view.resources.getString(R.string.done)} else { view.resources.getString(R.string.undone)}
        view.setOnClickListener {
            mViewModel.flipItemHistory(item,dateStr)
            notifyItemChanged(row + column * 5)
        }
    }
}

//                                column          column
// GridLayout VERTICAL(Span3)    0 1 2   HORIZON 0  3  6
//                               3 4 5    row    1  4  7
//                               6 7 8           2  5  8
//  除算　/　　%
