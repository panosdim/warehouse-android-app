package com.padi.warehouse.activities

import android.Manifest
import android.app.DownloadManager
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.padi.warehouse.*
import com.padi.warehouse.adapters.ItemAdapter
import com.padi.warehouse.databinding.ActivityMainBinding
import com.padi.warehouse.dialogs.ItemsSortDialog
import com.padi.warehouse.model.Item
import com.padi.warehouse.utils.ExtendedFloatingActionButtonScrollListener
import com.padi.warehouse.utils.checkForNewVersion
import com.padi.warehouse.utils.createNotificationChannel
import com.padi.warehouse.utils.refId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var onComplete: BroadcastReceiver
    private lateinit var manager: DownloadManager
    private lateinit var itemsSortDialog: ItemsSortDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        createNotificationChannel(this)
        FirebaseApp.initializeApp(this)

        binding.progressBar.visibility = View.VISIBLE

        val itemsRef = database.getReference("items").child(user?.uid.toString())

        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                // Not used
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val itemViewAdapter = ItemAdapter(items) { itm: Item -> itemClicked(itm) }

                binding.rvItems.setHasFixedSize(true)
                binding.rvItems.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.rvItems.adapter = itemViewAdapter
                binding.rvItems.addOnScrollListener(
                    ExtendedFloatingActionButtonScrollListener(
                        binding.newItem
                    )
                )
                binding.progressBar.visibility = View.GONE
                itemsSortDialog = ItemsSortDialog(binding.rvItems.adapter)
                val adapter = binding.rvItems.adapter as ItemAdapter
                adapter.sortItems()
            }
        })

        itemsRef.orderByChild("exp_date").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
                // Not used
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                // Not used
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val item = dataSnapshot.getValue(Item::class.java)
                if (item != null) {
                    item.id = dataSnapshot.key
                    val index = items.indexOfFirst { itm -> itm.id == item.id }
                    items[index] = item
                    val adapter = binding.rvItems.adapter as ItemAdapter
                    adapter.sortItems()
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val item = dataSnapshot.getValue(Item::class.java)
                if (item != null) {
                    item.id = dataSnapshot.key
                    val index = items.indexOf(item)
                    items.remove(item)
                    binding.rvItems.adapter?.notifyItemRemoved(index)
                }
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val item = dataSnapshot.getValue(Item::class.java)
                if (item != null) {
                    item.id = dataSnapshot.key
                    items.add(item)
                    val adapter = binding.rvItems.adapter as ItemAdapter
                    adapter.sortItems()
                }
            }
        })

        binding.newItem.setOnClickListener {
            val intent = Intent(this, ItemDetailsActivity::class.java)
            startActivity(intent)
        }

        // Check for permissions to download new version of the app
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                RC.PERMISSION_REQUEST.code
            )
        }

        // Check for expired items
        val itemExpiredBuilder =
            PeriodicWorkRequestBuilder<ExpiredItemsWorker>(30, TimeUnit.DAYS)

        val itemExpiredWork = itemExpiredBuilder.build()
        // Then enqueue the recurring task:
        WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork(
            "itemExpired",
            ExistingPeriodicWorkPolicy.KEEP,
            itemExpiredWork
        )

        manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Check for new version
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            checkForNewVersion(this@MainActivity)
        }

        onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val referenceId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (referenceId != -1L && referenceId == refId) {
                    val apkUri = manager.getUriForDownloadedFile(refId)
                    val installIntent = Intent(Intent.ACTION_VIEW)
                    installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                    installIntent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    startActivity(installIntent)
                }

            }
        }

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun itemClicked(itm: Item) {
        val intent = Intent(this, ItemDetailsActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable(MSG.ITEM.message, itm)
        intent.putExtra(MSG.ITEM.message, itm)
        startActivity(intent)
    }

    override fun onDestroy() {
        items.clear()
        unregisterReceiver(onComplete)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            inputType = EditorInfo.TYPE_TEXT_FLAG_CAP_CHARACTERS
            val sv = this
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(p0: String?): Boolean {
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    sv.setQuery("", false)
                    sv.isIconified = true
                    menu.findItem(R.id.app_bar_search).collapseActionView()
                    return false
                }
            })
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    user = null
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            true
        }
        R.id.action_sort -> {
            itemsSortDialog.showNow(supportFragmentManager, ItemsSortDialog.TAG)
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
