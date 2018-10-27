package com.padi.warehouse

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
import com.padi.warehouse.R.layout.activity_item_details
import kotlinx.android.synthetic.main.activity_item_details.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.database.FirebaseDatabase



class FoodDetails : AppCompatActivity() {
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
            item = bundle!!.getParcelable<Parcelable>(MSG.FOOD_ITEM.message) as Item
        }

        tv_food_name.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (tv_food_name.right - tv_food_name.compoundDrawables[DRAWABLE.RIGHT.index].bounds.width())) {
                    val intent = Intent(this, BarcodeScan::class.java)
                    startActivityForResult(intent, RC.BARCODE_SCAN.code)
                    return@OnTouchListener true
                }
            }
            false
        })

        tv_food_exp_date.setOnTouchListener(View.OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (tv_food_exp_date.right - tv_food_exp_date.compoundDrawables[DRAWABLE.RIGHT.index].bounds.width())) {
                    // Use the date from the TextView
                    mCalendar = Calendar.getInstance()
                    try {
                        val date = mDateFormatter.parse(tv_food_exp_date.text.toString())
                        mCalendar.time = date
                    } catch (e: ParseException) {
                        mCalendar = Calendar.getInstance()
                    }

                    val cYear = mCalendar.get(Calendar.YEAR)
                    val cMonth = mCalendar.get(Calendar.MONTH)
                    val cDay = mCalendar.get(Calendar.DAY_OF_MONTH)

                    // date picker dialog
                    datePickerDialog = DatePickerDialog(this@FoodDetails,
                            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                // set day of month , month and year value in the edit text
                                mCalendar.set(year, month, dayOfMonth, 0, 0)
                                tv_food_exp_date.setText(mDateFormatter.format(mCalendar.time))
                            }, cYear, cMonth, cDay)
                    datePickerDialog.show()
                    return@OnTouchListener true
                }
            }
            false
        })

        // Set decimal filter to amount
        tv_food_amount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))

        // Set decimal filter to box
        tv_food_box.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(2))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC.BARCODE_SCAN.code) {
                val product = data?.getStringExtra("result")
                Log.d(TAG, "Found product: $product")
                if (product != null) {
                    tv_food_name.setText(product)
                }
            }
        }
    }

    private fun validateInputs() {
        // Reset errors.
        tv_food_name.error = null
        tv_food_exp_date.error = null
        tv_food_amount.error = null
        tv_food_box.error = null

        // Store values.
        val name = tv_food_name.text.toString()
        val exp_date = tv_food_exp_date.text.toString()
        val amount = tv_food_amount.text.toString()
        val box = tv_food_box.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid name.
        if (name.isEmpty()) {
            tv_food_name.error = getString(R.string.error_field_required)
            focusView = tv_food_name
            cancel = true
        }

        // Check for a valid expiration date.
        if (exp_date.isEmpty()) {
            tv_food_exp_date.error = getString(R.string.error_field_required)
            focusView = tv_food_exp_date
            cancel = true
        } else {
            try {
                mDateFormatter.parse(exp_date)
            } catch (e: ParseException) {
                tv_food_exp_date.error = getString(R.string.invalidDate)
                focusView = tv_food_exp_date
                cancel = true
            }
        }

        // Check for a valid amount.
        if (amount.isEmpty()) {
            tv_food_amount.error = getString(R.string.error_field_required)
            focusView = tv_food_amount
            cancel = true
        }

        // Check for a valid box.
        if (box.isEmpty()) {
            tv_food_box.error = getString(R.string.error_field_required)
            focusView = tv_food_box
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt to store data and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            item.name = name
            item.exp_date = exp_date
            item.amount = amount
            item.box = box

            // Save item to firebase
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("items")

            val newItemRef = myRef.push()
            newItemRef.setValue(item)

            Toast.makeText(this, "Item Saved Successfully.",
                    Toast.LENGTH_LONG).show()

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

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.food_save -> {
            validateInputs()
            true
        }

        R.id.food_delete -> {
            Log.d(TAG, "Food Delete Selected")
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
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
        private const val TAG = "FoodDetails"
    }
}
