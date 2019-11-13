package com.padi.warehouse

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.padi.warehouse.item.Item
import com.padi.warehouse.item.ItemAdapter
import com.padi.warehouse.item.ItemDetails
import kotlinx.android.synthetic.main.activity_search_results.*

class SearchResults : AppCompatActivity() {
    private lateinit var mResults: List<Item>
    private lateinit var mQuery: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                searchItems(query)
            }
        }
    }

    private fun searchItems(query: String) {
        mQuery = query
        mResults = items.filter {
            it.name!!.contains(query, true)
        }

        val itemViewAdapter = ItemAdapter(mResults) { itm: Item -> itemClicked(itm) }

        rvResults.setHasFixedSize(true)
        rvResults.layoutManager = LinearLayoutManager(this@SearchResults)
        rvResults.adapter = itemViewAdapter
    }

    private fun itemClicked(itm: Item) {
        val intent = Intent(this, ItemDetails::class.java)
        val bundle = Bundle()
        bundle.putParcelable(MSG.ITEM.message, itm)
        intent.putExtras(bundle)
        startActivityForResult(intent, RC.ITEM.code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == RC.ITEM.code) {
            mResults = items.filter {
                it.name!!.contains(mQuery, true)
            }
            val itemViewAdapter = ItemAdapter(mResults) { itm: Item -> itemClicked(itm) }
            rvResults.adapter = itemViewAdapter
            rvResults.adapter?.notifyDataSetChanged()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
