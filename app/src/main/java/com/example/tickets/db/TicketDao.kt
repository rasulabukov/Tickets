package com.example.tickets.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Insert
    suspend fun insert(ticket: Ticket)

    @Query("SELECT * FROM tickets")
    suspend fun getAllTickets(): List<Ticket>

    @Query("SELECT * FROM tickets WHERE departure = :departure AND destination = :destination")
    suspend fun getFilteredTickets(departure: String, destination: String): List<Ticket>
}