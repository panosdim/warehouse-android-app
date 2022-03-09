package com.padi.warehouse.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.padi.warehouse.R
import com.padi.warehouse.databinding.ItemRowBinding
import com.padi.warehouse.model.Item
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class ItemAdapter(private val itemsList: List<Item>, private val clickListener: (Item) -> Unit) :
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
        with(holder) {
            with(itemsList[position]) {
                binding.name.text = name
                binding.amount.text = "${itemView.context.getString(R.string.amount)}: ${amount}"
                binding.expDate.text = exp_date
                binding.box.text = "${itemView.context.getString(R.string.box)}: ${box}"
                binding.cvItem.setCardBackgroundColor(
                    ResourcesCompat.getColor(
                        itemView.resources,
                        R.color.white,
                        null
                    )
                )
                if (!exp_date.isNullOrEmpty()) {
                    val date = LocalDate.parse(exp_date)
                    binding.expDate.text = date.format(dateFormatter)

                    if (date.isBefore(today)) {
                        binding.cvItem.setCardBackgroundColor(
                            ResourcesCompat.getColor(
                                itemView.resources,
                                R.color.red,
                                null
                            )
                        )
                    }

                    if ((date.isBefore(nextMonthLastDay) || date.isEqual(nextMonthLastDay)) && date.isAfter(
                            today
                        )
                    ) {
                        binding.cvItem.setCardBackgroundColor(
                            ResourcesCompat.getColor(
                                itemView.resources,
                                R.color.yellow,
                                null
                            )
                        )
                    }
                }
                holder.itemView.setOnClickListener { clickListener(this) }
            }
        }
    }

    override fun getItemCount() = itemsList.size

    inner class ItemViewHolder(val binding: ItemRowBinding) :
        RecyclerView.ViewHolder(binding.root)
}