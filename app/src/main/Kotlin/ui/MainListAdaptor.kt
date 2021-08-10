package io.terameteo.actionlist.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.ItemTestBinding
import io.terameteo.actionlist.model.ItemEntity
import io.terameteo.actionlist.model.isDoneAt

// VMと dateStr YYYY/m/d を渡されると list の historyに含まれているかをみて､BackGroundを切りかえて表示する｡

class MainListAdaptor(
    private val viewModel: MainViewModel,
    private var dateStr:String ) : ListAdapter<ItemEntity, RecyclerView.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderOfCell {
        // リストの表示要求があったとき､viewTypeに応じて必要なViewHolderを確保する｡
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolderOfCell(ItemTestBinding.inflate(layoutInflater, parent, false))
    }
    class ViewHolderOfCell( val binding: ItemTestBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Viewへの参照を保持｡ViewBindingが使用可能となったので､個々の要素でなく､Bindingのみ保持するようになった｡
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // リストのPositionの部位の表示要求があったときに､データをViewに設定する｡
        val item = getItem(position)
        val holderOfCell = holder as ViewHolderOfCell
        val backGround = if (item.isDoneAt(dateStr)){
            R.drawable.square_gold_gradient
        } else {
            R.drawable.square_silver_gradient
        }
        val thisView = holderOfCell.binding.cellText
        thisView.text = item.title
        thisView.background = ResourcesCompat.getDrawable(
            thisView.resources, backGround, thisView.context.theme)
        val category = viewModel.currentCategory.value

        if( category.isNullOrBlank()) {
            bindCommands(holderOfCell.binding,position)
        } else if(getItem(position).category == category){
            holderOfCell.binding.root.visibility = View.VISIBLE
            bindCommands(holderOfCell.binding,position)
        } else {
            holderOfCell.binding.root.visibility = View.GONE
        }
    }
    fun dateChange(_dateStr: String){
        dateStr = _dateStr
        notifyDataSetChanged()
    }


    private fun bindCommands(binding:ItemTestBinding,position: Int){
        binding.root.setOnClickListener {
            viewModel.flipItemHistory(getItem(position),dateStr)
            notifyItemChanged(position)
        }
        binding.cellText.setOnLongClickListener {
                view ->
            //   val destination = MainFragmentDirections.actionMainFragmentToDetailFragment(getItem(position).id)
            view.showContextMenu()
            true
        }
        binding.root.setOnCreateContextMenuListener { menu, v, menuInfo ->
            MenuInflater(v.context).inflate(R.menu.menu_context,menu)
            menu.findItem(R.id.action_edit_item).setOnMenuItemClickListener {
                val idToEdit = getItem(position).id
                val destination = MainFragmentDirections.actionMainFragmentToDetailFragment(idToEdit)
                Log.i("MainListAdapter","item $idToEdit at $position was edited.")
                v.findNavController().navigate(destination)
                notifyItemChanged(position)
                true
            }
            menu.findItem(R.id.action_delete_item).setOnMenuItemClickListener {
                Log.i("MainListAdaptor","item was  to be deleted.")
                viewModel.deleteItem(getItem(position))
                notifyItemRemoved(position)
                true
            }
        }

    }
}
object DiffCallback : DiffUtil.ItemCallback<ItemEntity>() {
    override fun areItemsTheSame(
        old: ItemEntity, new: ItemEntity ): Boolean {
        return old.id == new.id
    }
    override fun areContentsTheSame(
        old: ItemEntity, new: ItemEntity
    ): Boolean {
        return ((old.title == new.title)
                && (old.history == new.history)
                && (old.category == new.category))
    }

}
// adapter 　Fragmentから呼び出され､ViewModelのデータを使ってListを表示する｡
// ViewHolder→ViewからContextを得る｡保持はしない
// Modelには直接アクセスせず､ViewModelに送る｡

// SpannedString ： テキスト･マークアップ共に作成後変更しない｡
// SpannableString ： テキストは変更する｡ 後からスパンをアタッチすることができる｡
// SpannableStringBuilder ： テキストやスパンを作成後に変更する｡ あるいは多数のスパンをアタッチするとき｡
// Span Bold Italic ､ フォント(Monospace､Serif,sans-serif) 文字色､バックグラウンドカラー 下線 取り消し線  テキストサイズの変更