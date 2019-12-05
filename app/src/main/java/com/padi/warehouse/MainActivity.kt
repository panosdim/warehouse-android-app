package com.padi.warehouse

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
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
import com.padi.warehouse.item.Item
import com.padi.warehouse.item.ItemAdapter
import com.padi.warehouse.item.ItemDetails
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection


class MainActivity : AppCompatActivity() {
    private val mExpDateAsc = Comparator<Item> { p1, p2 ->
        when {
            p1.exp_date.isNullOrEmpty() -> 1
            p2.exp_date.isNullOrEmpty() -> -1
            p1.exp_date!! > p2.exp_date!! -> 1
            p1.exp_date == p2.exp_date -> 0
            else -> -1
        }
    }

    private val mExpDateDesc = Comparator<Item> { p1, p2 ->
        when {
            p1.exp_date.isNullOrEmpty() -> -1
            p2.exp_date.isNullOrEmpty() -> 1
            p1.exp_date!! > p2.exp_date!! -> -1
            p1.exp_date == p2.exp_date -> 0
            else -> -1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        progressBar.visibility = View.VISIBLE

        val itemsRef = database.getReference("items").child(user?.uid!!)

        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                // Not used
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val itemViewAdapter = ItemAdapter(items) { itm: Item -> itemClicked(itm) }

                rvItems.setHasFixedSize(true)
                rvItems.layoutManager = LinearLayoutManager(this@MainActivity)
                rvItems.adapter = itemViewAdapter
                progressBar.visibility = View.GONE
                sortItems()
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
                val item = dataSnapshot.getValue<Item>(Item::class.java)
                item?.id = dataSnapshot.key
                val index = items.indexOfFirst { itm -> itm.id == item!!.id }
                items[index] = item!!
                sortItems()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val item = dataSnapshot.getValue<Item>(Item::class.java)
                item?.id = dataSnapshot.key
                items.remove(item)
                rvItems.adapter?.notifyDataSetChanged()
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val item = dataSnapshot.getValue<Item>(Item::class.java)
                item?.id = dataSnapshot.key
                items.add(item!!)
                sortItems()
            }
        })

        // Listeners for Items sort
        rgField.setOnCheckedChangeListener { _, _ ->
            sortItems()
        }

        rgDirection.setOnCheckedChangeListener { _, _ ->
            sortItems()
        }

        fab.setOnClickListener {
            val intent = Intent(this, ItemDetails::class.java)
            startActivityForResult(intent, RC.ITEM.code)
        }

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    RC.PERMISSION_REQUEST.code)
        }

        // Check for expired items
        val itemExpiredBuilder =
                PeriodicWorkRequestBuilder<ExpiredItemsWorker>(30, TimeUnit.DAYS)

        val itemExpiredWork = itemExpiredBuilder.build()
        // Then enqueue the recurring task:
        WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork("itemExpired", ExistingPeriodicWorkPolicy.KEEP, itemExpiredWork)

        GlobalScope.launch {
            checkForNewVersion()

        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkForNewVersion() {
        val url: URL
        val response: String
        try {
            url = URL("https://warehouse.cc.nf/api/v1/output.json")

            val conn = url.openConnection() as HttpURLConnection

            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.doOutput = false

            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")

            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val version = Version(JSONArray(response).getJSONObject(0).getJSONObject("apkData").getString("versionName"))
                val appVersion = Version(packageManager.getPackageInfo(packageName, 0).versionName)
                if (version.isGreater(appVersion) && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    downloadNewVersion()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadNewVersion() {
        val url: URL
        try {

            var destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canonicalPath + "/"
            val fileName = "Warehouse.apk"
            destination += fileName

            //Delete update file if exists
            val file = File(destination)
            if (file.exists())
                file.delete()

            url = URL("https://warehouse.cc.nf/api/v1/app-release.apk")

            val conn = url.openConnection() as HttpURLConnection

            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"

            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                conn.inputStream.use { input ->
                    File(destination).outputStream().use { fileOut ->
                        input.copyTo(fileOut)
                    }
                }

                val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val apk = File(downloads, fileName)
                val apkUri = Uri.fromFile(apk)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun itemClicked(itm: Item) {
        val intent = Intent(this, ItemDetails::class.java)
        val bundle = Bundle()
        bundle.putParcelable(MSG.ITEM.message, itm)
        intent.putExtra(MSG.ITEM.message, itm)
        startActivityForResult(intent, RC.ITEM.code)
    }

    override fun onDestroy() {
        items.clear()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.app_bar_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            val sv = this
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(p0: String?): Boolean {
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    sv.setQuery("", false)
                    sv.isIconified = true
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
                        val intent = Intent(this, Login::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
            true
        }
        R.id.action_sort -> {
            sortItems.visibility = if (sortItems.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun sortItems() {
        when (rgField.checkedRadioButtonId) {
            R.id.rbName -> {
                when (rgDirection.checkedRadioButtonId) {
                    R.id.rbAscending -> {
                        items.sortBy { it.name }
                    }
                    R.id.rbDescending -> {
                        items.sortByDescending { it.name }
                    }
                }
            }

            R.id.rbExpDate -> {
                when (rgDirection.checkedRadioButtonId) {
                    R.id.rbAscending -> {
                        items.sortWith(mExpDateAsc)
                    }
                    R.id.rbDescending -> {
                        items.sortWith(mExpDateDesc)
                    }
                }
            }

            R.id.rbBox -> {
                when (rgDirection.checkedRadioButtonId) {
                    R.id.rbAscending -> {
                        items.sortBy { it.box }
                    }
                    R.id.rbDescending -> {
                        items.sortByDescending { it.box }
                    }
                }
            }
        }
        rvItems.adapter?.notifyDataSetChanged()
    }
}
