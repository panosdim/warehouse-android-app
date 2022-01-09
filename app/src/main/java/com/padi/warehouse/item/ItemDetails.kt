package com.padi.warehouse.item

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
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
import com.padi.warehouse.R.layout.activity_item_details
import kotlinx.android.synthetic.main.activity_item_details.*
import kotlinx.android.synthetic.main.add_product_description.view.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt


class ItemDetails : AppCompatActivity() {
    private var item = Item(name = "", exp_date = "", amount = "", box = "")
    private val bundle: Bundle? by lazy { intent.extras }
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var mSnackbar: Snackbar
    private val mDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val scanOptions = ScanOptions()

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(this@ItemDetails, "Cancelled", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(
                this@ItemDetails,
                "Scanned: " + result.contents,
                Toast.LENGTH_LONG
            ).show()
        }

        // We will get scan results here
        mSnackbar = Snackbar.make(
            llvDetails,
            "Searching for product name in online database.",
            Snackbar.LENGTH_LONG
        )
        mSnackbar.show()
        if (result.contents == null) {
            Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_LONG).show()
        } else {
            searchForProduct(result.contents)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_item_details)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        if (bundle != null) {
            item = bundle!!.getParcelable<Parcelable>(MSG.ITEM.message) as Item
        }

        tv_product_name.setOnTouchListener(View.OnTouchListener { v, event ->
            v.performClick()
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= (tv_product_name.right - tv_product_name.compoundDrawables[DRAWABLE.RIGHT.index].bounds.width())) {
                // Initiate scan with zxing custom scan activity
                scanOptions.captureActivity = BarcodeScan::class.java
                barcodeLauncher.launch(scanOptions)
                return@OnTouchListener true
            }
            false
        })

        tv_exp_date.setOnTouchListener(View.OnTouchListener { v, event ->
            v.performClick()
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= (tv_exp_date.right - tv_exp_date.compoundDrawables[DRAWABLE.RIGHT.index].bounds.width())) {
                // Use the date from the TextView
                val date: LocalDate = try {
                    LocalDate.parse(tv_exp_date.text.toString())
                } catch (ex: DateTimeParseException) {
                    LocalDate.now()
                }

                val cYear = date.year
                val cMonth = date.monthValue - 1
                val cDay = date.dayOfMonth

                // date picker dialog
                datePickerDialog = DatePickerDialog(
                    this@ItemDetails,
                    { _, year, month, dayOfMonth ->
                        // set day of month , month and year value in the edit text
                        val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                        tv_exp_date.setText(newDate.format(mDateFormatter))
                    }, cYear, cMonth, cDay
                )
                datePickerDialog.show()

                return@OnTouchListener true
            }
            false
        })

        // Set decimal filter to amount
        tv_amount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))

        // Set decimal filter to box
        tv_box.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))

        tv_amount.setText(item.amount)
        tv_box.setText(item.box)
        tv_product_name.setText(item.name)
        tv_exp_date.setText(item.exp_date)

        val barcodeDrawable = ContextCompat.getDrawable(this, R.drawable.barcode)
        var pixelDrawableSize = (tvBarcodeHint.lineHeight * 1.0).roundToInt()
        barcodeDrawable!!.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize)

        val ssbBarcode = SpannableStringBuilder(getString(R.string.barcode_hint))
        ssbBarcode.setSpan(
            ImageSpan(barcodeDrawable, ImageSpan.ALIGN_BOTTOM),
            21,
            22,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvBarcodeHint.setText(ssbBarcode, TextView.BufferType.SPANNABLE)

        val dateDrawable = ContextCompat.getDrawable(this, R.drawable.calendar)
        pixelDrawableSize = (tvDateHint.lineHeight * 1.0).roundToInt()
        dateDrawable!!.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize)

        val ssbDate = SpannableStringBuilder(getString(R.string.date_hint))
        ssbDate.setSpan(
            ImageSpan(dateDrawable, ImageSpan.ALIGN_BOTTOM),
            21,
            22,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvDateHint.setText(ssbDate, TextView.BufferType.SPANNABLE)
    }

    private fun searchForProduct(result: String) {
        // Search for product description from barcode in Firebase
        val myRef = database.getReference("barcodes").child(result)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                // Not used
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    tv_product_name.setText(snapshot.value as String)
                    mSnackbar.dismiss()
                } else {
                    // Search for product description in i520 service
                    val product = GlobalScope.async(IO) { findProductDescription(result) }
                    runBlocking {
                        val prod = product.await()
                        if (prod.isNotEmpty()) {
                            mSnackbar.dismiss()
                            val res = JSONObject(prod)
                            if (res.getBoolean("found")) {
                                tv_product_name.setText(res.getString("description"))
                                // Store description in database
                                val barcodeRef = database.getReference("barcodes")
                                barcodeRef.child(result)
                                    .setValue(res.getString("description"))
                            } else {
                                // Show dialogue to add description when product not found
                                showAddDescriptionDialogue(result)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun findProductDescription(barcode: String): String {
        val url: URL
        var response = ""
        try {
            val jsonParam = JSONObject()
            jsonParam.put("barcode", barcode)

            url = URL("https://warehouse.cc.nf/api/v1/barcode.php")

            val conn = url.openConnection() as HttpURLConnection

            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "POST"
            conn.doOutput = true

            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")

            val printout = DataOutputStream(conn.outputStream)
            printout.write(jsonParam.toString().toByteArray(Charsets.UTF_8))
            printout.flush()
            printout.close()

            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                response = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return response
    }

    @SuppressLint("InflateParams")
    private fun showAddDescriptionDialogue(result: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.add_product_description, null)
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(result, BarcodeFormat.EAN_13, 350, 150)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            dialogView.imageView.setImageBitmap(bitmap)
            dialogView.barcode.text = result
        } catch (e: Exception) {
            e.printStackTrace()
        }

        builder.setTitle("Product Not Found")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                // Save to Firebase
                val myRef = database.getReference("barcodes")
                myRef.child(result).setValue(dialogView.tv_prod_desc.text.toString())
                tv_product_name.setText(dialogView.tv_prod_desc.text.toString())
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Do nothing
                dialog.dismiss()
            }
            .show()
    }

    private fun validateInputs() {
        // Reset errors.
        tv_product_name.error = null
        tv_exp_date.error = null
        tv_amount.error = null
        tv_box.error = null

        // Store values.
        val name = tv_product_name.text.toString()
        val expDate = tv_exp_date.text.toString()
        val amount = tv_amount.text.toString()
        val box = tv_box.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid name.
        if (name.isEmpty()) {
            tv_product_name.error = getString(R.string.error_field_required)
            focusView = tv_product_name
            cancel = true
        }

        // Check for a valid expiration date.
        if (expDate.isNotEmpty()) {
            try {
                mDateFormatter.parse(expDate)
            } catch (e: DateTimeParseException) {
                tv_exp_date.error = getString(R.string.invalidDate)
                focusView = tv_exp_date
                cancel = true
            }
        }

        // Check for a valid amount.
        if (amount.isEmpty()) {
            tv_amount.error = getString(R.string.error_field_required)
            focusView = tv_amount
            cancel = true
        }

        // Check for a valid box.
        if (box.isEmpty()) {
            tv_box.error = getString(R.string.error_field_required)
            focusView = tv_box
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt to store data and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            item.name = name
            item.exp_date = expDate
            item.amount = amount
            item.box = box

            // Save item to firebase
            if (item.id.isNullOrEmpty()) {
                val myRef = database.getReference("items").child(user?.uid!!)

                val newItemRef = myRef.push()
                newItemRef.setValue(item)

                Toast.makeText(
                    this, "Item Saved Successfully.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val myRef = database.getReference("items").child(user?.uid!!).child(item.id!!)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.food_details, menu)
        if (bundle == null) {
            menu.findItem(R.id.food_delete).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(mnuItem: MenuItem) = when (mnuItem.itemId) {
        R.id.food_save -> {
            validateInputs()
            true
        }

        R.id.food_delete -> {
            val myRef = database.getReference("items").child(user?.uid!!).child(item.id!!)
            myRef.removeValue()
            Toast.makeText(
                this, "Item Deleted Successfully.",
                Toast.LENGTH_LONG
            ).show()
            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(mnuItem)
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
