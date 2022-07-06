package com.example.airdropsio.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.example.airdropsio.OnSwipeTouchListener
import com.example.airdropsio.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.uk.tastytoasty.TastyToasty

class NotificationsActivity : AppCompatActivity() {

    private lateinit var analytics: FirebaseAnalytics

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        analytics = Firebase.analytics

        window.statusBarColor = getColor(R.color.ad_blue)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }

        val newAirdropSwitch = findViewById<SwitchMaterial>(R.id.new_airdrop_notification_switch2)

        //Get the SharedPreferences referenes
        val sharedPreferences = getSharedPreferences("com.example.airdropsio", MODE_PRIVATE)

        //Create SharedPref editor
        val editor = sharedPreferences.edit()

        //New Airdrops notifications switch state listener
        //If checked add true to the shared pref
        //If !checked add false to the shared pref
        //
        //This also affects the notifications from the FCM servers
        newAirdropSwitch.isChecked = sharedPreferences.getBoolean("new_airdrops_notifications", true)
        newAirdropSwitch.setOnCheckedChangeListener { it, _ ->
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            if (!newAirdropSwitch.isChecked) {

                //Edit the shared pref
                editor.putBoolean("new_airdrops_notifications", false)

                //Apply the edit
                editor.apply()

                //Tell FCM servers that this user will not receive New Airdrops notifications
                FirebaseMessaging.getInstance().unsubscribeFromTopic("new_airdrops_notifications")
                    .addOnFailureListener {
                        TastyToasty.makeText(this, "Failed unsubscribing your notifications", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                    }
            } else {
                editor.putBoolean("new_airdrops_notifications", true)
                editor.apply()

                //Tell FCM servers that this user will receive New Airdrops notifications
                FirebaseMessaging.getInstance().subscribeToTopic("new_airdrops_notifications")
                    .addOnFailureListener {
                        TastyToasty.makeText(this, "Failed subscribing your notifications", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                    }
            }
        }

        val rootLayout = findViewById<ScrollView>(R.id.notification_scroll_view)
        rootLayout.setOnTouchListener(object : OnSwipeTouchListener(this@NotificationsActivity) {
            override fun onSwipeRight() {
                super.onSwipeRight()
                rootLayout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                val intent = Intent(this@NotificationsActivity, ProfileActivity::class.java)
                startActivity(intent)

                //Override the default transition
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                finish()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
    }
}