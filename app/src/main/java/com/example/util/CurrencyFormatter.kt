package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    /**
     * Formats an integer amount in paise (100 paise = 1 INR)
     * using the Indian numbering format (e.g., ₹1,00,000.00).
     */
    fun formatPaise(
        paise: Long,
        symbol: String = "₹",
        showDecimals: Boolean = true
    ): String {
        val isNegative = paise < 0
        val absPaise = Math.abs(paise)
        val rupees = absPaise / 100
        val remainingPaise = absPaise % 100

        val formattedRupees = formatIndianNumber(rupees)
        val sign = if (isNegative) "-" else ""

        return if (showDecimals) {
            val paiseString = String.format(Locale.US, "%02d", remainingPaise)
            "$sign$symbol$formattedRupees.$paiseString"
        } else {
            "$sign$symbol$formattedRupees"
        }
    }

    /**
     * Custom Indian numbering formatter:
     * 1000 -> 1,000
     * 100000 -> 1,00,000
     * 10000000 -> 1,00,00,000
     */
    private fun formatIndianNumber(number: Long): String {
        val str = number.toString()
        val len = str.length
        if (len <= 3) return str

        val lastThree = str.substring(len - 3)
        val remaining = str.substring(0, len - 3)

        val sb = java.lang.StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count % 2 == 0 && i != 0) {
                sb.append(',')
            }
        }
        return sb.reverse().toString() + "," + lastThree
    }

    /**
     * Safely parses an amount string (e.g. "1500.50" or "500") into integer paise.
     * Prevents floating point errors.
     */
    fun parseAmountToPaise(input: String): Long {
        val cleaned = input.trim().replace(",", "").replace("₹", "").replace("$", "")
        if (cleaned.isEmpty()) return 0L

        val parts = cleaned.split(".")
        val rupees = parts[0].toLongOrNull() ?: 0L
        val paise = if (parts.size > 1) {
            val dec = parts[1].take(2).padEnd(2, '0')
            dec.toLongOrNull() ?: 0L
        } else {
            0L
        }
        return (rupees * 100) + paise
    }

    fun paiseToDecimalString(paise: Long): String {
        val rupees = paise / 100
        val rem = Math.abs(paise % 100)
        return if (rem == 0L) {
            rupees.toString()
        } else {
            "$rupees.${String.format(Locale.US, "%02d", rem)}"
        }
    }
}
