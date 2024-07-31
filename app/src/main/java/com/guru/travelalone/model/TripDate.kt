package com.guru.travelalone.model

data class TripDate(
    val id: Long = 0,
    val title: String,
    val location: String,
    val start_date: Long,
    val end_date : Long
)
