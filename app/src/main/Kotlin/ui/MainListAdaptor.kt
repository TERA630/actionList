package io.terameteo.actionlist.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.terameteo.actionlist.MainViewModel
import io.terameteo.actionlist.R
import io.terameteo.actionlist.databinding.ItemTestBinding
import io.terameteo.actionlist.model.ItemEntity
import io.terameteo.actionlist.model.isDoneAt

class MainListAdaptor(
    private val viewModel: MainViewModel,
    private val page:Int
) : ListAdapter<ItemEntity, RecyclerView.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderOfCell {
        // リストの表示要求があったとき､viewTypeに応じて必要なViewHolderを確保する｡
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolderOfCell(ItemTestBinding.inflate(layoutInflater, parent, false))
    }
    class ViewHolderOfCell( val binding: ItemTestBinding) :
        RecyclerView.ViewHolder(binding.root)
    // Viewへの参照を保持｡以前は個々の子要素の参照を保存していたが､
    // ViewBindingが使用可能となったので､Bindingのみ保持するようになった｡

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // リストのPositionの部位の表示要求があったときに､データをViewに設定する｡
        val item = getItem(position)
        val holderOfCell = holder as ViewHolderOfCell

        val backGround = if (item.isDoneAt(viewModel.dateEnList[page])){
            R.drawable.square_gold_gradient
        } else {
            R.drawable.square_silver_gradient
        }
        val thisView = holderOfCell.binding.cellText
        thisView.text = item.title
        thisView.background = ResourcesCompat.getDrawable(
            thisView.resources, backGround, thisView.context.theme)
        thisView.setOnClickListener {
            viewModel.flipItemHistory(item,page)
            notifyItemChanged(position)
        }
    }
}

private object DiffCallback : DiffUtil.ItemCallback<ItemEntity>() {
    override fun areItemsTheSame(
        old: ItemEntity, new: ItemEntity ): Boolean {
        return old.id == new.id
    }
    override fun areContentsTheSame(
        old: ItemEntity, new: ItemEntity): Boolean {
        val isSameAppearance = (old.title == new.title)
                &&(old.finishedHistory == new.finishedHistory )
                &&(old.category ==  new.category )
         return isSameAppearance
    }

}
// adapter 　Fragmentから呼び出され､ViewModelのデータを使ってListを表示する｡
// ViewHolder→ViewからContextを得る｡保持はしない
// Modelには直接アクセスせず､ViewModelに送る｡

// SpannedString ： テキスト･マークアップ共に作成後変更しない｡
// SpannableString ： テキストは変更する｡ 後からスパンをアタッチすることができる｡
// SpannableStringBuilder ： テキストやスパンを作成後に変更する｡ あるいは多数のスパンをアタッチするとき｡
// Span Bold Italic ､ フォント(Monospace､Serif,sans-serif) 文字色､バックグラウンドカラー 下線 取り消し線  テキストサイズの変更