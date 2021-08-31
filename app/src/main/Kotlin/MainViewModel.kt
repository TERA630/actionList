package io.terameteo.actionlist
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import io.terameteo.actionlist.model.*
import io.terameteo.actionlist.ui.CategoryWithChecked
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val VIEW_MODEL = "mainViewModel"
const val DATE_JP = 1
const val DATE_EN = 2
const val DATE_SHORT = 3

class MainViewModel(private val myModel:MyModel) : ViewModel() {
    private val  viewModelIOScope =  CoroutineScope(Job() + viewModelScope.coroutineContext + Dispatchers.IO)
    // LiveData
    val allItemList:LiveData<List<ItemEntity>> =  myModel.dao.getAll()
    private val currentReward:MutableLiveData<Int> = MutableLiveData(0)
    val currentRewardStr = MediatorLiveData<String>()
    val currentPage = MutableLiveData(0)
    val currentCategory = MutableLiveData("")
    val usedCategories= MediatorLiveData<List<CategoryWithChecked>>()

    fun initialize(_context:Context) {
        currentReward.postValue(myModel.loadRewardFromPreference(_context))
        currentRewardStr.addSource(currentReward) { value -> currentRewardStr.postValue("$value　円") }
        currentCategory.postValue(myModel.loadCategoryFromPreference(_context))

        usedCategories.addSource(allItemList){
            if(it.isNullOrEmpty()) return@addSource
            val list = myModel.makeCategoryList(it)
            val newList = updateCategoryList(list)
            if(newList.isNotEmpty()) usedCategories.postValue(newList)
        }
        usedCategories.addSource(currentCategory){
            val list = usedCategories.value
            if(list.isNullOrEmpty()) return@addSource
            val newlist = List(list.size){
                index -> CategoryWithChecked(list[index].title,(list[index].title == it))
            }
            usedCategories.postValue(newlist)
        }
    }
    private fun updateCategoryList(list:List<String>):List<CategoryWithChecked> {
        return List(list.size){ i ->  CategoryWithChecked(list[i], (list[i] == currentCategory.value )) }
    }


    fun stateSave(_context: Context) {
        val reward = currentReward.value ?:0
        myModel.saveRewardToPreference(reward,_context)
        myModel.saveCurrentCategory(currentCategory.value ?:"",_context)

    }
    fun flipItemHistory(item:ItemEntity,dateStr: String){
        // クリックでその日の完了/未完了を切り替える｡ dateStr YYYY/m/d
        val currentValue =  currentReward.valueOrZero()
        val newValue = if ( item.isDoneAt(dateStr) ) {
            // アイテムがチェック済み チェックをはずす
            myModel.deleteDateFromItem(item,dateStr)
            currentValue - item.reward
        } else {
            // アイテムが未チェック､チェックをつける
            item.appendDate(dateStr)
            currentValue + item.reward
        }
        viewModelIOScope.launch { myModel.dao.update(item) }
        currentReward.postValue(newValue)
    }

