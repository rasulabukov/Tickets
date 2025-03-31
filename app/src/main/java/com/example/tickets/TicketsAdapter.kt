package com.example.tickets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.tickets.db.Ticket

class TicketsAdapter(
    private val tickets: List<Ticket>,
    private val onBuyClick: (Ticket) -> Unit
) : RecyclerView.Adapter<TicketsAdapter.TicketViewHolder>() {

    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val datetimeTextView: TextView = itemView.findViewById(R.id.datetimeTextView)
        val airlineTextView: TextView = itemView.findViewById(R.id.airlineTextView)
        val airlineImage: ImageView = itemView.findViewById(R.id.airlineImage)
        val buyButton: AppCompatButton = itemView.findViewById(R.id.buy_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ticket_item, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]
        holder.priceTextView.text = ticket.price
        holder.nameTextView.text = "${ticket.departure} → ${ticket.destination}"
        holder.datetimeTextView.text = "${ticket.date} • ${ticket.time}"
        holder.airlineTextView.text = ticket.airline
        holder.airlineImage.setImageResource(ticket.airlineImageResId)

        holder.buyButton.setOnClickListener {
            onBuyClick(ticket)
        }
    }

    override fun getItemCount(): Int = tickets.size
}