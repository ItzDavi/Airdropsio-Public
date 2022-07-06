package com.example.airdropsio

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.airdropsio.learn.LearnActivity
import com.example.airdropsio.profile.ProfileActivity
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.uk.tastytoasty.TastyToasty
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.properties.Delegates
import kotlinx.coroutines.*

//ciao molo, anche Andrei è stato qui

class NewMainActivity : AppCompatActivity() {

    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var analytics: FirebaseAnalytics
    private var doubleBackToExitPressedOnce by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_main)

        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        analytics = Firebase.analytics
        doubleBackToExitPressedOnce = false

        //Set status bar color
        window.statusBarColor = getColor(R.color.ad_blue)

        //Notification channel and channel manager
        val channel = NotificationChannel("0000", "general", NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        //Firebase Messaging instantiation and subscription to the default notification channel
        FirebaseMessaging.getInstance().subscribeToTopic("new_airdrops_notifications")
            .addOnFailureListener {
                TastyToasty.makeText(this, "Failed to subscribe to the notifications", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }

        val learnButton = findViewById<Button>(R.id.learn_button)
        learnButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, LearnActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
        }

        val accountButton = findViewById<ImageButton>(R.id.profile_button)
        accountButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, ProfileActivity.PERMISSION_CODE)

            } else {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                finish()
            }
        }

        val floatingButton = findViewById<CircleImageView>(R.id.list_airdrop_floatingbutton)
        floatingButton.setOnClickListener {
            val intent = Intent(this, ListAirdropActivity::class.java)
            intent.putExtra("from", "newmain")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
            finish()
        }

        //Airdrops recyclerviews declaration
        val newRecyclerView = findViewById<RecyclerView>(R.id.new_recyclerview)
        newRecyclerView.layoutManager = LinearLayoutManager(baseContext)
        newRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        newRecyclerView.visibility = View.GONE

        val verifiedRecyclerView = findViewById<RecyclerView>(R.id.verified_recyclerview)
        verifiedRecyclerView.layoutManager = LinearLayoutManager(baseContext)
        verifiedRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        verifiedRecyclerView.visibility = View.GONE

        val hotRecyclerView = findViewById<RecyclerView>(R.id.hot_recyclerview)
        hotRecyclerView.layoutManager = LinearLayoutManager(baseContext)
        hotRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hotRecyclerView.visibility = View.GONE

        val oldRecyclerView = findViewById<RecyclerView>(R.id.old_recyclerview)
        oldRecyclerView.layoutManager = LinearLayoutManager(baseContext)
        oldRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        oldRecyclerView.visibility = View.GONE

        loadAirdrops(newRecyclerView, verifiedRecyclerView, hotRecyclerView, oldRecyclerView)
    }

    private fun loadAirdrops (
        newRecyclerView: RecyclerView,
        verifiedRecyclerView: RecyclerView,
        hotRecyclerView: RecyclerView,
        oldRecyclerView: RecyclerView

    ) {

        val newData = ArrayList<ItemsViewModel>()
        val verifiedData = ArrayList<ItemsViewModel>()
        val hotData = ArrayList<ItemsViewModel>()
        val oldData = ArrayList<ItemsViewModel>()

        //Query to get the "NEW" airdrops
        database.collection("Airdrops").whereArrayContains("tags", "new").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document in it.result) {
                        val airdropTitle = document.data["title"].toString()
                        val image = document.data["bannerURL"].toString()

                        //Add the required data thanks to the ItemViewModel
                        newData.add(ItemsViewModel(image, airdropTitle))

                        //Adapter declaration and add the required data
                        val adapter = CustomAdapter(newData, baseContext)

                        //Insert adapter data to the recyclerview
                        newRecyclerView.adapter = adapter
                    }
                }
            }
            .addOnFailureListener {
                TastyToasty.makeText(this, "Failed getting new airdrops", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }

        //Same as above but for "VERIFIED" aridrops
        database.collection("Airdrops").whereArrayContains("tags", "verified").get()
                .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document in it.result) {
                        val airdropTitle = document.data["title"].toString()
                        val image = document.data["bannerURL"].toString()

                        verifiedData.add(ItemsViewModel(image, airdropTitle))
                        val adapter = CustomAdapter(verifiedData, baseContext)
                        verifiedRecyclerView.adapter = adapter

                    }
                }
            }
            .addOnFailureListener {
                TastyToasty.makeText(this, "Failed getting verified airdrops", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }

        //Same ad above but for "HOT" airdrops
        database.collection("Airdrops").whereArrayContains("tags", "hot").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document in it.result) {
                        val airdropTitle = document.data["title"].toString()
                        val image = document.data["bannerURL"].toString()

                        hotData.add(ItemsViewModel(image, airdropTitle))
                        val adapter = CustomAdapter(hotData, baseContext)
                        hotRecyclerView.adapter = adapter
                    }
                }
            }
            .addOnFailureListener {
                TastyToasty.makeText(this, "Failed getting hot airdrops", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }

        //Same as above but for "OLD" airdrops
        database.collection("Airdrops").whereArrayContains("tags", "old").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    for (document in it.result) {
                        val airdropTitle = document.data["title"].toString()
                        val image = document.data["bannerURL"].toString()

                        oldData.add(ItemsViewModel(image, airdropTitle))
                        val adapter = CustomAdapter(oldData, baseContext)
                        oldRecyclerView.adapter = adapter
                    }
                }
            }
            .addOnFailureListener {
                TastyToasty.makeText(this, "Failed getting old airdrops", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }
    }

    //onResume override to start the shimmer effect
    override fun onResume() {
        super.onResume()
        val newShimmer = findViewById<ShimmerFrameLayout>(R.id.new_AD_shimmer)
        newShimmer.startShimmer()

        val verifiedShimmer = findViewById<ShimmerFrameLayout>(R.id.verified_AD_shimmer)
        verifiedShimmer.startShimmer()

        val hotShimmer = findViewById<ShimmerFrameLayout>(R.id.hot_AD_shimmer)
        hotShimmer.startShimmer()

        val oldShimmer = findViewById<ShimmerFrameLayout>(R.id.old_AD_shimmer)
        oldShimmer.startShimmer()

        //After 1 second, show airdrops
        lifecycleScope.launch {
            delay(1000)
            showAirdrops()
        }
    }

    //Function to show the airdrops when everything's done and remove the shimmer loading effect
    private fun showAirdrops() {
        val newShimmer = findViewById<ShimmerFrameLayout>(R.id.new_AD_shimmer)
        newShimmer.visibility = View.GONE
        newShimmer.stopShimmer()

        val verifiedShimmer = findViewById<ShimmerFrameLayout>(R.id.verified_AD_shimmer)
        verifiedShimmer.visibility = View.GONE
        verifiedShimmer.stopShimmer()

        val hotShimmer = findViewById<ShimmerFrameLayout>(R.id.hot_AD_shimmer)
        hotShimmer.visibility = View.GONE
        hotShimmer.stopShimmer()

        val oldShimmer = findViewById<ShimmerFrameLayout>(R.id.old_AD_shimmer)
        oldShimmer.visibility = View.GONE
        oldShimmer.stopShimmer()

        val newRecyclerView = findViewById<RecyclerView>(R.id.new_recyclerview)
        newRecyclerView.visibility = View.VISIBLE

        val hotRecyclerView = findViewById<RecyclerView>(R.id.hot_recyclerview)
        hotRecyclerView.visibility = View.VISIBLE

        val verifiedRecyclerView = findViewById<RecyclerView>(R.id.verified_recyclerview)
        verifiedRecyclerView.visibility = View.VISIBLE

        val oldRecyclerView = findViewById<RecyclerView>(R.id.old_recyclerview)
        oldRecyclerView.visibility = View.VISIBLE
    }

    //onBack ovverride to confirm the app exit
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            finish()
            return
        }

        doubleBackToExitPressedOnce = true

        lifecycleScope.launch {
            TastyToasty.makeText(this@NewMainActivity, "Swipe again to confirm the exit", TastyToasty.SHORT, R.drawable.ic_baseline_warning_24, R.color.ad_darker_yellow, R.color.white, false).show()

            delay(2500L)
            doubleBackToExitPressedOnce = false
        }
    }
}
