package com.domedav.sportmatenobullshit.data

import kotlinx.serialization.Serializable

// Login Response (from WebView interception)
@Serializable
data class LoginResponse(
    val data: LoginData?,
    val status: String?
)

@Serializable
data class LoginData(
    val user: User?,
    val token: String?
)

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String
)

// GraphQL Request
@Serializable
data class GraphQlRequest(
    val query: String,
    val variables: Map<String, String?>
)

// GraphQL Response
@Serializable
data class GraphQlResponse(
    val data: GraphQlData?
)

@Serializable
data class GraphQlData(
    val me: Me?
)

@Serializable
data class Me(
    val qr_code: String?
)
