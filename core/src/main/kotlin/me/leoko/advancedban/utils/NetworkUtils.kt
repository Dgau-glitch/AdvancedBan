package me.leoko.advancedban.utils

import java.net.HttpURLConnection
import java.net.URI

object NetworkUtils {
    private const val DEFAULT_TIMEOUT_MILLIS = 3_000

    fun openHttpConnection(url: String, timeoutMillis: Int = DEFAULT_TIMEOUT_MILLIS): HttpURLConnection {
        return (URI.create(url).toURL().openConnection() as HttpURLConnection).apply {
            connectTimeout = timeoutMillis
            readTimeout = timeoutMillis
            useCaches = false
        }
    }
}
