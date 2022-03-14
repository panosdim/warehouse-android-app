package com.padi.warehouse.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.padi.warehouse.R
import com.padi.warehouse.adapters.ItemAdapter
import com.padi.warehouse.databinding.DialogItemsSortBinding
import com.padi.warehouse.utils.SortDirection
import com.padi.warehouse.utils.SortField
import com.padi.warehouse.utils.sortDirection
import com.padi.warehouse.utils.sortField

class ItemsSortDialog(
    private val itemsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?
) : BottomSheetDialogFragment() {
    private var _binding: DialogItemsSortBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogItemsSortBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.rgField.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbExpDate -> sortField = SortField.EXP_DATE
                R.id.rbName -> sortField = SortField.NAME
                R.id.rbBox -> sortField = SortField.BOX
            }
            val adapter = itemsAdapter as ItemAdapter
            adapter.sortItems()
        }

        binding.rgDirection.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            when (checkedRadioButtonId) {
                R.id.rbAscending -> sortDirection = SortDirection.ASC
                R.id.rbDescending -> sortDirection = SortDirection.DESC
            }
            val adapter = itemsAdapter as ItemAdapter
            adapter.sortItems()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ItemsSortDialog"
    }
}