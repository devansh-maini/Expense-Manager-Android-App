package com.example.data.gemini.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val tools: List<Tool>? = null,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Content(
    val role: String? = null, // "user", "model", "function"
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val functionCall: FunctionCall? = null,
    val functionResponse: FunctionResponse? = null,
    val thoughtSignature: String? = null
)

@Serializable
data class FunctionCall(
    val name: String,
    val args: JsonObject? = null,
    val id: String? = null
)

@Serializable
data class FunctionResponse(
    val name: String,
    val response: JsonObject,
    val id: String? = null
)

@Serializable
data class Tool(
    val functionDeclarations: List<FunctionDeclaration>? = null
)

@Serializable
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: FunctionParameters? = null
)

@Serializable
data class FunctionParameters(
    val type: String = "OBJECT",
    val properties: Map<String, PropertySchema>? = null,
    val required: List<String>? = null
)

@Serializable
data class PropertySchema(
    val type: String, // "STRING", "NUMBER", "INTEGER", "BOOLEAN"
    val description: String? = null,
    val enum: List<String>? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val maxOutputTokens: Int? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiErrorResponse? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

@Serializable
data class GeminiErrorResponse(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
