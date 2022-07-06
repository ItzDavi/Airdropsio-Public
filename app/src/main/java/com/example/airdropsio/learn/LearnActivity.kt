package com.example.airdropsio.learn

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.airdropsio.OnSwipeTouchListener
import com.example.airdropsio.R

class LearnActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn)

        window.statusBarColor = getColor(R.color.ad_blue)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            finish()
            //Override the default transition
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        }

        val rootLayout = findViewById<ConstraintLayout>(R.id.root_layout)
        rootLayout.setOnTouchListener(object : OnSwipeTouchListener(this@LearnActivity) {
            override fun onSwipeUp() {
                super.onSwipeUp()
                rootLayout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                finish()
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
            }
        })

        val backTextView = findViewById<TextView>(R.id.back_home_textview)
        backTextView.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            finish()
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        }

        val icoLayout = findViewById<LinearLayout>(R.id.learnICOLayout)
        val icoButton = findViewById<ImageButton>(R.id.ICO_button)
        icoLayout.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, ICOActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
        icoButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, ICOActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }

        val nftLayout = findViewById<LinearLayout>(R.id.learnNFTLayout)
        val nftButton = findViewById<ImageButton>(R.id.NFT_button)
        nftLayout.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, NFTActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
        nftButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, NFTActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }

        val daoLayout = findViewById<LinearLayout>(R.id.learnDAOLayout)
        val daoButton = findViewById<ImageButton>(R.id.DAO_button)
        daoLayout.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, DAOActvitity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
        daoButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, DAOActvitity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }

        val igoLayout = findViewById<LinearLayout>(R.id.learnIGOLayout)
        val igoButton = findViewById<ImageButton>(R.id.IGO_button)
        igoLayout.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, IGOActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
        igoButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, IGOActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
        finish()
    }
}