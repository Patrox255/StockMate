package com.example.stockmate.data.http.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val baseDelayMs: Long = 500L,
    private val maxDelayMs: Long = 8_000L,
    private val retryableStatusCodes: Set<Int> = setOf(429,500,502,503,504)
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var lastResponse: Response? = null

        while (attempt <= maxRetries) {
            lastResponse?.close()

            val response = try {
                chain.proceed(request)
            } catch (e: IOException) {
                if (attempt == maxRetries) throw e
                sleepBeforeRetry(attempt, retryAfterHeader = null)
                attempt++
                continue
            }

            if (response.code !in retryableStatusCodes || attempt == maxRetries) {
                return response
            }

            lastResponse = response
            val retryAfter = response.header("Retry-After")
            sleepBeforeRetry(attempt, retryAfter)
            attempt++
        }

        return lastResponse ?: chain.proceed(request)
    }

    private fun sleepBeforeRetry(attempt: Int, retryAfterHeader: String?) {
        val delay = retryAfterHeader?.toLongOrNull()?.times(1000)
            ?: run {
                val exponential = baseDelayMs * (2.0.pow(attempt.toDouble())).toLong()
                val jitter = Random.nextLong(0, baseDelayMs)
                min(exponential + jitter, maxDelayMs)
            }
        Thread.sleep(delay)
    }
}