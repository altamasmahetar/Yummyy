package com.example.pr2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class SignInActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        val email = findViewById<EditText>(R.id.etLoginEmail)
        val password = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvGoSignUp)
        val tvForgot = findViewById<TextView>(R.id.tvForgotPassword)

        val dbHelper = DatabaseHelper(this)

        btnLogin.setOnClickListener {

            val enteredEmail = email.text.toString().trim()
            val enteredPassword = password.text.toString()

            if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
                Toast.makeText(
                    this, "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                email.error = "Enter valid email"
                return@setOnClickListener
            }

            val isValidUser = dbHelper.checkUserLogin(enteredEmail, enteredPassword)

            if (isValidUser) {
                val pref = getSharedPreferences("JournalData", MODE_PRIVATE)
                pref.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("email", enteredEmail)
                    .apply()

                val userProfile = dbHelper.getUserProfile(enteredEmail)
                val name = userProfile?.getAsString(DatabaseHelper.COL_USER_NAME) ?: ""

                Toast.makeText(
                    this, "Login Successful via SQLite DB!",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("name", name)
                intent.putExtra("openFoodHome", true)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(
                    this, "Invalid email or password in Database",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tvSignUp.setOnClickListener {
            startActivity(
                Intent(this, SignUpActivity::class.java)
            )
        }

        tvForgot.setOnClickListener {
            Toast.makeText(
                this,
                "Please register again if you forget your password",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}