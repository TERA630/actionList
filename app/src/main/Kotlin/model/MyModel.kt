
package io.terameteo.actionlist.model

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.text.format.DateFormat
import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.room.Room
import io.terameteo.actionlist.R
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

const val ERROR_TITLE = "error title"
const val ERROR_CATEGORY = "error category"
const val REWARD_HISTORY = "rewardHistory"
const val CURRENT_CATEGORY = "currentCategory"
const val CATEGORY_LIST = "categoryList"

const val MY_MODEL="myModel"
const val DEFAULT_REWARD = 30

class MyModel {
    lateinit var db: ItemCollectionDB
    lateinit var dao: ItemCollectionDAO

    fun initializeDB( _context: Context) {
         db = Room.databaseBuilder(_context, ItemCollectionDB::class.java, "collection_item")
             .fallbackToDestructiveMigration()
             .build()
         dao = db.itemCollectionDAO()
        return
    } // MakeItemListの前に実行
    fun getDayStringJp(backDate:Int): String {
        val local = Locale.JAPAN
        val pattern = DateFormat.getBestDateTimePattern(local, "YYYYEEEMMMd")
        val date = LocalDate.now().minusDays(backDate.toLong())
        val javaUtilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        return SimpleDateFormat(pattern, local).format(javaUtilDate)
    } // 0：本日　1～：backDate日前を返す｡
    fun getDayStringEn (backDate: Int):String{
        val date = LocalDate.now().minusDays(backDate.toLong())
        val javaUtilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        return  SimpleDateFormat("yyyy/M/d",Locale.ENGLISH).format(javaUtilDate)
    }
    fun getDayStringShort(backDate: Int):String{
        val date = LocalDate.now().minusDays(backDate.toLong())
        val javaUtilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        return  SimpleDateFormat("M/d", Locale.ENGLISH).format(javaUtilDate)
    }
     fun makeItemListFromResource(_context: Context): List<ItemEntity> {
        val itemsFromResource = _context.resources.getStringArray(R.array.default_item_list)
        return List(itemsFromResource.size) { index ->
            parseToItem(index, itemsFromResource[index])
        }
    }
    fun insertItem(itemEntity: ItemEntity){
        dao.insert(itemEntity)
    }

    private fun parseToItem(id:Int, _string: String): ItemEntity {
        // 入力： string = "title ; reward ; category ; finishedHistory"
        // 出力： storedItem (title,reward,category,finishedHistory )を返す｡
        val elementList = _string.split(";").toMutableList()
        // 文字列が規則に従っているか
        if (elementList.lastIndex<2) {
            Log.w(MY_MODEL,"Invalid String was passed parseToItem.")
            return ItemEntity(title = ERROR_TITLE)
        } // title, reward  Category がなければ追加せず

        val title = if(elementList[0].isBlank()) ERROR_TITLE else elementList[0].trim()
        val reward = if(elementList[1].isDigitsOnly())  elementList[1].toInt() else DEFAULT_REWARD
        val category = if(elementList[2].isBlank()) ERROR_CATEGORY else elementList[2].trim()

        return if (elementList.lastIndex == 2 ) {
            // historyが無い場合
            ItemEntity(id, title,reward,category,history = "")
        } else {
            // history がある場合
            if(elementList[3].matches("(20[0-9]{2}/([1-9]|1[0-2])/([1-9]|[12][0-9]|3[01]),?)+".toRegex())) {
                // 年：2000-2099 /月： 1～9 or 10～12/ 日： 1～9　or　10～29　or　30,31の要素が一つでもあればマッチ
                ItemEntity(id, title,reward,category,history = elementList[3])
            } else {
                // マッチしなければ空文字列をHistoryに返しておく
                ItemEntity(id,title,reward,category,history = "")
            }
        }
    }
    fun makeCategoryList( list:List<ItemEntity> ) : List<String>{
        val categoryList = List(list.size){index-> list[index].category}
        return categoryList.distinct()
    }
    fun loadRewardFromPreference(_context: Context):Int {
        val preferences = _context.getSharedPreferences(REWARD_HISTORY, Context.MODE_PRIVATE)
        return preferences?.getInt(REWARD_HISTORY, 0) ?: 0
    }
    fun saveRewardToPreference(reward: Int,_context: Context){
        val preferenceEditor = _context.getSharedPreferences(REWARD_HISTORY, Context.MODE_PRIVATE).edit()
        preferenceEditor.putInt(REWARD_HISTORY, reward)
        preferenceEditor.apply()
    }
    fun saveCurrentCategory(_category:String,_context: Context){
        val preferenceEditor = _context.getSharedPreferences(CURRENT_CATEGORY, Context.MODE_PRIVATE).edit()
        preferenceEditor.putString(CURRENT_CATEGORY,_category)
        preferenceEditor.apply()
    }
    fun saveCategories(list:List<String>, _context: Context){
        val string  = list.joinToString(",")
        val preferenceEditor = _context.getSharedPreferences(CATEGORY_LIST, Context.MODE_PRIVATE).edit()
        preferenceEditor.putString(CATEGORY_LIST,string)
        preferenceEditor.apply()
    }
    fun loadCategories(_context: Context) : List<String>{
        val preferences = _context.getSharedPreferences(CATEGORY_LIST, Context.MODE_PRIVATE)
        val categoryStr =  preferences?.getString(CATEGORY_LIST,"") ?: ""
        val list =  categoryStr.split(",")
        return if(list.isNullOrEmpty()) {
            Log.w(MY_MODEL,"category was empty")
            listOf(DEFAULT_CATEGORY)
        } else {
            list
        }
    }
    fun loadCategoryFromPreference(_context: Context):String {
        val preferences = _context.getSharedPreferences(CURRENT_CATEGORY, Context.MODE_PRIVATE)
        return preferences?.getString(CURRENT_CATEGORY, "") ?: ""
    }
    fun appendDateToItem(itemEntity: ItemEntity, dateStr:String) {
        val dateList = itemEntity.history.split(",").toMutableList()
        if(dateStr.matches("20[0-9]{2}/([1-9]|1[0-2])/([1-9]|[12][0-9]|3[01])".toRegex())){
            dateList.add(dateStr)
            dateList.sort()
            val newDateList = dateList.joinToString(",")
            itemEntity.history = newDateList
        } else {
            Log.w(MY_MODEL,"Appending $dateList of ${itemEntity.id} was fail")
        }
    }
    fun deleteDateFromItem(itemEntity:ItemEntity, dateStr: String){
        val dateList = itemEntity.history.split(",").toMutableList()
        if(dateList.contains(dateStr)){
            dateList.remove(dateStr)
            val newDateList = dateList.joinToString (",")
            itemEntity.history = newDateList
        } else {
            Log.w(MY_MODEL,"deleting $dateList of ${itemEntity.id} was fail")
        }

    }
}

