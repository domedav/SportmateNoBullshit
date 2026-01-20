package com.domedav.sportmatenobullshit.utils

import android.util.Log
import android.webkit.JavascriptInterface
import com.domedav.sportmatenobullshit.data.LoginResponse
import kotlinx.serialization.json.Json

class WebAppInterface(private val onTokenReceived: (String) -> Unit) {

    @JavascriptInterface
    fun onLoginResponse(responseBody: String) {
        try {
            val json = Json { ignoreUnknownKeys = true }
            val response = json.decodeFromString<LoginResponse>(responseBody)

            val token = response.data?.token
            if (token != null) {
                onTokenReceived(token)
            } else {
                Log.e("WebAppInterface", "Token is NULL in response")
            }
        } catch (e: Exception) {
            Log.e("WebAppInterface", "JSON Parsing Error", e)
        }
    }
}
