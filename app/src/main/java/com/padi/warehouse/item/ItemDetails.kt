package com.padi.warehouse.item

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.padi.warehouse.*
import com.padi.warehouse.R.layout.activity_item_details
import kotlinx.android.synthetic.main.activity_item_details.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import com.padi.warehouse.barcode.BarcodeScan


class ItemDetails : AppCompatActivity() {
    private var item = Item(name = "", exp_date = "", amount = "", box = "")
    private val bundle: Bundle? by lazy { intent.extras }
    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var mCalendar: Calendar

    @SuppressLint("SimpleDateFormat")
    private val mDateFormatter = SimpleDateFormat("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_item_details)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        if (bundle != null) {
            item = bundle!!.getParcelable<Parcelable>(MSG.ITEM.message) as Item
        }

        tv_name.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (tv_name.right - tv_name.compoundDrawables[DRAWABLE.RIGHT.index].bounds.width())) {
                    val intent = Intent(this, BarcodeScan::class.java)
                    startActivityForResult(intent, RC.BARCODE_SCAN.code)
                    return@OnTouchListener true
                }
            }
            false
        })

        tv_exp_date.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (tv_exp_date.right - tv_exp_date.compoundDrawables[DRAWABLE.RIGHT.index].bounds.width())) {
                    // Use the date from the TextView
                    mCalendar = Calendar.getInstance()
                    try {
                        val date = mDateFormatter.parse(tv_exp_date.text.toString())
                        mCalendar.time = date
                    } catch (e: ParseException) {
                        mCalendar = Calendar.getInstance()
                    }

                    val cYear = mCalendar.get(Calendar.YEAR)
                    val cMonth = mCalendar.get(Calendar.MONTH)
                    val cDay = mCalendar.get(Calendar.DAY_OF_MONTH)

                    // date picker dialog
                    datePickerDialog = DatePickerDialog(this@ItemDetails,
                            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                // set day of month , month and year value in the edit text
                                mCalendar.set(year, month, dayOfMonth, 0, 0)
                                tv_exp_date.setText(mDateFormatter.format(mCalendar.time))
                            }, cYear, cMonth, cDay)
                    datePickerDialog.show()
                    return@OnTouchListener true
                }
            }
            false
        })

        // Set decimal filter to amount
        tv_amount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))

        // Set decimal filter to box
        tv_box.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))

        tv_amount.setText(item.amount)
        tv_box.setText(item.box)
        tv_name.setText(item.name)
        tv_exp_date.setText(item.exp_date)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC.BARCODE_SCAN.code) {
                val product = data?.getStringExtra("result")
                Log.d(TAG, "Found product: $product")
                if (product != null) {
                    tv_name.setText(product)
                }
            }
        }
    }

    private fun validateInputs() {
        // Reset errors.
        tv_name.error = null
        tv_exp_date.error = null
        tv_amount.error = null
        tv_box.error = null

        // Store values.
        val name = tv_name.text.toString()
        val expDate = tv_exp_date.text.toString()
        val amount = tv_amount.text.toString()
        val box = tv_box.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid name.
        if (name.isEmpty()) {
            tv_name.error = getString(R.string.error_field_required)
            focusView = tv_name
            cancel = true
        }

        // Check for a valid expiration date.
        if (expDate.isNotEmpty()) {
            try {
                mDateFormatter.parse(expDate)
            } catch (e: ParseException) {
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

                Toast.makeText(this, "Item Saved Successfully.",
                        Toast.LENGTH_LONG).show()
            } else {
                val myRef = database.getReference("items").child(user?.uid!!).child(item.id!!)
                myRef.setValue(item)
                myRef.child("id").removeValue()

                Toast.makeText(this, "Item Updated Successfully.",
                        Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, "Item Deleted Successfully.",
                    Toast.LENGTH_LONG).show()
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

    companion object {
        private const val TAG = "ItemDetails"
    }
}
