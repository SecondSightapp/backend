package com.secondsight.gemeniimplementation

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Properties
import java.io.FileInputStream
import java.io.IOException

// Data class to represent the API request
@Serializable
data class ApiRequest(val prompt: String)

// Data class to represent the API response
@Serializable
data class ApiResponse(val actions: List<String>)

// Function to get the API key from local.properties
fun getApiKey(): String {
    val properties = Properties()
    val localPropertiesFile = "local.properties"
    FileInputStream(localPropertiesFile).use { inputStream ->
        properties.load(inputStream)
    }
    return properties.getProperty("GEMINI_API_KEY")
}

// Function to make a POST request to the API
fun queryApi(prompt: String): ApiResponse? {
    val apiKey = getApiKey()
    val client = OkHttpClient()

    // Create the request payload
    val requestPayload = Json.encodeToString(ApiRequest(prompt))
    val requestBody = requestPayload.toRequestBody("application/json".toMediaTypeOrNull())

    // Create the request
    val request = Request.Builder()
        .url("https://api.gemini.com/v1/query")
        .addHeader("Authorization", "Bearer $apiKey")
        .post(requestBody)
        .build()

    // Make the request and process the response
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseBody = response.body?.string()
        return responseBody?.let {
            Json.decodeFromString<ApiResponse>(it)
        }
    }
}

// Function to process the response into bullet points
fun processResponse(response: ApiResponse): String {
    val bulletList = response.actions.joinToString("\n") { "- $it" }
    return "Actions:\n$bulletList"
}

// Main function to test the API call
fun bruhmoment() {
    val prompt = "Your prompt here"
    val response = queryApi(prompt)
    response?.let {
        val processedResponse = processResponse(it)
        println(processedResponse)
    }
}
