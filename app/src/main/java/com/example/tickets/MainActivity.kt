package com.example.tickets

import EmailSender
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tickets.db.AppDatabase
import com.example.tickets.db.Ticket
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TicketsAdapter
    private lateinit var departureSpinner: Spinner
    private lateinit var destinationSpinner: Spinner
    private lateinit var airlineSpinner: Spinner
    private lateinit var filterButton: Button
    private lateinit var appDatabase: AppDatabase
    private lateinit var sharedPrefs: SharedPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        sharedPrefs = SharedPrefs(this)
        appDatabase = AppDatabase.getDatabase(this)

        checkAuthorization()

        departureSpinner = findViewById(R.id.departureSpinner)
        destinationSpinner = findViewById(R.id.destinationSpinner)
        airlineSpinner = findViewById(R.id.airlineSpinner)
        filterButton = findViewById(R.id.filterButton)
        recyclerView = findViewById(R.id.ticketsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val cities = listOf("Москва", "Санкт-Петербург", "Сочи", "Казань", "Екатеринбург")
        val airlines = listOf("Аэрофлот", "S7 Airlines", "Победа", "Уральские авиалинии")

        val departureList = listOf("Откуда") + cities
        val destinationList = listOf("Куда") + cities
        val airlineList = listOf("Выбор компании") + airlines

        val departureAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departureList)
        departureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val destinationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, destinationList)
        destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val airlineAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, airlineList)
        airlineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        departureSpinner.adapter = departureAdapter
        destinationSpinner.adapter = destinationAdapter
        airlineSpinner.adapter = airlineAdapter

        departureSpinner.setSelection(0, false)
        destinationSpinner.setSelection(0, false)
        airlineSpinner.setSelection(0, false)

        filterButton.setOnClickListener {
            if (departureSpinner.selectedItemPosition == 0 ||
                destinationSpinner.selectedItemPosition == 0) {
                Toast.makeText(this, "Выберите города отправления и назначения",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val departure = departureSpinner.selectedItem.toString()
            val destination = destinationSpinner.selectedItem.toString()
            val airline = if (airlineSpinner.selectedItemPosition == 0) {
                null
            } else {
                airlineSpinner.selectedItem.toString()
            }

            lifecycleScope.launch {
                val tickets = if (airline == null) {
                    appDatabase.ticketDao().getFilteredTickets(departure, destination)
                } else {
                    appDatabase.ticketDao().getFilteredTicketsByAirline(departure, destination, airline)
                }

                withContext(Dispatchers.Main) {
                    adapter = TicketsAdapter(tickets) { ticket ->
                        showPurchaseBottomSheet(ticket)
                    }
                    recyclerView.adapter = adapter
                }
            }
        }

        lifecycleScope.launch {
            addSampleTickets()
            loadTickets()
        }
    }

    private fun checkAuthorization() {
        if (!sharedPrefs.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showPurchaseBottomSheet(ticket: Ticket) {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_purchase, null)
        bottomSheet.setContentView(view)

        val emailInput = view.findViewById<EditText>(R.id.emailInput)
        val confirmButton = view.findViewById<Button>(R.id.confirmButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)

        confirmButton.setOnClickListener {
            val email = emailInput.text.toString().trim()

            when {
                email.isEmpty() -> {
                    emailInput.error = "Введите email"
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailInput.error = "Неверный формат email"
                }
                else -> {
                    completePurchase(ticket, email)
                    bottomSheet.dismiss()
                }
            }
        }

        cancelButton.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun completePurchase(ticket: Ticket, email: String) {
        val ticketCode = generateTicketCode()

        val subject = "Ваш билет: ${ticket.departure} → ${ticket.destination}"
        val message = """
            Спасибо за покупку!
            
            Маршрут: ${ticket.departure} → ${ticket.destination}
            Дата: ${ticket.date}
            Время: ${ticket.time}
            Авиакомпания: ${ticket.airline}
            Цена: ${ticket.price}
            Номер билета: $ticketCode
            
            Билет будет отправлен на email: $email
        """.trimIndent()

        lifecycleScope.launch(Dispatchers.IO) {
            val sender = EmailSender(
                username = "mygamecatalog@mail.ru",
                password = "1LexBJDy4Ms14eHkvrMF"
            )

            val isSent = sender.sendEmail(
                recipient = email,
                subject = subject,
                message = message
            )

            withContext(Dispatchers.Main) {
                if (isSent) {
                    Toast.makeText(
                        this@MainActivity,
                        "Билет успешно оформлен! Номер: $ticketCode",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Ошибка отправки письма. Попробуйте позже.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun generateTicketCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    private suspend fun addSampleTickets() {
        if (appDatabase.ticketDao().getAllTickets().isEmpty()) {
            val tickets = listOf(
                Ticket(
                    departure = "Москва",
                    destination = "Санкт-Петербург",
                    price = "5 900 ₽",
                    date = "15.05.2023",
                    time = "10:30",
                    airline = "Аэрофлот",
                    airlineImageResId = R.drawable.aeroflot,
                    destinationImageResId = R.drawable.sp
                ),
                Ticket(
                    departure = "Москва",
                    destination = "Сочи",
                    price = "8 200 ₽",
                    date = "16.05.2023",
                    time = "14:45",
                    airline = "S7 Airlines",
                    airlineImageResId = R.drawable.s7,
                    destinationImageResId = R.drawable.sochi
                ),
                Ticket(
                    departure = "Санкт-Петербург",
                    destination = "Москва",
                    price = "4 500 ₽",
                    date = "17.05.2023",
                    time = "18:20",
                    airline = "Победа",
                    airlineImageResId = R.drawable.pobeda,
                    destinationImageResId = R.drawable.moscow
                ),
                Ticket(
                    departure = "Казань",
                    destination = "Москва",
                    price = "3 800 ₽",
                    date = "18.05.2023",
                    time = "09:15",
                    airline = "Аэрофлот",
                    airlineImageResId = R.drawable.aeroflot,
                    destinationImageResId = R.drawable.moscow
                ),
                Ticket(
                    departure = "Екатеринбург",
                    destination = "Москва",
                    price = "6 700 ₽",
                    date = "19.05.2023",
                    time = "12:00",
                    airline = "Уральские авиалинии",
                    airlineImageResId = R.drawable.ural,
                    destinationImageResId = R.drawable.moscow
                )
            )
            tickets.forEach { appDatabase.ticketDao().insert(it) }
        }
    }

    private suspend fun loadTickets() {
        val tickets = appDatabase.ticketDao().getAllTickets()
        withContext(Dispatchers.Main) {
            adapter = TicketsAdapter(tickets) { ticket ->
                showPurchaseBottomSheet(ticket)
            }
            recyclerView.adapter = adapter
        }
    }
}