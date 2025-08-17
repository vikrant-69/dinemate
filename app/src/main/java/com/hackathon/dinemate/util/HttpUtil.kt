package com.hackathon.dinemate.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

enum class RequestType {
    GET, POST
}

data class HttpResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, List<String>>
)

object HttpUtil {

    /**
     * Makes an HTTP request and returns the response
     * @param requestType The type of request (GET or POST)
     * @param url The URL to make the request to
     * @param body The request body (optional, mainly for POST requests)
     * @param headers Additional headers to include (optional)
     * @param timeout Connection and read timeout in milliseconds (default: 10000)
     * @return HttpResponse containing status code, body, and headers
     */
    suspend fun makeRequest(
        requestType: RequestType,
        url: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
        timeout: Int = 10000
    ): HttpResponse = withContext(Dispatchers.IO) {

        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            // Set request method
            connection.requestMethod = requestType.name

            // Set timeouts
            connection.connectTimeout = timeout
            connection.readTimeout = timeout

            // Set default headers
            connection.setRequestProperty("User-Agent", "HttpUtil/1.0")
            connection.setRequestProperty("Accept", "*/*")

            // Add custom headers
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            // Handle POST request body
            if (requestType == RequestType.POST && body != null) {
                connection.doOutput = true
                if (!headers.containsKey("Content-Type")) {
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }

                OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                    writer.write(body)
                    writer.flush()
                }
            }

            // Get response
            val responseCode = connection.responseCode
            val inputStream = if (responseCode >= 400) {
                connection.errorStream ?: connection.inputStream
            } else {
                connection.inputStream
            }

            val responseBody = inputStream?.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: ""

            val responseHeaders = connection.headerFields.filterKeys { it != null }
                .mapKeys { it.key ?: "" }

            HttpResponse(responseCode, responseBody, responseHeaders)

        } finally {
            connection.disconnect()
        }
    }

    /**
     * Convenience method for GET requests
     */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        timeout: Int = 10000
    ): HttpResponse {
        return makeRequest(RequestType.GET, url, null, headers, timeout)
    }

    /**
     * Convenience method for POST requests
     */
    suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        timeout: Int = 10000
    ): HttpResponse {
        return makeRequest(RequestType.POST, url, body, headers, timeout)
    }
}