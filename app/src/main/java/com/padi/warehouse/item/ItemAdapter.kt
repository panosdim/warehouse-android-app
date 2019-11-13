package com.padi.warehouse.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.padi.warehouse.R
import kotlinx.android.synthetic.main.item_row.view.*
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class ItemAdapter(private val itemsList: List<Item>, private val clickListener: (Item) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // LayoutInflater: takes ID from layout defined in XML.
        // Instantiates the layout XML into corresponding View objects.
        // Use context from main app -> also supplies theme layout values!
        val inflater = LayoutInflater.from(parent.context)
        // Inflate XML. Last parameter: don't immediately attach new view to the parent view group
        val view = inflater.inflate(R.layout.item_row, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Populate ViewHolder with data that corresponds to the position in the list
        // which we are told to load
        (holder as IncomeViewHolder).bind(itemsList[position], clickListener)
    }

    override fun getItemCount() = itemsList.size

    class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val today = now()
        private val dateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        private val nextMonthLastDay = today.with(TemporalAdjusters.firstDayOfNextMonth()).with(TemporalAdjusters.lastDayOfMonth())

        @SuppressLint("SetTextI18n")
        fun bind(itm: Item, clickListener: (Item) -> Unit) {
            itemView.name.text = itm.name
            itemView.amount.text = "${itemView.context.getString(R.string.amount)}: ${itm.amount}"
            itemView.expDate.text = itm.exp_date
            itemView.box.text = "${itemView.context.getString(R.string.box)}: ${itm.box}"
            itemView.cvItem.setCardBackgroundColor(ResourcesCompat.getColor(itemView.resources, R.color.white, null))
            if (itm.exp_date!!.isNotEmpty()) {
                val date = LocalDate.parse(itm.exp_date)
                itemView.expDate.text = date.format(dateFormatter)

                if (date.isBefore(today)) {
                    itemView.cvItem.setCardBackgroundColor(ResourcesCompat.getColor(itemView.resources, R.color.red, null))
                }

                if ((date.isBefore(nextMonthLastDay) || date.isEqual(nextMonthLastDay)) && date.isAfter(today)) {
                    itemView.cvItem.setCardBackgroundColor(ResourcesCompat.getColor(itemView.resources, R.color.yellow, null))
                }
            }
            itemView.setOnClickListener { clickListener(itm) }
        }
    }
}