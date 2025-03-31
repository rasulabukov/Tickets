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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPrefs
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var tvSignUp: TextView
    private val appDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        sharedPrefs = SharedPrefs(this)
        checkIfUserLoggedIn()

        initViews()
        setupClickListeners()
    }

    private fun checkIfUserLoggedIn() {
        if (sharedPrefs.isLoggedIn()) {
            navigateToMain()
        }
    }

    private fun initViews() {
        etEmail = findViewById(R.id.email)
        etPassword = findViewById(R.id.password)
        btnSignIn = findViewById(R.id.signin_btn)
        tvSignUp = findViewById(R.id.signup)
    }

    private fun setupClickListeners() {
        btnSignIn.setOnClickListener { loginUser() }
        tvSignUp.setOnClickListener { navigateToSignUp() }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showErrorDialog("Все поля должны быть заполнены")
            return
        }

        lifecycleScope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    appDatabase.userDao().getUser(email, password)
                }

                if (user == null) {
                    showErrorDialog("Неверный email или пароль")
                } else {
                    sharedPrefs.saveLoginState(true, user.id)
                    showSuccessMessage()
                    navigateToMain()
                }
            } catch (e: Exception) {
                showErrorDialog("Ошибка при авторизации: ${e.localizedMessage}")
            }
        }
    }

    private fun showSuccessMessage() {
        Toast.makeText(
            this,
            "Авторизация прошла успешно!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToSignUp() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}