package com.padi.warehouse.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.padi.warehouse.*
import com.padi.warehouse.databinding.ActivityItemDetailsBinding
import com.padi.warehouse.databinding.AddProductDescriptionBinding
import com.padi.warehouse.model.Item
import com.padi.warehouse.utils.DecimalDigitsInputFilter
import com.padi.warehouse.utils.dateFormatter
import com.padi.warehouse.utils.findProductDescription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters.lastDayOfMonth
import kotlin.math.roundToInt


class ItemDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemDetailsBinding
    private var item = Item(name = "", exp_date = "", amount = "1", box = "")
    private val bundle: Bundle? by lazy { intent.extras }
    private lateinit var datePickerDialog: DatePickerDialog
    private val scanOptions = ScanOptions()
    private val scope = CoroutineScope(Dispatchers.Main)

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            Toast.makeText(
                this@ItemDetailsActivity,
                "Searching for product ${result.contents}.",
                Toast.LENGTH_LONG
            ).show()

            searchForProduct(result.contents)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (bundle != null) {
            item = bundle!!.getParcelable<Parcelable>(MSG.ITEM.message) as Item
            binding.saveItem.isVisible = true
            binding.saveItem.setText(R.string.update)
            binding.deleteItem.isVisible = true
        } else {
            binding.saveItem.isVisible = true
            binding.saveItem.setText(R.string.save)
            binding.deleteItem.isVisible = false
        }

        binding.tlProductName.setEndIconOnClickListener {
            // Initiate scan with zxing custom scan activity
            scanOptions.captureActivity = BarcodeScanActivity::class.java
            barcodeLauncher.launch(scanOptions)
        }

        binding.tlExpDate.setEndIconOnClickListener {
            // Use the date from the TextView
            val date: LocalDate = try {
                LocalDate.parse(binding.tvExpDate.text.toString())
            } catch (ex: DateTimeParseException) {
                LocalDate.now()
            }

            val cYear = date.year
            val cMonth = date.monthValue - 1
            val cDay = date.dayOfMonth

            // date picker dialog
            datePickerDialog = DatePickerDialog(
                this@ItemDetailsActivity,
                { _, year, month, dayOfMonth ->
                    // set day of month , month and year value in the edit text
                    val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                    binding.tvExpDate.setText(newDate.format(dateFormatter))
                }, cYear, cMonth, cDay
            )
            datePickerDialog.show()
        }

        binding.tlAmount.setStartIconOnClickListener {
            if (!binding.tvAmount.text.isNullOrEmpty()) {
                var amount = binding.tvAmount.text.toString().toInt()
                if (amount > 1) {
                    amount--
                    binding.tvAmount.setText(amount.toString())
                }
            } else {
                binding.tvAmount.setText("1")
            }
        }

        binding.tlAmount.setEndIconOnClickListener {
            if (!binding.tvAmount.text.isNullOrEmpty()) {
                var amount = binding.tvAmount.text.toString().toInt()
                amount++
                binding.tvAmount.setText(amount.toString())
            } else {
                binding.tvAmount.setText("1")
            }
        }

        binding.chipBox1.setOnClickListener {
            binding.tvBox.setText("1")
        }

        binding.chipBox2.setOnClickListener {
            binding.tvBox.setText("2")
        }

        binding.chipBox3.setOnClickListener {
            binding.tvBox.setText("3")
        }

        binding.chipBox4.setOnClickListener {
            binding.tvBox.setText("4")
        }

        // Set decimal filter to amount
        binding.tvAmount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))

        // Set decimal filter to box
        binding.tvBox.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))

        binding.tvAmount.setText(item.amount)
        binding.tvBox.setText(item.box)
        binding.tvProductName.setText(item.name)
        binding.tvExpDate.setText(item.exp_date)

        val barcodeDrawable = ContextCompat.getDrawable(this, R.drawable.barcode)
        var pixelDrawableSize = (binding.tvBarcodeHint.lineHeight * 1.0).roundToInt()
        barcodeDrawable?.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize)

        val ssbBarcode = SpannableStringBuilder(getString(R.string.barcode_hint))
        ssbBarcode.setSpan(
            barcodeDrawable?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) },
            21,
            22,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvBarcodeHint.setText(ssbBarcode, TextView.BufferType.SPANNABLE)

        val dateDrawable = ContextCompat.getDrawable(this, R.drawable.calendar)
        pixelDrawableSize = (binding.tvDateHint.lineHeight * 1.0).roundToInt()
        dateDrawable?.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize)

        val ssbDate = SpannableStringBuilder(getString(R.string.date_hint))
        ssbDate.setSpan(
            dateDrawable?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) },
            21,
            22,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvDateHint.setText(ssbDate, TextView.BufferType.SPANNABLE)

        val decreaseDrawable = ContextCompat.getDrawable(this, R.drawable.minus)
        val increaseDrawable = ContextCompat.getDrawable(this, R.drawable.plus_box)
        pixelDrawableSize = (binding.tvAmountHint.lineHeight * 1.0).roundToInt()
        decreaseDrawable?.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize)
        increaseDrawable?.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize)

        val ssbAmount = SpannableStringBuilder(getString(R.string.amount_hint))
        ssbAmount.setSpan(
            decreaseDrawable?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) },
            21,
            22,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssbAmount.setSpan(
            increaseDrawable?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) },
            26,
            27,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvAmountHint.setText(ssbAmount, TextView.BufferType.SPANNABLE)

        binding.saveItem.setOnClickListener {
            validateInputs()
        }

        binding.deleteItem.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.delete_item_title))
                .setMessage(resources.getString(R.string.delete_item_supporting_text))
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, _ ->
                    dialog.dismiss()
                    val myRef =
                        database.getReference("items").child(user?.uid.toString())
                            .child(item.id.toString())
                    myRef.removeValue()
                    Toast.makeText(
                        this, "Item Deleted Successfully.",
                        Toast.LENGTH_LONG
                    ).show()
                    val returnIntent = Intent()
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
                .show()
        }
    }

    private fun searchForProduct(barcode: String) {
        // Search for product description from barcode in Firebase
        val myRef = database.getReference("barcodes").child(barcode)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                // Not used
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    binding.tvProductName.setText(snapshot.value as String)
                } else {
                    // Search for product description in i520 service
                    val product = scope.async(IO) { findProductDescription(barcode) }
                    runBlocking {
                        val prod = product.await()

                        if (prod.isNotEmpty()) {
                            val res = JSONObject(prod)
                            if (res.getBoolean("found")) {
                                binding.tvProductName.setText(res.getString("description"))
                                // Store description in database
                                val barcodeRef = database.getReference("barcodes")
                                barcodeRef.child(barcode)
                                    .setValue(res.getString("description"))
                            } else {
                                // Show dialogue to add description when product not found
                                showAddDescriptionDialogue(barcode)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun showAddDescriptionDialogue(result: String) {
        val dialogBinding = AddProductDescriptionBinding.inflate(layoutInflater)
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(result, BarcodeFormat.EAN_13, 350, 150)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            dialogBinding.imageView.setImageBitmap(bitmap)
            dialogBinding.barcode.text = result
        } catch (e: Exception) {
            Log.w(TAG, e.toString())
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.product_not_found))
            .setView(dialogBinding.root)
            .setPositiveButton(resources.getString(R.string.accept)) { _, _ ->
                // Save to Firebase
                val myRef = database.getReference("barcodes")
                myRef.child(result).setValue(dialogBinding.tvProdDesc.text.toString())
                binding.tvProductName.setText(dialogBinding.tvProdDesc.text.toString())
            }
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                // Do nothing
                dialog.dismiss()
            }
            .show()
    }

    private fun validateInputs() {
        // Reset errors.
        binding.tvProductName.error = null
        binding.tvExpDate.error = null
        binding.tvAmount.error = null
        binding.tvBox.error = null

        // Store values.
        val name = binding.tvProductName.text.toString()
        var expDate = binding.tvExpDate.text.toString()
        val amount = binding.tvAmount.text.toString()
        val box = binding.tvBox.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid name.
        if (name.isEmpty()) {
            binding.tvProductName.error = getString(R.string.error_field_required)
            focusView = binding.tvProductName
            cancel = true
        }

        // Check for a valid expiration date.
        if (expDate.isNotEmpty()) {
            val date = expDate.split('-')
            if (date.size == 3) {
                try {
                    LocalDate.parse(
                        expDate,
                        dateFormatter
                    )
                } catch (e: DateTimeParseException) {
                    binding.tvExpDate.error = getString(R.string.invalidDate)
                    focusView = binding.tvExpDate
                    cancel = true
                }
            }
            if (date.size == 2) {
                try {
                    val parsedDated = LocalDate.parse(
                        "$expDate-01",
                        dateFormatter
                    )
                    expDate = parsedDated.with(lastDayOfMonth()).format(dateFormatter)
                } catch (e: DateTimeParseException) {
                    binding.tvExpDate.error = getString(R.string.invalidDate)
                    focusView = binding.tvExpDate
                    cancel = true
                }
            }
        }

        // Check for a valid amount.
        if (amount.isEmpty()) {
            binding.tvAmount.error = getString(R.string.error_field_required)
            focusView = binding.tvAmount
            cancel = true
        }

        // Check for a valid box.
        if (box.isEmpty()) {
            binding.tvBox.error = getString(R.string.error_field_required)
            focusView = binding.tvBox
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt to store data and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            item.name = name
            item.exp_date = expDate
            item.amount = amount
            item.box = box

            // Save item to firebase
            if (item.id.isNullOrEmpty()) {
                val myRef = database.getReference("items").child(user?.uid.toString())

                val newItemRef = myRef.push()
                newItemRef.setValue(item)

                Toast.makeText(
                    this, "Item Saved Successfully.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val myRef = database.getReference("items").child(user?.uid.toString())
                    .child(item.id.toString())
                myRef.setValue(item)
                myRef.child("id").removeValue()

                Toast.makeText(
                    this, "Item Updated Successfully.",
                    Toast.LENGTH_LONG
                ).show()
            }

            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    override fun onBackPressed() {
        val returnIntent = Intent()
        setResult(Activity.RESULT_CANCELED, returnIntent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
