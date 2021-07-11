package io.terameteo.actionlist
import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.terameteo.actionlist.model.ItemEntity
import io.terameteo.actionlist.model.MyModel
import io.terameteo.actionlist.model.isDoneAt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val myModel: MyModel by lazy { MyModel() }
    val liveList = MutableLiveData<List<ItemEntity>>()
    val dateJpList = MutableList(10){"1970年1月1日(木)"}
    val dateEnList = MutableList(10){"1970/1/1"}
    val dateShortList = MutableList(7){"1/1"}
    val currentReward:MutableLiveData<Int> = MutableLiveData(0)
    val currentRewardStr = MediatorLiveData<String>()
    val currentCategory = MediatorLiveData<List<String>>()

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
        viewModelScope.launch {
            val list = myModel.makeItemList(_context )
            liveList.postValue(list)
        }
        currentCategory.addSource(liveList){ value ->
            val list = myModel.makeCategoryList(value)
            currentCategory.postValue(list)
        }

    }
    fun stateSave(_context: Context) {
        val reward = currentReward.value ?:0
        myModel.saveRewardToPreference(reward,_context)
        val list = List(liveList.value?.size ?:0 ){
                index -> liveList.safetyGet(index)
        }
        val job = Job() + viewModelScope.coroutineContext + Dispatchers.IO
        val viewModelBGScope=  CoroutineScope(job)
        viewModelBGScope.launch {
            for(i in list.indices) {
                myModel.insertItem(list[i])
            }
        }
    }
    // クリックでその日の完了/未完了を切り替える｡
    fun flipItemHistory(item:ItemEntity,page:Int){
        val currentValue =  currentReward.valueOrZero()
        if ( item.isDoneAt(dateEnList[page])) {
            // アイテムがチェック済み チェックをはずす
            myModel.deleteDateFromItem(item,dateEnList[page])
            val newValue = currentValue - item.reward
            currentReward.postValue(newValue)
        } else {
            myModel.appendDateToItem(item,dateEnList[page])
            val newValue = currentValue + item.reward
            currentReward.postValue(newValue)
        }
    }
    fun appendItem(newTitle:String,newReward:Int,category:String){
        if(newTitle.isBlank()) return
        val newCategory = if(category.isBlank())  "Daily" else category
        val newItem = ItemEntity(title = newTitle,reward = newReward,category = newCategory)
        val list = liveList.value?.toMutableList() ?: emptyList<ItemEntity>().toMutableList()
        list.add(newItem)
        liveList.postValue(list)
    }


}

// LiveDataの拡張関数 Static method

fun MutableLiveData<Int>.valueOrZero() : Int{
    return this.value ?: 0
}
fun MutableLiveData<List<ItemEntity>>.safetyGet(position:Int): ItemEntity {
    val list = this.value
    return if (list.isNullOrEmpty()) {
        ItemEntity()
    } else {
        list[position]
    }
}
//  ViewModel: Activity再生成や回転で破棄されない独自のLifecycleで管理されるClass(ViewModelLifeCycle)
//  各Activity固有｡ 同じActivityのFragmentでは共有される｡
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



