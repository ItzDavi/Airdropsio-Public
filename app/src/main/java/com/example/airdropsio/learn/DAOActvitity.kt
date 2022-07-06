package com.example.airdropsio.learn

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.ImageButton
import android.widget.ScrollView
import com.example.airdropsio.OnSwipeTouchListener
import com.example.airdropsio.R

class DAOActvitity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daoactvitity)

        window.statusBarColor = getColor(R.color.ad_blue)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, LearnActivity::class.java)
            startActivity(intent)

            //Override the default transition
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }

        val rootLayout = findViewById<ScrollView>(R.id.DAO_scoll_view)
        rootLayout.setOnTouchListener(object : OnSwipeTouchListener(this@DAOActvitity) {
            override fun onSwipeRight() {
                super.onSwipeRight()

                rootLayout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                val intent = Intent(this@DAOActvitity, LearnActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                finish()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, LearnActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
    }
}