package com.example

import com.example.util.CurrencyFormatter
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testCurrencyFormatting() {
    // 1 Lakh Rupees = 10,000,000 paise
    val formatted = CurrencyFormatter.formatPaise(10000000L)
    assertTrue("Should contain 1,00,000", formatted.contains("1,00,000"))
  }

  @Test
  fun testCurrencyParsing() {
    val paise = CurrencyFormatter.parseAmountToPaise("1500.50")
    assertEquals(150050L, paise)
  }
}
