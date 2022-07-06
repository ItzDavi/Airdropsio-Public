package com.example.airdropsio

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        window.statusBarColor = getColor(R.color.ad_blue)

        val background = findViewById<ImageView>(R.id.splash_screen)

        lifecycleScope.launch {
            //Custom splash screen animation
            val animation = AnimationUtils.loadAnimation(this@SplashScreenActivity, R.anim.slide_down)
            background.startAnimation(animation)
            delay(2000L)

            val intent = Intent(this@SplashScreenActivity, StarterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, R.anim.splash_fade_out)
            finish()
        }
    }
}