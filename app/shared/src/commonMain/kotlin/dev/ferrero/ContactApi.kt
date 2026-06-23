package dev.ferrero

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess

private const val REPLY_ENDPOINT = "https://api.ailaai.app/wild/reply"
private const val REPLY_TOKEN =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vMC4wLjAuMDo4MDgwLyIsImlzcyI6Imh0dHA6Ly8wLjAuMC4wOjgwODAvIiwiaWQiOiIyOTcyIn0._td39UjGKnXJZGweMQNxqU-55DnW7R2tuu_-tHWnlGM"
private const val REPLY_CARD = "25408196108"
private const val REPLY_DEVICE =
    "r567c9nqmdehdeh3yd7ndxm2208nihtvb5ym7qo175jmc13moq966qxmyexixer8uduug9xtnq2oho9s18c9foia2uuaj7v5xfpknvl7f97b0ecelx8ymgdg27t68y6g"

private val httpClient by lazy { HttpClient() }

/** Escapes a string so it can be safely embedded inside a JSON string literal. */
private fun String.jsonEscape(): String = buildString {
    for (c in this@jsonEscape) {
        when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            else -> if (c < ' ') append("\\u" + c.code.toString(16).padStart(4, '0')) else append(c)
        }
    }
}

/**
 * Sends the contact form to the real reply API.
 * Throws if the request fails or the server responds with a non-success status.
 */
suspend fun sendContactMessage(
    name: String,
    email: String,
    service: String,
    message: String,
) {
    val composedMessage = buildString {
        append("Name: ").append(name).append("\n\n")
        append("Email: ").append(email).append("\n\n")
        if (service.isNotBlank()) append("Service: ").append(service).append("\n\n")
        appendLine()
        appendLine()
        append(message)
    }

    val body = """{"message":"${composedMessage.jsonEscape()}","card":"$REPLY_CARD","device":"$REPLY_DEVICE"}"""

    val response: HttpResponse = httpClient.post(REPLY_ENDPOINT) {
        headers {
            append(HttpHeaders.Authorization, "Bearer $REPLY_TOKEN")
            append(HttpHeaders.Accept, "application/json")
        }
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    if (!response.status.isSuccess()) {
        throw RuntimeException("Request failed with status ${response.status}")
    }
}
