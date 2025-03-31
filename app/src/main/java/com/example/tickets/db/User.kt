package com.example.tickets.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)