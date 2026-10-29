package com.example.pr2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class SignUpActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val name = findViewById<EditText>(R.id.etName)
        val email = findViewById<EditText>(R.id.etEmail)
        val phone = findViewById<EditText>(R.id.etPhone)
        val password = findViewById<EditText>(R.id.etPassword)
        val confirm = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvGoLogin)

        val dbHelper = DatabaseHelper(this)

        btnRegister.setOnClickListener {

            val n = name.text.toString().trim()
            val e = email.text.toString().trim()
            val p = phone.text.toString().trim()
            val pass = password.text.toString()
            val confirmPass = confirm.text.toString()

            if (n.isEmpty() || e.isEmpty() || p.isEmpty() ||
                pass.isEmpty() || confirmPass.isEmpty()
            ) {
                Toast.makeText(
                    this, "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                email.error = "Enter valid email"
                return@setOnClickListener
            }

            if (p.length != 10) {
                phone.error = "Enter 10 digit mobile number"
                return@setOnClickListener
            }

            if (pass.length < 6) {
                password.error = "Minimum 6 characters"
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                confirm.error = "Passwords do not match"
                return@setOnClickListener
            }

            val success = dbHelper.registerUser(n, e, p, pass)

            if (success) {
                val pref = getSharedPreferences("JournalData", MODE_PRIVATE)
                pref.edit()
                    .putString("name", n)
                    .putString("email", e)
                    .putString("phone", p)
                    .apply()

                Toast.makeText(
                    this, "Registration Successful in SQLite DB!",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(this, SignInActivity::class.java)
                )
                finish()
            } else {
                Toast.makeText(
                    this, "Email already registered in Database",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tvLogin.setOnClickListener {
            startActivity(
                Intent(this, SignInActivity::class.java)
            )
        }
    }
}