package com.padi.warehouse

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern


class DecimalDigitsInputFilter(digitsBeforeZero: Int) : InputFilter {
    private var mPattern = Pattern.compile("[0-9]{0,$digitsBeforeZero}")

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        val text = dest.toString() + source
        val matcher = mPattern.matcher(text)
        return if (!matcher.matches()) "" else null
    }

}