package io.terameteo.actionlist.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.databinding.ItemCategoryBinding

class CategoryListAdaptor(val mViewModel: MainViewModel)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    class ItemViewHolder(private val mBinding:ItemCategoryBinding)
        :RecyclerView.ViewHolder(mBinding.root){
        fun bind(text:String){
            mBinding.categoryText.text = text
        }
    }
    override fun getItemCount(): Int {
        return mViewModel.currentCategories.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemCategoryBinding.inflate(layoutInflater, parent, false)
        return ItemViewHolder(binding)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ItemViewHolder).bind(mViewModel.currentCategories[position])
    }
}