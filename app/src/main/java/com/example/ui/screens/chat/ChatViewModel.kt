package com.example.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.gemini.GeminiChatRepository
import com.example.data.gemini.GeminiChatResult
import com.example.data.gemini.PendingExpenseAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isThinking: Boolean = false,
    val currentToolName: String? = null,
    val isApiKeyMissing: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val chatRepository: GeminiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val quickSuggestions = listOf(
        "How much did I spend on food this month?",
        "Show my expenses above ₹2000",
        "What was my biggest expense last month?",
        "Compare spending this month vs last month",
        "Add ₹500 for groceries",
        "How much did I spend in September?"
    )

    init {
        checkApiKey()
        addWelcomeMessage()
    }

    private fun checkApiKey() {
        val key = BuildConfig.GEMINI_API_KEY
        val isMissing = key.isBlank() || key == "MY_GEMINI_API_KEY"
        _uiState.update { it.copy(isApiKeyMissing = isMissing) }
    }

    private fun addWelcomeMessage() {
        val welcome = ChatMessage(
            sender = MessageSender.ASSISTANT,
            text = "👋 Hello! I'm your AI Expense Assistant. I can analyze your spending, find transactions, summarize months, and log new expenses using your real financial records.\n\nAsk me anything or tap one of the suggested prompts below!"
        )
        _uiState.update { it.copy(messages = listOf(welcome)) }
    }

    fun sendMessage(inputText: String) {
        val trimmed = inputText.trim()
        if (trimmed.isEmpty() || _uiState.value.isThinking) return

        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = trimmed
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMsg,
                isThinking = true,
                currentToolName = null,
                error = null
            )
        }

        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(trimmed)) {
                is GeminiChatResult.Success -> {
                    val aiMsg = ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        text = result.responseText
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + aiMsg,
                            isThinking = false,
                            currentToolName = null
                        )
                    }
                }
                is GeminiChatResult.ToolExecuted -> {
                    val aiMsg = ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        text = result.finalResponseText,
                        toolCall = ToolCallBadge(
                            functionName = result.functionName,
                            summary = result.summary
                        )
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + aiMsg,
                            isThinking = false,
                            currentToolName = null
                        )
                    }
                }
                is GeminiChatResult.RequiresConfirmation -> {
                    val confirmationMsg = ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        text = result.intermediateMessage,
                        pendingAction = result.pendingAction
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + confirmationMsg,
                            isThinking = false,
                            currentToolName = null
                        )
                    }
                }
                is GeminiChatResult.Error -> {
                    val errorMsg = ChatMessage(
                        sender = MessageSender.SYSTEM,
                        text = "⚠️ ${result.message}"
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + errorMsg,
                            isThinking = false,
                            currentToolName = null,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun confirmPendingAction(action: PendingExpenseAction, messageId: String) {
        // Mark the action message as resolved
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) msg.copy(isActionResolved = true) else msg
                },
                isThinking = true
            )
        }

        viewModelScope.launch {
            when (val result = chatRepository.completeConfirmedAction(action)) {
                is GeminiChatResult.ToolExecuted -> {
                    val aiMsg = ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        text = result.finalResponseText,
                        toolCall = ToolCallBadge(
                            functionName = result.functionName,
                            summary = "Confirmed & Executed"
                        )
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + aiMsg,
                            isThinking = false
                        )
                    }
                }
                is GeminiChatResult.Success -> {
                    val aiMsg = ChatMessage(
                        sender = MessageSender.ASSISTANT,
                        text = result.responseText
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + aiMsg,
                            isThinking = false
                        )
                    }
                }
                is GeminiChatResult.Error -> {
                    val errorMsg = ChatMessage(
                        sender = MessageSender.SYSTEM,
                        text = "⚠️ Execution failed: ${result.message}"
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + errorMsg,
                            isThinking = false
                        )
                    }
                }
                is GeminiChatResult.RequiresConfirmation -> {
                    _uiState.update { it.copy(isThinking = false) }
                }
            }
        }
    }

    fun cancelPendingAction(messageId: String) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId) msg.copy(isActionResolved = true) else msg
            }
            val cancelNotice = ChatMessage(
                sender = MessageSender.SYSTEM,
                text = "Action cancelled. No changes were made to your records."
            )
            state.copy(messages = updatedMessages + cancelNotice)
        }
    }

    fun clearChat() {
        chatRepository.clearHistory()
        _uiState.update { it.copy(messages = emptyList(), error = null) }
        addWelcomeMessage()
    }
}
