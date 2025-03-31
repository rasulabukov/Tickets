package com.example.tickets

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tickets.db.AppDatabase
import com.example.tickets.db.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvSignIn: TextView
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        appDatabase = AppDatabase.getDatabase(this)

        etFirstName = findViewById(R.id.fname)
        etLastName = findViewById(R.id.lname)
        etEmail = findViewById(R.id.email)
        etPassword = findViewById(R.id.password)
        btnSignUp = findViewById(R.id.signup_btn)
        tvSignIn = findViewById(R.id.signin)

        btnSignUp.setOnClickListener { registerUser() }
        tvSignIn.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showErrorDialog("Все поля должны быть заполнены")
            return
        }

        lifecycleScope.launch {
            val existingUser = withContext(Dispatchers.IO) {
                appDatabase.userDao().getUserByEmail(email)
            }
            if (existingUser != null) {
                withContext(Dispatchers.Main) {
                    showErrorDialog("Пользователь с таким email уже существует")
                }
                return@launch
            }

            val newUser = User(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = password
            )

            withContext(Dispatchers.IO) {
                appDatabase.userDao().insert(newUser)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Регистрация прошла успешно!",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                finish()
            }
        }

    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}