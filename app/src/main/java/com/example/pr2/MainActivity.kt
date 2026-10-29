package com.example.pr2

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var registrationScreen: View
    private lateinit var yummCartScreen: View
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbar: Toolbar

    private val airplaneModeReceiver = AirplaneModeReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registrationScreen = findViewById(R.id.registrationScreen)
        yummCartScreen = findViewById(R.id.yummCartScreen)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)

        // Sign In button
        btnSignIn.setOnClickListener {
            startActivity(
                Intent(this, SignInActivity::class.java)
            )
        }

        // Sign Up button
        btnSignUp.setOnClickListener {
            startActivity(
                Intent(this, SignUpActivity::class.java)
            )
        }

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.nav_categories -> {
                    loadFragment(CategoriesFragment())
                    true
                }

                R.id.nav_cart -> {
                    loadFragment(CartFragment())
                    true
                }

                R.id.nav_account -> {
                    loadFragment(AccountFragment())
                    true
                }

                else -> false
            }
        }

        // Open Food Home after successful login or if already logged in
        val pref = getSharedPreferences("JournalData", Context.MODE_PRIVATE)
        val isLoggedIn = pref.getBoolean("isLoggedIn", false)
        val openFoodHome = intent.getBooleanExtra("openFoodHome", false)

        if (openFoodHome || isLoggedIn) {
            openYummCart()
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        registerReceiver(airplaneModeReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(airplaneModeReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Options Menu (Practical 3)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.menu_user_profile -> {
                registrationScreen.visibility = View.GONE
                yummCartScreen.visibility = View.VISIBLE
                bottomNavigation.selectedItemId = R.id.nav_account
                loadFragment(AccountFragment())
                true
            }

            R.id.menu_about_us -> {
                AlertDialog.Builder(this)
                    .setTitle("About Us")
                    .setMessage("YummCart Food Ordering Application\nVersion 1.0\nDelicious food delivered to your doorstep.")
                    .setPositiveButton("OK", null)
                    .show()
                true
            }

            R.id.menu_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menu_logout -> {
                showLogoutDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Logout Dialog (Practical 3)
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                val pref = getSharedPreferences("JournalData", Context.MODE_PRIVATE)
                pref.edit().putBoolean("isLoggedIn", false).apply()
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Open Food App
    fun openYummCart() {

        registrationScreen.visibility = View.GONE
        yummCartScreen.visibility = View.VISIBLE

        bottomNavigation.selectedItemId = R.id.nav_home

        loadFragment(HomeFragment())
    }

    // Load Fragment
    private fun loadFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}