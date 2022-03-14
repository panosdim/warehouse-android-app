package com.padi.warehouse.utils

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.padi.warehouse.model.Item


class ItemDiffCallback(oldItemsList: List<Item>, newItemsList: List<Item>) :
    DiffUtil.Callback() {
    private val mOldItemsList: List<Item> = oldItemsList
    private val mNewItemsList: List<Item> = newItemsList
    override fun getOldListSize(): Int {
        return mOldItemsList.size
    }

    override fun getNewListSize(): Int {
        return mNewItemsList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldItemsList[oldItemPosition].id === mNewItemsList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem: Item = mOldItemsList[oldItemPosition]
        val newItem: Item = mNewItemsList[newItemPosition]
        return oldItem.name.equals(newItem.name) && oldItem.exp_date.equals(newItem.exp_date) && oldItem.amount.equals(
            newItem.amount
        ) && oldItem.box.equals(newItem.box)
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}