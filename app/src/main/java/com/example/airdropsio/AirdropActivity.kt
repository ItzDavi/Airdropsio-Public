package com.example.airdropsio

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.uk.tastytoasty.TastyToasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AirdropActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_airdrop)

        auth = Firebase.auth

        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        window.statusBarColor = getColor(R.color.ad_blue)

        val airdropExtra = intent.getStringExtra("title").toString()
        // commento messo a caso ciao davide del futuro vivo nei tuoi muri
        // by Fabio Burlone Artero
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }

        val normalLayout = findViewById<ConstraintLayout>(R.id.test_normal_layout)
        normalLayout.visibility = View.VISIBLE

        val airdropTitle = findViewById<TextView>(R.id.AD_title_details)
        val airdropDescription = findViewById<TextView>(R.id.AD_description_details)


        val airdropRuleOne = findViewById<TextView>(R.id.AD_rule_one)
        val airdropRuleTwo = findViewById<TextView>(R.id.AD_rule_two)
        val airdropRuleThree = findViewById<TextView>(R.id.AD_rule_three)

        val airdropRequirementOne = findViewById<TextView>(R.id.AD_requirements_one)
        val airdropRequirementTwo = findViewById<TextView>(R.id.AD_requirements_two)
        val airdropRequirementThree = findViewById<TextView>(R.id.AD_requirements_three)

        val blockchainTag = findViewById<ImageView>(R.id.AD_blockchain_badge)
        val categoryTag = findViewById<ImageView>(R.id.AD_category_badge)
        val otherTag = findViewById<ImageView>(R.id.AD_other_badge)

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar_airdrop)
        val progressBarCard = findViewById<CardView>(R.id.progress_bar_cardview)
        progressBar.visibility = View.VISIBLE
        progressBarCard.visibility = View.VISIBLE

        val howToEnter = findViewById<TextView>(R.id.how_to_enter)
        val howToEnterDetails = findViewById<TextView>(R.id.how_to_enter_details)

        val requirements = findViewById<TextView>(R.id.requirements)
        val requirementsDetails = findViewById<TextView>(R.id.requirements_details)

        val categoryTagTextView = findViewById<TextView>(R.id.category_tag_textview)
        val blockchainTagTextView = findViewById<TextView>(R.id.blockchain_tag_textview)
        val otherTagTextView = findViewById<TextView>(R.id.other_tag_textview)

        val airdropBanner = findViewById<ImageView>(R.id.airdrop_banner_imageview)

        val scrollView = findViewById<NestedScrollView>(R.id.details_scroll_view)
        swipeHandler(scrollView)

        //Function to load the airdrop details
        lifecycleScope.launch {
            delay(500L)

            loadAirdrop(
                airdropTitle,
                airdropDescription,
                airdropRuleOne,
                airdropRuleTwo,
                airdropRuleThree,
                blockchainTag,
                categoryTag,
                otherTag,
                airdropRequirementOne,
                airdropRequirementTwo,
                airdropRequirementThree,
                airdropExtra,
                progressBar,
                progressBarCard,
                howToEnter,
                howToEnterDetails,
                requirements,
                requirementsDetails,
                categoryTagTextView,
                blockchainTagTextView,
                otherTagTextView,
                airdropBanner
            )
        }

    }

    @SuppressLint("SetTextI18n")
    private fun loadAirdrop(
        airdropTitle: TextView,
        airdropDescription: TextView,
        airdropRuleOne: TextView,
        airdropRuleTwo: TextView,
        airdropRuleThree: TextView,
        blockchainTag: ImageView,
        categoryTag: ImageView,
        otherTag: ImageView,
        airdropRequirementOne: TextView,
        airdropRequirementTwo: TextView,
        airdropRequirementThree: TextView,
        airdropExtra: String,
        progressBar: ProgressBar,
        progressBarCard: CardView,
        howToEnter: TextView,
        howToEnterDetails: TextView,
        requirements: TextView,
        requirementsDetails: TextView,
        categoryTagTextView: TextView,
        blockchainTagTextView: TextView,
        otherTagTextView: TextView,
        airdropBanner: ImageView
    ) {

        //Query to get the correct airdrop from the extras sent with the intent
        database.collection("Airdrops").whereEqualTo("title", airdropExtra).get()
            .addOnSuccessListener { it ->
                for (document in it) {

                    //Load every Airdrop information
                    airdropTitle.text = document.data["title"].toString()
                    airdropDescription.text = document.data["description"].toString()
                    val image = document.data["bannerURL"].toString()

                    Glide.with(this).load(image).into(airdropBanner)

                    //Split rules array from Firebase
                    val rules = arrayOf(document.data["rules"].toString())
                    val delimiter = ","
                    val rulesSplitted = rules.toList().toString()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(" ", "")
                        .split(delimiter)

                    //Set the rules
                    "1. ${rulesSplitted[0]}".also { airdropRuleOne.text = it }
                    "2. ${rulesSplitted[1]}".also { airdropRuleTwo.text = it }
                    "3. ${rulesSplitted[2]}".also { airdropRuleThree.text = it }

                    //Split the tags array from Firebase
                    val tagsArray = arrayOf(document.data["tags"])
                    val tagsSplitted = tagsArray.toList().toString()
                        .replace("[", "")
                        .replace("]", "")
                        .replace(" ", "")
                        .split(delimiter)

                    //Set the tags
                    val tagOne = tagsSplitted[0]
                    val tagTwo = tagsSplitted[1]
                    val tagThree = tagsSplitted[2]

                    if (tagOne == "eth") {
                        Glide.with(this).load(R.drawable.eth_logo).into(blockchainTag)
                        blockchainTagTextView.text = "ETH"

                    } else {
                        Glide.with(this).load(R.drawable.solana_logo).into(blockchainTag)
                        blockchainTagTextView.text = "SOL"
                    }

                    when (tagTwo) {
                        "nft" -> {
                            Glide.with(this).load(R.drawable.icons8_nft_64).into(categoryTag)
                            categoryTagTextView.text = "NFT"
                        }

                        "igo" -> {
                            Glide.with(this).load(R.drawable.icons8_gamefi_64).into(categoryTag)
                            categoryTagTextView.text = "IGO"
                        }

                        "ico" -> {
                            Glide.with(this).load(R.drawable.icons8_ico_64).into(categoryTag)
                            categoryTagTextView.text = "ICO"
                        }

                        "dao" -> {
                            Glide.with(this).load(R.drawable.icons8_rete_decentralizzata_64).into(categoryTag)
                            categoryTagTextView.text = "DAO"
                        }
                    }

                    when (tagThree) {
                        "new" -> {
                            Glide.with(this).load(R.drawable.ic_baseline_new_releases_28).into(otherTag)
                            otherTagTextView.text = "NEW"
                        }

                        "verified" -> {
                            Glide.with(this).load(R.drawable.ic_baseline_verified_24).into(otherTag)
                            otherTagTextView.text = "VERIFIED"
                        }

                        "hot" -> {
                            Glide.with(this).load(R.drawable.ic_baseline_whatshot_24).into(otherTag)
                            otherTagTextView.text = "HOT"
                        }

                        "old" -> {
                            Glide.with(this).load(R.drawable.ic_baseline_notifications_paused_28).into(otherTag)
                            otherTagTextView.text = "OLD"
                        }
                    }
                }

                lifecycleScope.launch(Dispatchers.Main) {
                    airdropTitle.visibility = View.VISIBLE
                    airdropDescription.visibility = View.VISIBLE
                    categoryTag.visibility = View.VISIBLE
                    blockchainTag.visibility = View.VISIBLE
                    otherTag.visibility = View.VISIBLE
                    airdropRuleOne.visibility = View.VISIBLE
                    airdropRuleTwo.visibility = View.VISIBLE
                    airdropRuleThree.visibility = View.VISIBLE
                    airdropRequirementOne.visibility = View.VISIBLE
                    airdropRequirementTwo.visibility = View.VISIBLE
                    airdropRequirementThree.visibility = View.VISIBLE
                    howToEnter.visibility = View.VISIBLE
                    howToEnterDetails.visibility = View.VISIBLE
                    requirements.visibility = View.VISIBLE
                    requirementsDetails.visibility = View.VISIBLE
                    blockchainTagTextView.visibility = View.VISIBLE
                    otherTagTextView.visibility = View.VISIBLE
                    categoryTagTextView.visibility = View.VISIBLE
                    airdropBanner.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    progressBarCard.visibility = View.GONE
                }
            }

            .addOnFailureListener {
                TastyToasty.makeText(this, "Failed while loading the Airdrop", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun swipeHandler(view: NestedScrollView) {
        view.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeRight() {
                super.onSwipeRight()

                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }
}