package io.terameteo.actionlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import io.terameteo.actionlist.databinding.ActivityScrollingBinding
import io.terameteo.actionlist.model.MyModel
import io.terameteo.actionlist.ui.MainFragmentDirections


class ScrollingActivity : AppCompatActivity() {

    private val  mViewModel: MainViewModel by lazy {
        val myModel = MyModel()
        myModel.initializeDB(this)
        val factory =  MainViewModel.Factory(myModel)
        ViewModelProvider(this,factory)[MainViewModel::class.java]
        }
    private lateinit var mBinding: ActivityScrollingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.initialize(this)
        mViewModel.currentPage.postValue(0)
        mBinding = ActivityScrollingBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        mBinding.toolbarLayout.title = title

        // コマンド処理
        mBinding.fab.setOnClickListener { view ->
            val destination = MainFragmentDirections.actionMainFragmentToDetailFragment(-1)
            findNavController(R.id.mainFragmentContainer).navigate(destination)
        }
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
            R.id.action_make_list_from_resource->{
                mViewModel.makeListFromResource(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

// ボタンのデフォルトの高さは48dp
// 低優先度で42pix   中優先度 60px   高優先度 72px
// ボタンの間隔は12～48px

