package io.terameteo.actionlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import io.terameteo.actionlist.databinding.ActivityScrollingBinding
import io.terameteo.actionlist.ui.MainFragment

const val MAIN_WINDOW = "mainWindow"
const val DETAIL_WINDOW = "detailWindow"
const val HISTORY_WINDOW = "historyWindow"
const val CATEGORY_WINDOW = "categoryWindow"

class ScrollingActivity : AppCompatActivity() {
    private val mViewModel: MainViewModel by viewModels() // activity-ktx
    private lateinit var mBinding: ActivityScrollingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.initialize(this)
        mViewModel.currentPage.postValue(9)
        mBinding = ActivityScrollingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        mBinding.toolbarLayout.title = title

        // コマンド処理
        mBinding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        wakeMainFragment()
        //　データ更新時の処理
        mViewModel.currentRewardStr.observe(this){
            mBinding.rewardText.text = it
        }

    }

    override fun onPause() {
        mViewModel.stateSave(this)
        super.onPause()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_make_category_list->{
                findNavController(R.id.mainFragmentContainer).navigate(R.id.action_mainFragment_to_categoryFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun wakeMainFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragmentOrNull =
            supportFragmentManager.findFragmentByTag(MAIN_WINDOW)

        if (fragmentOrNull == null) {
            // Fragmentがまだインスタンス化されてなければ(初回起動)
            val fragment = MainFragment()
            transaction.add(R.id.baseFrame,fragment)
        } else {
            transaction.replace(R.id.baseFrame,fragmentOrNull)
        }
        transaction.commit()
    }
}

// ボタンのデフォルトの高さは48dp
// 低優先度で42pix   中優先度 60px   高優先度 72px
// ボタンの間隔は12～48px

