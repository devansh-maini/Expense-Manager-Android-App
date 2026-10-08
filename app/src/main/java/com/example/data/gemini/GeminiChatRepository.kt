package com.example.data.gemini

import com.example.BuildConfig
import com.example.data.gemini.model.Candidate
import com.example.data.gemini.model.Content
import com.example.data.gemini.model.FunctionCall
import com.example.data.gemini.model.FunctionDeclaration
import com.example.data.gemini.model.FunctionParameters
import com.example.data.gemini.model.FunctionResponse
import com.example.data.gemini.model.GenerateContentRequest
import com.example.data.gemini.model.GenerationConfig
import com.example.data.gemini.model.Part
import com.example.data.gemini.model.PropertySchema
import com.example.data.gemini.model.Tool
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class GeminiChatResult {
    data class Success(val responseText: String) : GeminiChatResult()
    data class RequiresConfirmation(
        val pendingAction: PendingExpenseAction,
        val intermediateMessage: String
    ) : GeminiChatResult()
    data class ToolExecuted(
        val functionName: String,
        val summary: String,
        val finalResponseText: String
    ) : GeminiChatResult()
    data class Error(val message: String) : GeminiChatResult()
}

class GeminiChatRepository(
    private val expenseToolsService: ExpenseToolsService,
    private val apiService: GeminiApiService = GeminiRetrofitClient.service
) {

    private val conversationHistory = mutableListOf<Content>()

    private val tools: List<Tool> by lazy {
        listOf(
            Tool(
                functionDeclarations = listOf(
                    FunctionDeclaration(
                        name = "getExpenses",
                        description = "Get a list of expenses with optional filtering by date range, min/max amount, category, or pagination.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "limit" to PropertySchema("INTEGER", "Maximum number of records to return (1-50, default 20)."),
                                "offset" to PropertySchema("INTEGER", "Number of records to skip for pagination."),
                                "minAmountRupees" to PropertySchema("NUMBER", "Minimum expense amount in Rupees (e.g. 2000.0)."),
                                "maxAmountRupees" to PropertySchema("NUMBER", "Maximum expense amount in Rupees."),
                                "startDate" to PropertySchema("STRING", "Start date formatted as YYYY-MM-DD."),
                                "endDate" to PropertySchema("STRING", "End date formatted as YYYY-MM-DD."),
                                "category" to PropertySchema("STRING", "Category name filter (e.g. 'Food', 'Groceries').")
                            )
                        )
                    ),
                    FunctionDeclaration(
                        name = "getExpenseById",
                        description = "Retrieve details of a single specific expense by its unique ID.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "id" to PropertySchema("INTEGER", "The unique expense ID.")
                            ),
                            required = listOf("id")
                        )
                    ),
                    FunctionDeclaration(
                        name = "searchExpenses",
                        description = "Search expenses by matching a keyword across merchant names and notes/descriptions.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "keyword" to PropertySchema("STRING", "Keyword to search for (e.g. 'Swiggy', 'Starbucks', 'Flight').")
                            ),
                            required = listOf("keyword")
                        )
                    ),
                    FunctionDeclaration(
                        name = "getExpensesByCategory",
                        description = "Get expenses belonging to a specific category, optionally filtered by a date range (e.g. 'THIS_MONTH', 'LAST_MONTH', or 'September 2026').",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "category" to PropertySchema("STRING", "Category name (e.g. 'Food', 'Fuel', 'Shopping', 'Rent')."),
                                "dateRange" to PropertySchema("STRING", "Named date range like 'THIS_MONTH', 'LAST_MONTH', 'THIS_YEAR', or month name like 'September 2026'."),
                                "startDate" to PropertySchema("STRING", "Start date (YYYY-MM-DD)."),
                                "endDate" to PropertySchema("STRING", "End date (YYYY-MM-DD).")
                            ),
                            required = listOf("category")
                        )
                    ),
                    FunctionDeclaration(
                        name = "getTotalExpenses",
                        description = "Calculate the total spent and transaction count across all expenses or within a specific date range.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "dateRange" to PropertySchema("STRING", "Period such as 'THIS_MONTH', 'LAST_MONTH', 'TODAY', 'YESTERDAY', 'THIS_WEEK', 'THIS_YEAR'."),
                                "startDate" to PropertySchema("STRING", "Start date (YYYY-MM-DD)."),
                                "endDate" to PropertySchema("STRING", "End date (YYYY-MM-DD).")
                            )
                        )
                    ),
                    FunctionDeclaration(
                        name = "getMonthlySummary",
                        description = "Get a comprehensive monthly summary including total spending, transaction count, category breakdown with percentages, and the largest transaction.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "month" to PropertySchema("INTEGER", "Month number (1 for Jan to 12 for Dec)."),
                                "year" to PropertySchema("INTEGER", "Year (e.g. 2026)."),
                                "relativeMonth" to PropertySchema("STRING", "'THIS_MONTH' or 'LAST_MONTH'.")
                            )
                        )
                    ),
                    FunctionDeclaration(
                        name = "getLargestExpense",
                        description = "Find the single largest expense within a given date range or across all time.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "dateRange" to PropertySchema("STRING", "Named date range like 'THIS_MONTH', 'LAST_MONTH', 'THIS_YEAR', 'ALL_TIME'."),
                                "startDate" to PropertySchema("STRING", "Start date (YYYY-MM-DD)."),
                                "endDate" to PropertySchema("STRING", "End date (YYYY-MM-DD).")
                            )
                        )
                    ),
                    FunctionDeclaration(
                        name = "addExpense",
                        description = "Record a new expense into the user's expense database.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "amountRupees" to PropertySchema("NUMBER", "Amount in Indian Rupees (e.g. 500.0, 1250.50)."),
                                "category" to PropertySchema("STRING", "Category (e.g. 'Groceries', 'Food', 'Transport', 'Entertainment')."),
                                "merchant" to PropertySchema("STRING", "Merchant or vendor name (e.g. 'BigBasket', 'Zomato', 'Shell')."),
                                "description" to PropertySchema("STRING", "Additional notes or description."),
                                "paymentMethod" to PropertySchema("STRING", "Payment method used (e.g. 'UPI', 'Cash', 'Credit Card', 'Debit Card')."),
                                "date" to PropertySchema("STRING", "Date for the expense, e.g. 'today', 'yesterday', or 'YYYY-MM-DD'.")
                            ),
                            required = listOf("amountRupees", "category")
                        )
                    ),
                    FunctionDeclaration(
                        name = "updateExpense",
                        description = "Update an existing expense record. Destructive/modifying action that will trigger confirmation.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "id" to PropertySchema("INTEGER", "The ID of the expense to update."),
                                "amountRupees" to PropertySchema("NUMBER", "New amount in Rupees."),
                                "category" to PropertySchema("STRING", "New category name."),
                                "merchant" to PropertySchema("STRING", "New merchant name."),
                                "description" to PropertySchema("STRING", "New notes/description.")
                            ),
                            required = listOf("id")
                        )
                    ),
                    FunctionDeclaration(
                        name = "deleteExpense",
                        description = "Delete an expense record by its ID. Destructive action that will trigger confirmation.",
                        parameters = FunctionParameters(
                            properties = mapOf(
                                "id" to PropertySchema("INTEGER", "The ID of the expense to delete.")
                            ),
                            required = listOf("id")
                        )
                    )
                )
            )
        )
    }

    private fun getSystemInstruction(): Content {
        val todayStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
        return Content(
            parts = listOf(
                Part(
                    text = """
                    You are the AI Financial Assistant for Expense Manager, an offline-first personal financial manager in India.
                    Today's date is: $todayStr (Current Year: 2026).
                    
                    STRICT RULES:
                    1. NEVER hallucinate, invent, assume, or guess expense amounts, dates, or transactions. You must strictly invoke tools to query real user data.
                    2. If a query returns no records or zero expenses, state that honestly based on the tool result.
                    3. The currency is Indian Rupees (₹). Use Indian numbering formatting (₹1,00,000.00 / Lakhs) where appropriate.
                    4. When user asks to add an expense (e.g. "Add ₹500 for groceries"), call addExpense.
                    5. When user asks to update or delete an expense, call updateExpense or deleteExpense. If confirmation is required, inform the user clearly.
                    6. Be concise, polite, and helpful. Always summarize financial metrics with clarity.
                    """.trimIndent()
                )
            )
        )
    }

    fun clearHistory() {
        conversationHistory.clear()
    }

    suspend fun sendMessage(userText: String): GeminiChatResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiChatResult.Error(
                "Gemini API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio."
            )
        }

        // Add user turn to conversation history
        val userContent = Content(
            role = "user",
            parts = listOf(Part(text = userText))
        )
        conversationHistory.add(userContent)

        try {
            val initialRequest = GenerateContentRequest(
                contents = conversationHistory.toList(),
                systemInstruction = getSystemInstruction(),
                tools = tools,
                generationConfig = GenerationConfig(
                    temperature = 0.2f,
                    topP = 0.95f
                )
            )

            val initialResponse = apiService.generateContent(apiKey, initialRequest)
            val candidate = initialResponse.candidates?.firstOrNull()
            val candidateContent = candidate?.content

            if (candidateContent == null) {
                val errorMsg = initialResponse.error?.message ?: "Received empty response from Gemini."
                return@withContext GeminiChatResult.Error(errorMsg)
            }

            // Check if model returned a functionCall
            val functionCallPart = candidateContent.parts.firstOrNull { it.functionCall != null }
            if (functionCallPart?.functionCall != null) {
                val functionCall = functionCallPart.functionCall
                return@withContext handleFunctionCall(functionCall, candidateContent, apiKey)
            }

            // Otherwise, normal text response
            val responseText = candidateContent.parts.firstOrNull { !it.text.isNullOrBlank() }?.text
                ?: "I processed your request, but have no text to display."
            conversationHistory.add(candidateContent)
            return@withContext GeminiChatResult.Success(responseText)

        } catch (e: retrofit2.HttpException) {
            val friendlyMsg = parseErrorMessage(e)
            return@withContext GeminiChatResult.Error("Gemini API Error (${e.code()}): $friendlyMsg")
        } catch (e: Exception) {
            return@withContext GeminiChatResult.Error(e.message ?: "Failed to communicate with Gemini API.")
        }
    }

    private fun parseErrorMessage(e: retrofit2.HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string() ?: return e.message()
            val element = Json.parseToJsonElement(errorBody).jsonObject
            val apiMessage = element["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
            apiMessage ?: errorBody.take(250)
        } catch (_: Exception) {
            e.message()
        }
    }

    private suspend fun handleFunctionCall(
        call: FunctionCall,
        modelTurnContent: Content,
        apiKey: String
    ): GeminiChatResult {
        // Append model's tool request turn (preserves thoughtSignature)
        conversationHistory.add(modelTurnContent)

        val args = call.args ?: buildJsonObject {}
        val toolName = call.name
        val callId = call.id

        // Check if function is destructive and requires user confirmation
        when (toolName) {
            "deleteExpense" -> {
                val id = args["id"]?.jsonPrimitive?.longOrNull ?: 0L
                val (jsonResult, pendingDelete) = expenseToolsService.checkDeleteExpense(
                    id = id,
                    isConfirmed = false,
                    callId = callId
                )
                if (pendingDelete != null) {
                    val prompt = "Please confirm deleting expense #${pendingDelete.expenseId} (${pendingDelete.merchant.ifEmpty { pendingDelete.categoryName }} for ${pendingDelete.amountFormatted})."
                    return GeminiChatResult.RequiresConfirmation(pendingDelete, prompt)
                }
                return sendToolResponseAndComplete(toolName, callId, jsonResult, apiKey)
            }
            "updateExpense" -> {
                val id = args["id"]?.jsonPrimitive?.longOrNull ?: 0L
                val amount = args["amountRupees"]?.jsonPrimitive?.doubleOrNull
                val cat = args["category"]?.jsonPrimitive?.content
                val merchant = args["merchant"]?.jsonPrimitive?.content
                val desc = args["description"]?.jsonPrimitive?.content

                val (jsonResult, pendingUpdate) = expenseToolsService.checkUpdateExpense(
                    id = id,
                    amountRupees = amount,
                    categoryName = cat,
                    merchant = merchant,
                    description = desc,
                    isConfirmed = false,
                    callId = callId
                )
                if (pendingUpdate != null) {
                    val prompt = "Please confirm modifying expense #${pendingUpdate.expenseId}."
                    return GeminiChatResult.RequiresConfirmation(pendingUpdate, prompt)
                }
                return sendToolResponseAndComplete(toolName, callId, jsonResult, apiKey)
            }
            else -> {
                // Execute standard non-destructive tools
                val jsonResult = executeSafeTool(toolName, args)
                return sendToolResponseAndComplete(toolName, callId, jsonResult, apiKey)
            }
        }
    }

    suspend fun completeConfirmedAction(action: PendingExpenseAction): GeminiChatResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val toolName = when (action) {
            is PendingExpenseAction.Delete -> "deleteExpense"
            is PendingExpenseAction.Update -> "updateExpense"
        }
        val callId = when (action) {
            is PendingExpenseAction.Delete -> action.callId
            is PendingExpenseAction.Update -> action.callId
        }
        val resultJson = when (action) {
            is PendingExpenseAction.Delete -> expenseToolsService.executeConfirmedDelete(action)
            is PendingExpenseAction.Update -> expenseToolsService.executeConfirmedUpdate(action)
        }
        return@withContext sendToolResponseAndComplete(toolName, callId, resultJson, apiKey)
    }

    private suspend fun executeSafeTool(name: String, args: JsonObject): JsonObject {
        return try {
            when (name) {
                "getExpenses" -> {
                    expenseToolsService.getExpenses(
                        limit = args["limit"]?.jsonPrimitive?.intOrNull,
                        offset = args["offset"]?.jsonPrimitive?.intOrNull,
                        minAmountRupees = args["minAmountRupees"]?.jsonPrimitive?.doubleOrNull,
                        maxAmountRupees = args["maxAmountRupees"]?.jsonPrimitive?.doubleOrNull,
                        startDate = args["startDate"]?.jsonPrimitive?.content,
                        endDate = args["endDate"]?.jsonPrimitive?.content,
                        category = args["category"]?.jsonPrimitive?.content
                    )
                }
                "getExpenseById" -> {
                    val id = args["id"]?.jsonPrimitive?.longOrNull ?: 0L
                    expenseToolsService.getExpenseById(id)
                }
                "searchExpenses" -> {
                    val kw = args["keyword"]?.jsonPrimitive?.content ?: ""
                    expenseToolsService.searchExpenses(kw)
                }
                "getExpensesByCategory" -> {
                    val cat = args["category"]?.jsonPrimitive?.content ?: ""
                    expenseToolsService.getExpensesByCategory(
                        categoryName = cat,
                        dateRange = args["dateRange"]?.jsonPrimitive?.content,
                        startDate = args["startDate"]?.jsonPrimitive?.content,
                        endDate = args["endDate"]?.jsonPrimitive?.content
                    )
                }
                "getTotalExpenses" -> {
                    expenseToolsService.getTotalExpenses(
                        dateRange = args["dateRange"]?.jsonPrimitive?.content,
                        startDate = args["startDate"]?.jsonPrimitive?.content,
                        endDate = args["endDate"]?.jsonPrimitive?.content
                    )
                }
                "getMonthlySummary" -> {
                    expenseToolsService.getMonthlySummary(
                        month = args["month"]?.jsonPrimitive?.intOrNull,
                        year = args["year"]?.jsonPrimitive?.intOrNull,
                        relativeMonth = args["relativeMonth"]?.jsonPrimitive?.content
                    )
                }
                "getLargestExpense" -> {
                    expenseToolsService.getLargestExpense(
                        dateRange = args["dateRange"]?.jsonPrimitive?.content,
                        startDate = args["startDate"]?.jsonPrimitive?.content,
                        endDate = args["endDate"]?.jsonPrimitive?.content
                    )
                }
                "addExpense" -> {
                    val amount = args["amountRupees"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                    val cat = args["category"]?.jsonPrimitive?.content ?: "General"
                    expenseToolsService.addExpense(
                        amountRupees = amount,
                        categoryName = cat,
                        merchant = args["merchant"]?.jsonPrimitive?.content,
                        description = args["description"]?.jsonPrimitive?.content,
                        paymentMethodName = args["paymentMethod"]?.jsonPrimitive?.content,
                        date = args["date"]?.jsonPrimitive?.content
                    )
                }
                else -> {
                    buildJsonObject {
                        put("error", "Unknown function: $name")
                    }
                }
            }
        } catch (e: Exception) {
            buildJsonObject {
                put("error", e.message ?: "Failed executing $name")
            }
        }
    }

    private suspend fun sendToolResponseAndComplete(
        toolName: String,
        callId: String?,
        jsonResult: JsonObject,
        apiKey: String
    ): GeminiChatResult {
        // Construct function turn
        val functionResponseContent = Content(
            role = "function",
            parts = listOf(
                Part(
                    functionResponse = FunctionResponse(
                        name = toolName,
                        id = callId,
                        response = buildJsonObject {
                            put("output", jsonResult)
                        }
                    )
                )
            )
        )
        conversationHistory.add(functionResponseContent)

        return try {
            // Request final model synthesis
            val finalRequest = GenerateContentRequest(
                contents = conversationHistory.toList(),
                systemInstruction = getSystemInstruction(),
                tools = tools,
                generationConfig = GenerationConfig(
                    temperature = 0.2f,
                    topP = 0.95f
                )
            )

            val finalResponse = apiService.generateContent(apiKey, finalRequest)
            val candidate = finalResponse.candidates?.firstOrNull()
            val candidateContent = candidate?.content

            // Model could theoretically call another function or return text
            val nextCallPart = candidateContent?.parts?.firstOrNull { it.functionCall != null }
            if (nextCallPart?.functionCall != null) {
                return handleFunctionCall(nextCallPart.functionCall, candidateContent, apiKey)
            }

            val text = candidateContent?.parts?.firstOrNull { !it.text.isNullOrBlank() }?.text
                ?: "Completed function $toolName."

            candidateContent?.let { conversationHistory.add(it) }

            GeminiChatResult.ToolExecuted(
                functionName = toolName,
                summary = "Called $toolName",
                finalResponseText = text
            )
        } catch (e: retrofit2.HttpException) {
            val friendlyMsg = parseErrorMessage(e)
            GeminiChatResult.Error("Gemini API Error (${e.code()}): $friendlyMsg")
        } catch (e: Exception) {
            GeminiChatResult.Error(e.message ?: "Failed in final tool completion.")
        }
    }
}
