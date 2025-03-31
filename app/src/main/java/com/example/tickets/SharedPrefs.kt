package com.example.tickets
import android.content.Context
import android.content.SharedPreferences

class SharedPrefs(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val IS_LOGGED_IN = "is_logged_in"
        private const val USER_ID = "user_id"
    }

    fun saveLoginState(isLoggedIn: Boolean, userId: Int? = null) {
        with(sharedPreferences.edit()) {
            putBoolean(IS_LOGGED_IN, isLoggedIn)
            if (userId != null) {
                putInt(USER_ID, userId)
            }
            apply()
        }
    }

    fun isLoggedIn(): Boolean = sharedPreferences.getBoolean(IS_LOGGED_IN, false)

    fun getUserId(): Int = sharedPreferences.getInt(USER_ID, -1)

    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }
}