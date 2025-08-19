package com.example.transtools.api.model

data class LoginResponse(
    val apiData: ApiData,
    val dbData: List<DbData>
)

data class ApiData(
    val error: Boolean,
    val uid: String,
    val user: User
)

data class DbData(
    val dept_code: String,
    val store_code: String,
    val dept_name: String,
    val store_name: String
)
