package io.terameteo.actionlist.model


import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ItemCollectionDAO {
    /** 全データ取得 */
    @Query("SELECT * FROM collection_item")
    fun getAll(): LiveData<List<ItemEntity>>
    /** データ更新 */
    @Update
    fun update(item: ItemEntity)
    /** データ追加 */
    @Insert (onConflict = OnConflictStrategy.REPLACE) // 同じアイテムを追加すると上書き
    fun insert(item: ItemEntity)
    /** データ削除 */
    @Delete
    fun delete(item: ItemEntity)

}
@Database(entities = [ItemEntity::class], version = 2) // 使うentityのクラスを渡す｡
abstract class ItemCollectionDB : RoomDatabase() {
    abstract fun itemCollectionDAO(): ItemCollectionDAO // 上記Interfaceの抽象メソッドを含む
}

@Entity (tableName = "collection_item")
data class ItemEntity(
    @PrimaryKey @ColumnInfo(name = "id", index = true) var id:Int = 0,
    @ColumnInfo(name = "title") var title: String = "unnamed",
    @ColumnInfo(name = "reward") var reward: Int = 30,
    @ColumnInfo(name = "category") var category: String = "",
    @ColumnInfo(name = "should_do_today") var shouldDoToday: Boolean = true,
    @ColumnInfo(name = "finished_history")  var finishedHistory: String = ""
)
// 拡張関数として静的(Static)メソッドを宣言

fun ItemEntity.isDoneAt(dateStr: String): Boolean { // Str yyyy/mm/ddがFinished Historyに含まれればTRUE､なければFalse
    return dateStr.toRegex().containsMatchIn(this.finishedHistory)
}