    fun appendItem(newTitle:String,newReward:Int,category:String){
        if(newTitle.isBlank()) return
        val newCategory = if( category.isBlank())  "Daily" else category

        val currentList = allItemList.safetyGetList()
        var newId = currentList.size
        // idが被らない様に処理
        do {
             var duplicateItem = currentList.firstOrNull() { itemEntity -> itemEntity.id == newId }
             newId++
        } while (duplicateItem != null)

        val newItem = ItemEntity(id = newId, title = newTitle,reward = newReward,category = newCategory)

        viewModelIOScope.launch {
            myModel.dao.insert(newItem)
            Log.i(VIEW_MODEL,"item $newTitle was appended to List")
        }
    }
    fun deleteItem(item: ItemEntity){
        viewModelIOScope.launch {
            myModel.dao.delete(item)
            Log.i(VIEW_MODEL,"item ${item.title}  ${item.id} was deleted to List")
        }
    }
    fun saveListToRoom(_list:List<ItemEntity>){
        viewModelIOScope.launch {
            myModel.dao.updateList(_list)
        }
    }
    fun makeListFromResource(_context: Context){
        viewModelIOScope.launch {
            val list = myModel.makeItemListFromResource(_context)
            list.forEach { item ->
                myModel.dao.insert(item)
            }
        }
    }
    fun getDateStr(backDate: Int, mode: Int): String {
        return when (mode) {
            DATE_EN -> myModel.getDayStringEn(backDate)
            DATE_JP -> myModel.getDayStringJp(backDate)
            DATE_SHORT -> myModel.getDayStringShort(backDate)
            else -> {
                Log.w(VIEW_MODEL, "wrong param was found getDateStr")
                ""
            }
        }
    }
    class Factory(private val model:MyModel):ViewModelProvider.NewInstanceFactory(){
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(model) as T
        }
    }
}
// LiveDataの拡張関数 Static method
fun MutableLiveData<Int>.valueOrZero() : Int{
    return this.value ?: 0
}
fun MutableLiveData<List<ItemEntity>>.safetyGet(position:Int): ItemEntity {
    val list = this.value
    return if (list.isNullOrEmpty()) {
        ItemEntity(title = ERROR_TITLE,category = ERROR_CATEGORY)
    } else {
        list[position]
    }
}

fun LiveData<List<ItemEntity>>.safetyGetList():List<ItemEntity> {
    val list = this.value
    return if (list.isNullOrEmpty()) {
        Log.w(VIEW_MODEL, "livaData list  was empty.")
        listOf(ItemEntity(title = ERROR_TITLE,category = DEFAULT_CATEGORY))
    } else {
        list
    }
}

fun LiveData<List<CategoryWithChecked>>.getCategories():List<String> {
    val list = this.value
    return if (list.isNullOrEmpty()) {
        Log.w(VIEW_MODEL, "livaData category list  was empty.")
        listOf( ERROR_TITLE )
    } else {
        return List(list.size) { i -> list[i].title }
    }
}
fun MediatorLiveData<List<String>>.safetyGet(position: Int):String{
    val category = this.value
    return if ((category.isNullOrEmpty())) {
        Log.w(VIEW_MODEL,"safetyGet $position was failed.")
        ERROR_CATEGORY
    } else {
        category[position]
    }
}
fun LiveData<List<CategoryWithChecked>>.listSize():Int{
    val list = this.value
    if(list.isNullOrEmpty()){return 0 }else{ return list.size}
}


//  ViewModel: Activity再生成や回転で破棄されない独自の長いLifecycleで管理されるClass(ViewModelLifeCycle)
//  retainInstance = trueなHolderFragmentにキャッシュされているらしい｡
//  各Activity固有｡ 同じActivityのFragmentでは共有できる｡
//  負わせるべき役割
//  Model-> ViewModel　ModelからUIの描画(Binding)に必要な情報に変換しLivedataで保持する｡
//  ActivityやFragmentはLiveDataをObserveして変更があればUI反映 or DataBinding使用｡
//  VMはView､ActivityContext の参照を保持するべきでない｡
//  ユーザーのViewへのActionを受け取り､Modelに通知する｡　Commands
//  ViewがModelのメンバを直接操作するのは推奨されない｡

// CoroutineScope：CoroutineをLaunchする情報をもつClass
// CoroutineContextを持つ｡
// CoroutineContext　Job　CoroutineDispatcher　CoroutineExceptionHandlerなどををもつ
//　Job：LaunchされたCoroutineをキャンセルできるClass　Launchごとに割り当てられる｡
//  各Coroutineの親子関係も制御できる｡
//　CoroutineDispatcher：Coroutineを動かすThreadを指定できる｡
//　指定がなければDispatcher.Defaultが追加される｡　ほかにはDispatcher.Mail　.IOなど