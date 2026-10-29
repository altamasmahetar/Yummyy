package com.example.pr2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<View>(R.id.splashLogo)
        val title = findViewById<View>(R.id.splashTitle)
        val tagline = findViewById<View>(R.id.splashTagline)

        // Animation: scale & fade in
        logo.alpha = 0f
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f

        title.alpha = 0f
        tagline.alpha = 0f

        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .start()

        title.animate()
            .alpha(1f)
            .setDuration(1200)
            .start()

        tagline.animate()
            .alpha(1f)
            .setDuration(1400)
            .start()

        // Delay for splash screen then load main home screen
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2500)
    }
}