package com.domedav.sportmatenobullshit.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class SportmateApi {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getQrCode(token: String, deviceId: String): String? {
        val query = """
            query MyQrCode {
              me {
                qr_code
              }
            }
        """.trimIndent()

        try {
            val response = client.post("https://app.sportmateclub.com/graphql?op=MyQrCode") {
                header("Authorization", token)
                // Required Headers
                header("x-api-consumer", "app")
                header("x-api-version", "2")
                header("x-app-identifier", "com.rrapps.sportmate")
                header("x-app-version", "6.3.0")
                contentType(ContentType.Application.Json)

                setBody(GraphQlRequest(
                    query = query,
                    variables = mapOf(
                        "device_id" to deviceId,
                        "coordinates" to null
                    )
                ))
            }

            val graphQlResponse: GraphQlResponse = response.body()
            return graphQlResponse.data?.me?.qr_code
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
