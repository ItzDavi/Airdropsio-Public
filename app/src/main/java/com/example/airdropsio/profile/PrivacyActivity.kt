package com.example.airdropsio.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.example.airdropsio.OnSwipeTouchListener
import com.example.airdropsio.R
import com.google.android.material.switchmaterial.SwitchMaterial

class PrivacyActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        window.statusBarColor = getColor(R.color.ad_blue)

        val url = "https://www.lipsum.com/"

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }

        //Asks the user with the intent to open the external web browser
        //to show law required documents
        val privacyPolicyLayout = findViewById<LinearLayout>(R.id.privacy_policy_layout)
        privacyPolicyLayout.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        val tosLayout = findViewById<LinearLayout>(R.id.tos_policy_layout)
        tosLayout.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        val legalNotesLayout = findViewById<LinearLayout>(R.id.legal_notes_layout)
        legalNotesLayout.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        //Shared Preferences reference
        val sharedPreferences = getSharedPreferences("com.example.airdropsio", MODE_PRIVATE)

        //SharedPref editor
        val editor = sharedPreferences.edit()

        val advSwitch = findViewById<SwitchMaterial>(R.id.adv_preferences_switch)

        //Advertisements consent switch state listener
        //If checked add true to the shared pref
        //If !checked add false to the shared pref
        advSwitch.isChecked = sharedPreferences.getBoolean("advertisements_preferences", true)
        advSwitch.setOnCheckedChangeListener { it, _ ->
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            if(!advSwitch.isChecked) {
                //Edit the shared pref
                editor.putBoolean("advertisements_preferences", false)

                //Apply the edit
                editor.apply()

            } else {
                editor.putBoolean("advertisements_preferences", true)
                editor.apply()

            }
        }

        //Advertisements consent switch state listener
        //If checked add true to the shared pref
        //If !checked add false to the shared pref
        val cookieSwitch = findViewById<SwitchMaterial>(R.id.cookie_preferences_switch)
        cookieSwitch.isChecked = sharedPreferences.getBoolean("cookie_preferences", true)
        cookieSwitch.setOnCheckedChangeListener { it, _ ->
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            if (!cookieSwitch.isChecked) {
                editor.putBoolean("cookie_preferences", false)
                editor.apply()

            } else {
                editor.putBoolean("cookie_preferences", true)
                editor.apply()

            }
        }

        val rootLayout = findViewById<ScrollView>(R.id.privacy_scroll_view)
        rootLayout.setOnTouchListener(object : OnSwipeTouchListener(this@PrivacyActivity) {
            override fun onSwipeRight() {
                super.onSwipeRight()
                rootLayout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                val intent = Intent(this@PrivacyActivity, ProfileActivity::class.java)
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