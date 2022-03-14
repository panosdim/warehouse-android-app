package com.padi.warehouse.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.padi.warehouse.R
import com.padi.warehouse.databinding.ItemRowBinding
import com.padi.warehouse.model.Item
import com.padi.warehouse.utils.*
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


class ItemAdapter(
    private val itemsList: MutableList<Item>,
    private val clickListener: (Item) -> Unit
) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    private val today = now()
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val nextMonthLastDay = today.with(TemporalAdjusters.firstDayOfNextMonth())
        .with(TemporalAdjusters.lastDayOfMonth())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemRowBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (position + 1 == itemCount) {
            // It is the last item of the list
            // Set bottom margin
            setBottomMargin(
                holder.itemView,
                (64 * Resources.getSystem().displayMetrics.density).toInt()
            )
        } else {
            // Reset bottom margin
            setBottomMargin(holder.itemView, 0)
        }

        with(holder) {
            with(itemsList[position]) {
                binding.name.text = name
                binding.amount.text =
                    itemView.context.getString(R.string.amount_with_number, amount)
                binding.expDate.text = exp_date
                binding.box.text = itemView.context.getString(R.string.box_with_number, box)

                // Change color on Expired and Expires Soon items
                binding.expDate.setTextColor(itemView.context.resolveColorAttr(R.attr.colorOnSurface))
                binding.name.setTextColor(itemView.context.resolveColorAttr(R.attr.colorOnSurface))

                if (!exp_date.isNullOrEmpty()) {
                    val date = LocalDate.parse(exp_date)
                    binding.expDate.text = date.format(dateFormatter)

                    if (date.isBefore(today)) {
                        binding.expDate.setTextColor(itemView.context.resolveColorAttr(R.attr.colorExpired))
                        binding.name.setTextColor(itemView.context.resolveColorAttr(R.attr.colorExpired))
                    }

                    if ((date.isBefore(nextMonthLastDay) || date.isEqual(nextMonthLastDay))
                        && date.isAfter(today)
                    ) {
                        binding.expDate.setTextColor(itemView.context.resolveColorAttr(R.attr.colorExpiresSoon))
                        binding.name.setTextColor(itemView.context.resolveColorAttr(R.attr.colorExpiresSoon))
                    }
                }
                holder.itemView.setOnClickListener { clickListener(this) }
            }
        }
    }

    /**
     * Sets a margin to the bottom of the view.
     *
     * @param view         The view to add the margin to.
     * @param bottomMargin The bottom margin to be added to the view.
     */
    private fun setBottomMargin(view: View, bottomMargin: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, bottomMargin)
            view.requestLayout()
        }
    }

    private fun updateItemsList(items: List<Item>) {
        val diffCallback = ItemDiffCallback(this.itemsList, items)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.itemsList.clear()
        this.itemsList.addAll(items)
        diffResult.dispatchUpdatesTo(this)
    }

    fun sortItems() {
        val sortedItems = itemsList.map { it.copy() }.toMutableList()
        when (sortField) {
            SortField.NAME -> {
                when (sortDirection) {
                    SortDirection.ASC -> sortedItems.sortBy { it.name }
                    SortDirection.DESC -> sortedItems.sortByDescending { it.name }
                }
            }

            SortField.EXP_DATE -> {
                when (sortDirection) {
                    SortDirection.ASC -> sortedItems.sortWith(expDateAsc)
                    SortDirection.DESC -> sortedItems.sortWith(expDateDesc)
                }
            }

            SortField.BOX -> {
                when (sortDirection) {
                    SortDirection.ASC -> sortedItems.sortBy { it.box }
                    SortDirection.DESC -> sortedItems.sortByDescending { it.box }
                }
            }
        }
        updateItemsList(sortedItems)
    }

    override fun getItemCount() = itemsList.size

    inner class ItemViewHolder(val binding: ItemRowBinding) :
        RecyclerView.ViewHolder(binding.root)
}