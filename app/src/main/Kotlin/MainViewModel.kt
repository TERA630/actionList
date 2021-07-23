package io.terameteo.actionlist
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import io.terameteo.actionlist.model.*
import kotlinx.coroutines.*

const val  VIEW_MODEL = "mainViewModel"

class MainViewModel() : ViewModel() {
    private val myModel by lazy { MyModel() }
    val dateJpList = MutableList(10){"1970年1月1日(木)"}
    val dateEnList = MutableList(10){"1970/1/1"}
    val dateShortList = MutableList(7){"1/1"}
    lateinit var currentCategories:MutableList<String>
    private val  viewModelIOScope =  CoroutineScope(Job() + viewModelScope.coroutineContext + Dispatchers.IO)
    // LiveData
    lateinit var allItemList:LiveData<List<ItemEntity>>
    private val currentReward:MutableLiveData<Int> = MutableLiveData(0)
    val currentRewardStr = MediatorLiveData<String>()
    val currentPage = MutableLiveData(0)
    val currentCategory = MutableLiveData<String>("")

    fun initialize(_context:Context) {
        myModel.initializeDB(_context)
        for (i in 0..9) {
            dateEnList[i] = myModel.getDayStringEn(9 - i)
        }
        for (i in 0..9) {
            dateJpList[i] = myModel.getDayStringJp(9 - i)
        }
        for (i in 0..6){
            dateShortList[i] = myModel.getDayStringShort(6 - i)
        }
        currentReward.postValue(myModel.loadRewardFromPreference(_context))
        currentRewardStr.addSource(currentReward) { value -> currentRewardStr.postValue("$value　円") }
        currentCategory.postValue(myModel.loadCategoryFromPreference(_context))
        currentCategories = myModel.loadCategories(_context).toMutableList()

        viewModelIOScope.launch {
                allItemList = myModel.dao.getAll()
                val category = currentCategory.value ?:""
                if(category.isBlank()) {

                } else {
                    allItemList = myModel.dao.getByCategory(category)
                }
                if( allItemList.value.isNullOrEmpty()) {
                    // Roomから得たリストが空やNULLならばリソースからリスト作成
                    val list = myModel.makeItemListFromResource(_context)
                    list.forEach { item ->
                        myModel.insertItem(item)
                    }
                    allItemList = myModel.dao.getAll()
                }
        }


    }
    fun stateSave(_context: Context) {
        val reward = currentReward.value ?:0
        myModel.saveRewardToPreference(reward,_context)
        myModel.saveCurrentCategory(currentCategory.value ?:"",_context)
        myModel.saveCategories(currentCategories,_context)
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
            myModel.appendDateToItem(item,dateStr)
            currentValue + item.reward
        }
        currentReward.postValue(newValue)
    }

    fun appendItem(newTitle:String,newReward:Int,category:String){
        if(newTitle.isBlank()) return
        val newCategory = if(category.isBlank())  "Daily" else category
        val newItem = ItemEntity(title = newTitle,reward = newReward,category = newCategory)
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                myModel.insertItem(newItem)
            }
        }
    }
    fun saveListToRoom(_list:List<ItemEntity>){
        viewModelIOScope.launch {
            myModel.dao.updateList(_list)
        }
    }
    fun makeCategoryFromList(_list:List<ItemEntity>){
        currentCategories = myModel.makeCategoryList(_list).toMutableList()
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

fun LiveData<List<ItemEntity>>.safetyGet(position:Int): ItemEntity {
    val list = this.value
    return if (list.isNullOrEmpty()) {
        Log.w(VIEW_MODEL,"safetyGet $position was failed.")
        ItemEntity(title = ERROR_TITLE,category = ERROR_CATEGORY)
    } else {
        list[position]
    }
}
fun LiveData<List<ItemEntity>>.safetyGetList():List<ItemEntity> {
    val list = this.value
    if (list.isNullOrEmpty()) {
        Log.w(VIEW_MODEL, "livaData list  was empty.")
        return listOf(ItemEntity(title = ERROR_TITLE,category = DEFAULT_CATEGORY))
    } else {
        return list
    }
}

//  ViewModel: Activity再生成や回転で破棄されない独自の長いLifecycleで管理されるClass(ViewModelLifeCycle)
//  retainInstance = trueなHolderFragmentにキャッシュされているらしい｡
//  各Activity固有｡ 同じActivityのFragmentでは共有できる｡
//  負わせるべき役割
//  Model-> ViewModel　ModelからUIの描画(Binding)に必要な情報に変換しLivedataで保持する｡
//  ActivityやFragmentはLiveDataをObserveして変更があればUI反映 or DataBinding使用｡
//  VMはViewへの参照は持つべきでない｡ ActivityContext の参照を保持するべきでない｡
//  ユーザーのViewへのActionを受け取り､Modelに通知する｡　Commands
//  ViewがModelのメンバを直接操作するのは推奨されない｡

// CoroutineScope：CoroutineをLaunchする情報をもつClass
// CoroutineContextを持つ｡
// CoroutineContext　Job　CoroutineDispatcher　CoroutineExceptionHandlerなどををもつ
//　Job：LaunchされたCoroutineをキャンセルできるClass　Launchごとに割り当てられる｡
//  各Coroutineの親子関係も制御できる｡
//　CoroutineDispatcher：Coroutineを動かすThreadを指定できる｡
//　指定がなければDispatcher.Defaultが追加される｡　ほかにはDispatcher.Mail　.IOなど