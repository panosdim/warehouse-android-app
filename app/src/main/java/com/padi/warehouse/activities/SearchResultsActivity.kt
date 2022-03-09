package com.padi.warehouse.activities

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.padi.warehouse.MSG
import com.padi.warehouse.adapters.ItemAdapter
import com.padi.warehouse.databinding.ActivitySearchResultsBinding
import com.padi.warehouse.items
import com.padi.warehouse.model.Item
import com.padi.warehouse.utils.unaccent

class SearchResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchResultsBinding
    private lateinit var results: List<Item>
    private lateinit var query: String
    private val searchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        this.onSearchResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Verify the action and get the query
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                searchItems(query)
            }
        }
    }

    private fun searchItems(query: String) {
        this.query = query.unaccent().trim()
        results = items.filter {
            it.name.toString().unaccent().contains(this.query, true)
        }

        val itemViewAdapter = ItemAdapter(results) { itm: Item -> itemClicked(itm) }

        binding.rvResults.setHasFixedSize(true)
        binding.rvResults.layoutManager = LinearLayoutManager(this@SearchResultsActivity)
        binding.rvResults.adapter = itemViewAdapter

        if (results.isNotEmpty()) {
            binding.rvResults.isVisible = true
            binding.txtEmptyResults.isVisible = false

        } else {
            binding.rvResults.isVisible = false
            binding.txtEmptyResults.isVisible = true
        }

    }

    private fun itemClicked(itm: Item) {
        val intent = Intent(this, ItemDetailsActivity::class.java)
        val bundle = Bundle()
        bundle.putParcelable(MSG.ITEM.message, itm)
        intent.putExtras(bundle)
        searchLauncher.launch(intent)
    }

    private fun onSearchResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            results = items.filter {
                it.name.toString().unaccent().contains(this.query, true)
            }
            val itemViewAdapter = ItemAdapter(results) { itm: Item -> itemClicked(itm) }
            binding.rvResults.adapter = itemViewAdapter
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
