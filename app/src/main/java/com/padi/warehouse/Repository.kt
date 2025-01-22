package com.padi.warehouse

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.padi.warehouse.models.Item
import com.padi.warehouse.models.Response
import com.padi.warehouse.utils.findProductNameInSite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class Repository {
    private val user = Firebase.auth.currentUser
    private val database = Firebase.database
    private var listener: ValueEventListener? = null
    private var dbRef: DatabaseReference? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getItems(): Flow<Response<List<Item>>> {
        return callbackFlow {
            dbRef = user?.let { database.getReference("items").child(it.uid) }

            listener = dbRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    trySend(Response.Loading)
                    val items = mutableListOf<Item>()
                    snapshot.children.forEach { item ->
                        val itm = item.getValue(Item::class.java)
                        if (itm != null) {
                            itm.id = item.key
                            items.add(itm)
                        }
                    }
                    // Emit the user data to the flow
                    trySend(Response.Success(items))
                }

                override fun onCancelled(error: DatabaseError) {
                    trySend(Response.Error(error.message))
                    cancel()
                }

            })

            awaitClose {
                listener?.let {
                    dbRef?.removeEventListener(it)
                }
            }
        }
    }

    private fun findSameItem(item: Item, callback: (item: Item?) -> Unit) {
        val itemsRef = user?.let { database.getReference("items").child(it.uid) }
        itemsRef?.get()
            ?.addOnSuccessListener {
                var sameItem: Item? = null
                it.children.forEach { dbItem ->
                    val itm = dbItem.getValue(Item::class.java)
                    if (itm != null) {
                        itm.id = dbItem.key
                        // Check if item with same product name, expiration date and box exists
                        if (itm.name == item.name
                            && itm.box == item.box
                            && itm.expirationDate == item.expirationDate
                        ) {
                            sameItem = itm
                        }
                    }
                }
                callback(sameItem)
            }?.addOnFailureListener {
                callback(null)
            }
    }

    fun addNewItem(item: Item): Flow<Boolean> {
        return callbackFlow {
            // Check if same item exists in order to increase the amount
            findSameItem(item) { sameItem ->
                if (sameItem == null) {
                    val itemsRef = user?.let { database.getReference("items").child(it.uid) }

                    itemsRef?.push()?.setValue(item)
                        ?.addOnSuccessListener {
                            trySend(true)
                            cancel()
                        }
                        ?.addOnFailureListener {
                            trySend(false)
                            cancel()
                        }

                } else {
                    sameItem.amount = (sameItem.amount.toInt() + item.amount.toInt()).toString()
                    scope.launch {
                        updateItem(sameItem).collect {
                            trySend(it)
                        }
                    }
                }
            }

            awaitClose {
                channel.close()
            }
        }
    }

    fun updateItem(item: Item): Flow<Boolean> {
        val itemsRef = user?.let {
            item.id?.let { id ->
                database.getReference("items").child(it.uid).child(
                    id
                )
            }
        }

        return callbackFlow {
            itemsRef?.setValue(item)
                ?.addOnSuccessListener {
                    trySend(true)
                    cancel()
                }?.addOnFailureListener {
                    trySend(false)
                    cancel()
                }
            itemsRef?.child("id")?.removeValue()

            awaitClose {
                channel.close()
            }
        }
    }

    fun deleteItem(item: Item): Flow<Boolean> {
        val itemsRef = user?.let {
            item.id?.let { id ->
                database.getReference("items").child(it.uid).child(
                    id
                )
            }
        }

        return callbackFlow {
            itemsRef?.removeValue()
                ?.addOnSuccessListener {
                    trySend(true)
                    cancel()
                }?.addOnFailureListener {
                    trySend(false)
                    cancel()
                }

            awaitClose {
                channel.close()
            }
        }
    }

    fun signOut() {
        listener?.let {
            dbRef?.removeEventListener(it)
        }
    }

    fun findProductName(barcode: String): Flow<String> {
        return callbackFlow {
            val barcodesRef = user?.let { database.getReference("barcodes").child(barcode) }

            val listener = barcodesRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        trySend(snapshot.value as String)
                    } else {
                        // Search for product description in i520 service
                        scope.launch {
                            val productName = findProductNameInSite(barcode)
                            if (productName.isNotEmpty()) {
                                trySend(productName)
                                // Store description in database
                                val barcodeRef = database.getReference("barcodes")
                                barcodeRef.child(barcode).setValue(productName)
                            } else {
                                trySend("")
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cancel()
                }

            })

            awaitClose {
                listener?.let {
                    barcodesRef.removeEventListener(it)
                }
            }
        }
    }

    fun addProductName(barcode: String, productName: String) {
        val barcodesRef = database.getReference("barcodes")
        barcodesRef.child(barcode).setValue(productName)
    }
}
