package com.example

import com.example.data.gemini.model.Content
import com.example.data.gemini.model.FunctionCall
import com.example.data.gemini.model.GenerateContentRequest
import com.example.data.gemini.model.Part
import com.example.util.CurrencyFormatter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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

  @Test
  fun testGeminiRequestSerialization() {
    val json = Json { ignoreUnknownKeys = true }
    val request = GenerateContentRequest(
      contents = listOf(
        Content(
          role = "user",
          parts = listOf(Part(text = "How much did I spend on food this month?"))
        ),
        Content(
          role = "model",
          parts = listOf(
            Part(
              functionCall = FunctionCall(
                name = "getExpensesByCategory",
                args = buildJsonObject {
                  put("category", "Food")
                  put("dateRange", "THIS_MONTH")
                }
              )
            )
          )
        )
      )
    )
    val encoded = json.encodeToString(GenerateContentRequest.serializer(), request)
    assertTrue("Should contain getExpensesByCategory", encoded.contains("getExpensesByCategory"))
    assertTrue("Should contain Food", encoded.contains("Food"))
  }
}

