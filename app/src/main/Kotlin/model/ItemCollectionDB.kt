package io.terameteo.actionlist.model


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*

// Livedataで返す様にすると､ スレッドを分けなくてもよくなる｡

const val DEFAULT_CATEGORY= "daily"

@Dao
interface ItemCollectionDAO {
    /** 全データ取得 */
    @Query("SELECT * FROM collection_item")
    fun getAll(): LiveData<List<ItemEntity>>
    @Query("SELECT * FROM collection_item WHERE category = :category")
    fun getByCategory(category: String): LiveData<List<ItemEntity>>


    /** データ更新 */
    @Update
    fun update(item: ItemEntity)
    @Update
    fun updateList(list:List<ItemEntity>)

    /** データ追加 */
    @Insert (onConflict = OnConflictStrategy.REPLACE) // 同じアイテムを追加すると上書き
    fun insert(item: ItemEntity)
    /** データ削除 */
    @Delete
    fun delete(item: ItemEntity)

}
@Database(entities = [ItemEntity::class], version = 1) // 使うentityのクラスを渡す｡
abstract class ItemCollectionDB : RoomDatabase() {
    abstract fun itemCollectionDAO(): ItemCollectionDAO // 上記Interfaceの抽象メソッドを含む
}

@Entity (tableName = "collection_item")
data class ItemEntity(
    @PrimaryKey @ColumnInfo(name = "id", index = true) var id:Int = 0,
    @ColumnInfo(name = "title") var title: String = "unnamed",
    @ColumnInfo(name = "reward") var reward: Int = 30,
    @ColumnInfo(name = "category") var category: String = "",
    @ColumnInfo(name = "history")  var history: String = ""
)
// 拡張関数として静的(Static)メソッドを宣言

fun ItemEntity.isDoneAt(dateStr: String): Boolean { // Str yyyy/mm/ddがFinished Historyに含まれればTRUE､なければFalse
    return dateStr.toRegex().containsMatchIn(this.history)
}
fun ItemEntity.appendDate(dateStr: String){
    val dateList = this.history.split(",").toMutableList()
    if(dateStr.matches("20[0-9]{2}/([1-9]|1[0-2])/([1-9]|[12][0-9]|3[01])".toRegex())){
        dateList.add(dateStr)
        dateList.sort()
        val newDateList = dateList.joinToString(",")
        this.history = newDateList
    } else {
        Log.w("ItemEntity","Appending $dateStr of $id was fail")
    }
    return
}