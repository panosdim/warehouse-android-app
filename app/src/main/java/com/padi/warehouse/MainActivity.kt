package com.padi.warehouse

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
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


class MainActivity : AppCompatActivity() {
    val mItemsOrder = Comparator<Item> { p1, p2 ->
        when {
            p1.exp_date.isNullOrEmpty() -> 1
            p2.exp_date.isNullOrEmpty() -> -1
            p1.exp_date!! > p2.exp_date!! -> 1
            p1.exp_date == p2.exp_date -> 0
            else -> -1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        progressBar.visibility = View.VISIBLE

        val itemsRef = database.getReference("items").child(user?.uid!!)

        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                items.sortWith(mItemsOrder)
                val itemViewAdapter = ItemAdapter(items) { itm: Item -> itemClicked(itm) }

                rvItems.setHasFixedSize(true)
                rvItems.layoutManager = LinearLayoutManager(this@MainActivity)
                rvItems.adapter = itemViewAdapter
                progressBar.visibility = View.GONE
            }

        })

        itemsRef.orderByChild("exp_date").addChildEventListener(object : ChildEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, prevChildKey: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, prevChildKey: String?) {
                val item = dataSnapshot.getValue<Item>(Item::class.java)
                item?.id = dataSnapshot.key
                val index = items.indexOfFirst { itm -> itm.id == item!!.id }
                items[index] = item!!
                items.sortWith(mItemsOrder)
                rvItems.adapter?.notifyDataSetChanged()
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
                items.sortWith(mItemsOrder)
                rvItems.adapter?.notifyDataSetChanged()
            }
        })

        fab.setOnClickListener {
            val intent = Intent(this, ItemDetails::class.java)
            startActivityForResult(intent, RC.ITEM.code)
        }

    }

    private fun itemClicked(itm: Item) {
        val intent = Intent(this, ItemDetails::class.java)
        val bundle = Bundle()
        bundle.putParcelable(MSG.ITEM.message, itm)
        intent.putExtras(bundle)
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

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
