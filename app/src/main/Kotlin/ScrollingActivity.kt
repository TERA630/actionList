package io.terameteo.actionlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.terameteo.actionlist.databinding.ActivityScrollingBinding
import io.terameteo.actionlist.ui.MainFragment

const val MAIN_WINDOW = "mainWindow"
const val DETAIL_WINDOW = "detailWindow"
const val HISTORY_WINDOW = "historyWindow"

class ScrollingActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels() // activity-ktx

    private lateinit var binding: ActivityScrollingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(this)
        binding = ActivityScrollingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = title
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        wakeMainFragment()
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
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun wakeMainFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragmentOrNull =
            supportFragmentManager.findFragmentByTag(MAIN_WINDOW) as MainFragment?

        if (fragmentOrNull == null) {
            // Fragmentがまだインスタンス化されてなければ(初回起動)
            val fragment = MainFragment.newInstance(9)
            transaction.add(fragment, MAIN_WINDOW)
            transaction.replace(R.id.baseFrame,fragment)
            transaction.addToBackStack(null)
        } else {
            // detailFragmentがインスタンス化されていたら
            if (fragmentOrNull.isVisible) {
                transaction.hide(fragmentOrNull)
            } else {
                transaction.show(fragmentOrNull)
            }
        }
        transaction.commit()
    }
}

// if ( arg == null) {} else {}
// arg ?.let ?:run {}
