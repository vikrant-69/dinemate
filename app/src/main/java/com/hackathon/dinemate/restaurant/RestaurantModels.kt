package com.hackathon.dinemate.restaurant

data class Restaurant(
    val name: String,
    val distance: String,
    val description: String,
    val image: Int,
    val rating: Double,
    val address: String? = null,
    val website: String? = null,
    val phone: String? = null,
    val category: String? = null
)

