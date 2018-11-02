package com.padi.warehouse

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.FirebaseApp
import com.padi.warehouse.item.ItemDetails
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseError
import com.padi.warehouse.item.Item
import com.google.firebase.FirebaseError
import com.google.firebase.database.ValueEventListener
import com.padi.warehouse.item.ItemAdapter


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        progressBar.visibility = View.VISIBLE

        val itemsRef = database.getReference("items").child(user?.uid!!)

        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                items.sortWith(Comparator<Item> { p1, p2 ->
                    when {
                        p1.exp_date.isNullOrEmpty() -> 1
                        p2.exp_date.isNullOrEmpty() -> -1
                        p1.exp_date!! > p2.exp_date!! -> 1
                        p1.exp_date == p2.exp_date -> 0
                        else -> -1
                    }
                })
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
                items.sortWith(Comparator<Item> { p1, p2 ->
                    when {
                        p1.exp_date.isNullOrEmpty() -> 1
                        p2.exp_date.isNullOrEmpty() -> -1
                        p1.exp_date!! > p2.exp_date!! -> 1
                        p1.exp_date == p2.exp_date -> 0
                        else -> -1
                    }
                })
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
                items.sortWith(Comparator<Item> { p1, p2 ->
                    when {
                        p1.exp_date.isNullOrEmpty() -> 1
                        p2.exp_date.isNullOrEmpty() -> -1
                        p1.exp_date!! > p2.exp_date!! -> 1
                        p1.exp_date == p2.exp_date -> 0
                        else -> -1
                    }
                })
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC.ITEM.code) {
                // TODO: Check if needed
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
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

        R.id.app_bar_search -> {
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
