package com.example.ui.screens.chat

import com.example.data.gemini.PendingExpenseAction

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

data class ToolCallBadge(
    val functionName: String,
    val summary: String
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCall: ToolCallBadge? = null,
    val pendingAction: PendingExpenseAction? = null,
    val isActionResolved: Boolean = false
)
