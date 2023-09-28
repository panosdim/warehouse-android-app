package com.padi.warehouse.viewmodels

import androidx.lifecycle.ViewModel
import com.padi.warehouse.Repository
import com.padi.warehouse.models.Item
import kotlinx.coroutines.flow.Flow

class MainViewModel : ViewModel() {
    private val repository = Repository()
    val items = repository.getItems()

    fun addNewItem(item: Item): Flow<Boolean> {
        return repository.addNewItem(item)
    }

    fun updateItem(item: Item): Flow<Boolean> {
        return repository.updateItem(item)
    }

    fun deleteItem(item: Item): Flow<Boolean> {
        return repository.deleteItem(item)
    }

    fun signOut() {
        repository.signOut()
    }

    fun findProductName(barcode: String): Flow<String> {
        return repository.findProductName(barcode)
    }

    fun addProductName(barcode: String, productName: String) {
        return repository.addProductName(barcode, productName)
    }
}