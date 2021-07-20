package io.terameteo.actionlist
import android.content.Context
import androidx.lifecycle.*
import io.terameteo.actionlist.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val  VIEW_MODEL = "mainViewModel"

class MainViewModel() : ViewModel() {
    private val myModel by lazy { MyModel() }
    val dateJpList = MutableList(10){"1970年1月1日(木)"}
    val dateEnList = MutableList(10){"1970/1/1"}
    val dateShortList = MutableList(7){"1/1"}
    val currentCategories = mutableListOf<String>()

    // LiveData
    lateinit var liveList:LiveData<List<ItemEntity>>
    lateinit var bindList:MediatorLiveData<List<ItemEntity>>
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

        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val category = currentCategory.value ?:""
                if(category.isBlank()) {
                    liveList = myModel.dao.getAll()
                } else {
                    liveList = myModel.dao.getByCategory(category)
                }
                if( liveList.value.isNullOrEmpty()) {
                    // Roomから得たリストが空やNULLならばリソースからリスト作成
                    val list = myModel.makeItemListFromResource(_context)
                    currentCategories.addAll(myModel.makeCategoryList(list))
            }
        }


    }
    fun stateSave(_context: Context) {
        val reward = currentReward.value ?:0
        myModel.saveRewardToPreference(reward,_context)
        myModel.saveCurrentCategory(currentCategory.value ?:"",_context)
        val list = List(liveList.value?.size ?:0 ){
                index -> liveList.safetyGet(index)
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO){
            for(i in list.indices) {
                myModel.insertItem(list[i])
            }
            }
        }
    }
    // クリックでその日の完了/未完了を切り替える｡ dateStr YYYY/m/d
    fun flipItemHistory(item:ItemEntity,dateStr: String){
        val currentValue =  currentReward.valueOrZero()
        val newValue = if ( item.isDoneAt(dateStr)) {
            // アイテムがチェック済み チェックをはずす
            myModel.deleteDateFromItem(item,dateStr)
            currentValue - item.reward
        } else {
            myModel.appendDateToItem(item,dateStr)
            currentValue + item.reward
        }
        currentReward.postValue(newValue)
    }
    fun appendItem(newTitle:String,newReward:Int,category:String){
        if(newTitle.isBlank()) return
        val newCategory = if(category.isBlank())  "Daily" else category
        val newItem = ItemEntity(title = newTitle,reward = newReward,category = newCategory)
        val list = liveList.value?.toMutableList() ?: emptyList<ItemEntity>().toMutableList()
        list.add(newItem)
        liveList.postValue(list)
    }

    fun filterItemBy(category: String){
        viewModelScope.launch {
            val list = myModel.makeListByCategory(category)
            liveList.postValue(list)
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
//  ViewModel: Activity再生成や回転で破棄されない独自のLifecycleで管理されるClass(ViewModelLifeCycle)
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