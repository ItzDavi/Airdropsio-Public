package com.example.airdropsio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.example.airdropsio.learn.LearnActivity
import com.example.airdropsio.profile.ProfileActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = getColor(R.color.ad_blue)

        val learnButton = findViewById<ImageButton>(R.id.learnButton)
        learnButton.setOnClickListener {
            val intent = Intent(this, LearnActivity::class.java)
            startActivity(intent)
        }

        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val constraintLayout1 = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.airdropLayout)
        constraintLayout1.setOnClickListener {
            val intent = Intent(this, AirdropActivity::class.java)
            startActivity(intent)
        }

        val constraintLayout2 = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.airdropLayout2)
        constraintLayout2.setOnClickListener {
            val intent = Intent(this, AirdropActivity::class.java)
            startActivity(intent)
        }

        val newmainbutton = findViewById<Button>(R.id.newmainbutton)
        newmainbutton.setOnClickListener {
            val intent = Intent(this, NewMainActivity::class.java)
            startActivity(intent)
        }
    }
}