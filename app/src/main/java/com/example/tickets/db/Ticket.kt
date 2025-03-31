package com.example.tickets.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tickets")
data class Ticket(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val departure: String,
    val destination: String,
    val price: String,
    val date: String,
    val time: String,
    val airline: String,
    val airlineImageResId: Int
)