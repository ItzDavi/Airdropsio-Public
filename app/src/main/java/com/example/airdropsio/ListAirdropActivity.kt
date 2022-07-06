package com.example.airdropsio

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.airdropsio.profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.uk.tastytoasty.TastyToasty
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class ListAirdropActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var databaseRef: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var completed by Delegates.notNull<Boolean>()
    private lateinit var imageURL : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_airdrop)

        auth = Firebase.auth

        storage = FirebaseStorage.getInstance()
        database = Firebase.firestore
        databaseRef = FirebaseFirestore.getInstance()
        window.statusBarColor = getColor(R.color.ad_blue)
        completed = false

        //ASA the activity starts, get the default image to prevent
        //the user uploads an Airdrop without a banner
        storage.reference.child("images").child("defaults").child("airdropsio_logo.jpg").downloadUrl
            .addOnCompleteListener {
                imageURL = it.result.toString()
            }

        //Elements declarations
        val titleEditText = findViewById<EditText>(R.id.airdrop_title_edittext)

        val descriptionEditText = findViewById<EditText>(R.id.airdrop_description_edittext)

        val ruleOneEditText = findViewById<EditText>(R.id.airdrop_first_rule_edittext)
        val ruleTwoEditText = findViewById<EditText>(R.id.airdrop_second_rule_edittext)
        val ruleThreeEditText = findViewById<EditText>(R.id.airdrop_third_rule_edittext)

        val ethRadioButton = findViewById<RadioButton>(R.id.airdrop_eth_radiobutton)
        val solRadioButton = findViewById<RadioButton>(R.id.airdrop_sol_radiobutton)

        val nftRadioButton = findViewById<RadioButton>(R.id.airdrop_nft_radiobutton)
        val icoRadioButton = findViewById<RadioButton>(R.id.airdrop_ico_radiobutton)
        val daoRadioButton = findViewById<RadioButton>(R.id.airdrop_dao_radiobutton)
        val igoRadioButton = findViewById<RadioButton>(R.id.airdrop_igo_radiobutton)

        val startDateDatePicker = findViewById<DatePicker>(R.id.start_date_datepicker)
        val endDateDatePicker = findViewById<DatePicker>(R.id.end_date_datepicker)

        //Loose focus and close keyboard when pressing the screen
        val scrollviewLayout = findViewById<ConstraintLayout>(R.id.list_airdrop_scrollview_constraint)
        scrollviewLayout.setOnClickListener {
            when {
                titleEditText.hasFocus() -> {
                    titleEditText.clearFocus()
                    hideSoftKeyboard(titleEditText)

                }
                descriptionEditText.hasFocus() -> {
                    descriptionEditText.clearFocus()
                    hideSoftKeyboard(descriptionEditText)

                }
                ruleOneEditText.hasFocus() -> {
                    ruleOneEditText.clearFocus()
                    hideSoftKeyboard(ruleOneEditText)

                }
                ruleTwoEditText.hasFocus() -> {
                    ruleTwoEditText.clearFocus()
                    hideSoftKeyboard(ruleTwoEditText)

                }
                ruleThreeEditText.hasFocus() -> {
                    ruleThreeEditText.clearFocus()
                    hideSoftKeyboard(ruleThreeEditText)
                }
            }
        }

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            //Performs an haptic feedback (short phone vibration) to
            // let the user know the button was pressed correctly
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            //Checks if the airdrop banner has already been uploaded to the storage
            //if so, it deletes it before the user close the activity
            if (completed){
                val storageRef = storage.reference
                val currentAirdrop = titleEditText.text.toString()

                //Query to get the correct banner in the storage
                val imagesRef: StorageReference = storageRef.child("images").child("airdrops").child(currentAirdrop).child("banner")

                imagesRef.delete()
                    .addOnFailureListener {
                        TastyToasty.makeText(this, "Failed deleting the image", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                    }
            }

            //Check from which activity this activity was opened and
            //redirect to the correct activity
            val from = intent.getStringExtra("from")
            if (from == "newmain") {
                val intent = Intent(this, NewMainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
                finish()

            } else if (from == "profile") {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
                finish()
            }
        }

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        //Airdrop banner imageview declaration
        val airdropBannerImage = findViewById<ImageView>(R.id.airdrop_banner_upload)
        airdropBannerImage.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            progressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            //Check for external storage permissions, if not, asks for them
            // and clear the flag NOT_TOUCHABLE
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, ProfileActivity.PERMISSION_CODE)

                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            } else{

                //Check if the Airdrop title has a value to use it in the storage
                //if not, send a faboulous TastyToasty
                if (titleEditText.text.isEmpty()) {
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    TastyToasty.makeText(this, "You must your airdrop a title before uploading the banner", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                } else {
                    pickImageFromGallery()
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        }

        val listAirdropButton = findViewById<Button>(R.id.list_airdrop_button)
        listAirdropButton.setOnClickListener {
            listAirdropButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            //Check if the inputs from the user in all the values asked are correct and valid
            //Checks for types, length, correctness and validity of all inputs
            //This manages also the custom progressbar at the bottom of the screen
            if (!checkInputs(titleEditText, descriptionEditText, ruleOneEditText, ruleTwoEditText, ruleThreeEditText, airdropBannerImage, listAirdropButton, progressBar)) {
                progressBar.visibility = View.GONE
                listAirdropButton.isEnabled = true
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            } else {
                //Yes
                addAirdropDatabase(titleEditText, descriptionEditText, ruleOneEditText, ruleTwoEditText, ruleThreeEditText, startDateDatePicker, endDateDatePicker, ethRadioButton, solRadioButton, nftRadioButton, icoRadioButton, daoRadioButton, igoRadioButton, progressBar)
            }
        }
    }

    //Function to hide the keyboard
    private fun Activity.hideSoftKeyboard(editText: EditText){
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    //This function checks all the user inputs
    private fun checkInputs (
        titleEditText: EditText,
        descriptionEditText: EditText,
        ruleOneEditText: EditText,
        ruleTwoEditText: EditText,
        ruleThreeEditText: EditText,
        airdropBannerImage: ImageView,
        listButton: Button,
        progressBar: ProgressBar
        ): Boolean {

        //Flag to return to the if (!checkInputs..)
        val flag: Boolean

        when {
            //If the title edittext is empty
            TextUtils.isEmpty(titleEditText.text.toString()) -> {
                TastyToasty.makeText(this, "You must give your airdrop a title", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                flag = false
                listButton.isEnabled = true
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            }

            //If the description edittext is empty
            TextUtils.isEmpty(descriptionEditText.text.toString()) -> {
                TastyToasty.makeText(this, "You must give your airdrop a description", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                flag = false
                listButton.isEnabled = true
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

            //If the rule one edittext is empty
            TextUtils.isEmpty(ruleOneEditText.text.toString()) -> {
                TastyToasty.makeText(this, "You must specify three rules for your airdrop", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                flag = false
                listButton.isEnabled = true
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

            //If the rule two edittext is empty
            TextUtils.isEmpty(ruleTwoEditText.text.toString()) -> {
                TastyToasty.makeText(this, "You must specify three rules for your airdrop", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                flag = false
                listButton.isEnabled = true
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

            //If the rule three edittext is empty
            TextUtils.isEmpty(ruleThreeEditText.text.toString()) -> {
                TastyToasty.makeText(this, "You must specify three rules for your airdrop", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                flag = false
                listButton.isEnabled = true
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

            //If the Airdrop banner image is null
            airdropBannerImage.drawable == null-> {
                TastyToasty.makeText(this, "You must upload a banner for your airdrop", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                flag = false
                listButton.isEnabled = true
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
            else -> {
                //Everything's fine
                flag = true
            }
        }

        return flag
    }

    //Function to add the Airdrop to the database, dont go to the end of this
    private fun addAirdropDatabase(titleEditText: EditText, descriptionEditText: EditText, ruleOneEditText: EditText, ruleTwoEditText: EditText, ruleThreeEditText: EditText, startDateDatePicker: DatePicker, endDateDatePicker: DatePicker, ethRadioButton: RadioButton, solRadioButton: RadioButton, nftRadioButton: RadioButton, icoRadioButton: RadioButton, daoRadioButton: RadioButton, igoRadioButton: RadioButton, progressBar: ProgressBar
    ) {
        val currentUser = auth.currentUser
        val listButton = findViewById<Button>(R.id.list_airdrop_button)

        var blockchain = ""
        var category = ""
        var airdropsCount: Int

        //Get everything from the edittexts
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val ruleOne = ruleOneEditText.text.toString()
        val ruleTwo = ruleTwoEditText.text.toString()
        val ruleThree = ruleThreeEditText.text.toString()

        //Get the Airdrop starting day
        var startDay = startDateDatePicker.dayOfMonth
        if (startDay < 10) {
            val startDayString = "0$startDay"
            startDay = startDayString.toInt()
        }

        //Get the Airdrop starting month
        var startMonth = startDateDatePicker.month + 1
        if (startMonth < 10) {
            val startMonthString = "0$startMonth"
            startMonth = startMonthString.toInt()
        }

        val startYear = startDateDatePicker.year
        val startDate = "$startYear-$startMonth-$startDay"

        //Format the date for Firebase
        var formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val startFormattedDate = formatter.parse(startDate) as Date

        //Get the Airdrop ending day
        var endDay = endDateDatePicker.dayOfMonth
        if (endDay < 10) {
            val endDayString = "0$endDay"
            endDay = endDayString.toInt()
        }

        //Get the Airdrop ending month
        var endMonth = endDateDatePicker.month + 1
        if (endMonth < 10) {
            val endMonthString = "0$endMonth"
            endMonth = endMonthString.toInt()
        }
        val endYear = endDateDatePicker.year
        val endDate = "$endYear-$endMonth-$endDay"

        //Format the ending date
        formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val endFormattedDate = formatter.parse(endDate) as Date

        //Assign the correct Airdrop blockchain
        if (ethRadioButton.isChecked) {
            blockchain = "eth"
        } else if (solRadioButton.isChecked) {
            blockchain = "sol"
        }

        //Assign the correct Airdrop category
        when {
            nftRadioButton.isChecked -> {
                category = "nft"
            }
            icoRadioButton.isChecked -> {
                category = "ico"
            }
            daoRadioButton.isChecked -> {
                category = "dao"
            }
            igoRadioButton.isChecked -> {
                category = "igo"
            }
        }

        //Generate a random value from 0 to 3 and give it
        //the home categories tags
        val random = (0..3).shuffled().last()
        var randomTag = ""
        when (random) {
            0 -> {
                randomTag = "new"
            }
            1 -> {
                randomTag = "hot"
            }
            2 -> {
                randomTag = "verified"
            }
            3 -> {
               randomTag = "old"
            }
        }

        //Check for the validity of the dates
        if (checkDates(startFormattedDate, endFormattedDate, progressBar)) {
            database.collection("Airdrops").get()
                .addOnCompleteListener { result ->
                    airdropsCount = result.result.size() + 1

                    //Create a map of the Airdrop to send to Firebase
                    val airdrop = hashMapOf(
                        "_id" to airdropsCount,
                        "title" to title,
                        "description" to description,
                        "rules" to arrayListOf(ruleOne, ruleTwo, ruleThree),
                        "tags" to arrayListOf(blockchain, category, randomTag),
                        "start_date" to startFormattedDate,
                        "end_date" to endFormattedDate,
                        "bannerURL" to imageURL,
                        "lister_uid" to currentUser?.uid
                    )

                    //Query to save the Airdrop in the database
                    database.collection("Airdrops").document().set(airdrop)
                        .addOnSuccessListener {
                            completed = true
                            listButton.isEnabled = true
                            TastyToasty.makeText(this, "Congratulations\nAirdrop added to the database", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()
                            progressBar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                            val intent = Intent(this, ProfileActivity::class.java)
                            startActivity(intent)
                            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
                            finish()
                        }

                        .addOnFailureListener {
                            listButton.isEnabled = true
                            progressBar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            TastyToasty.makeText(this, "Error while adding your airdrop to the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                        }
                }

                .addOnFailureListener {
                    listButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    TastyToasty.makeText(this, "Error while connecting to the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }
        }
    }

    //This function let the user pick an image
    //from the gallery to use it as the Airdrop banner
    private fun pickImageFromGallery() {

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        this.startActivityForResult(intent, ProfileActivity.IMAGE_PICK_CODE)
    }

    //onActivityResult for the image being picked
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val titleEditText = findViewById<EditText>(R.id.airdrop_title_edittext)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val storageRef = storage.reference
        val currentAirdrop = titleEditText.text.toString()
        val imagesRef: StorageReference = storageRef.child("images").child("airdrops").child(currentAirdrop).child("banner")

        //if the image has been correctly picked
        if (resultCode == Activity.RESULT_OK && requestCode == ProfileActivity.IMAGE_PICK_CODE) {
            val airdropBannerImage = findViewById<ImageView>(R.id.airdrop_banner_upload)
            progressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            //Upload the image to the storage
            imagesRef.putFile(data?.data!!)
                .addOnCompleteListener {
                    airdropBannerImage.setImageURI(data.data)

                    //Get back the image url to show it
                    //in the Aidrop banner imageview
                    imagesRef.downloadUrl
                        .addOnCompleteListener { uri ->
                            val downloadUri = uri.result
                            imageURL = downloadUri.toString()
                            progressBar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            TastyToasty.makeText(this, "Error while getting the image URL", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                        }

                    completed = true
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    TastyToasty.makeText(this, "Image uploaded", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()
                }
                .addOnFailureListener {
                    completed = false
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    TastyToasty.makeText(this, "Error uploading the image", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }
        }
    }

    //Function that checks all the dates
    private fun checkDates(startFormattedDate: Date, endFormattedDate: Date, progressBar: ProgressBar) : Boolean {
        val current = Date()
        var datesOkay = false

        val listButton = findViewById<Button>(R.id.list_airdrop_button)

        if (startFormattedDate.before(current)) {
            progressBar.visibility = View.GONE
            listButton.isEnabled = true
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            TastyToasty.makeText(this, "Your starting date is before today", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()

        } else if (endFormattedDate.before(current)) {
            progressBar.visibility = View.GONE
            listButton.isEnabled = true
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            TastyToasty.makeText(this, "Your ending date is before today", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()

        } else if (endFormattedDate.before(startFormattedDate)){
            progressBar.visibility = View.GONE
            listButton.isEnabled = true
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            TastyToasty.makeText(this, "Your ending date is before the starting date", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()

        } else if (startFormattedDate.after(endFormattedDate)){
            progressBar.visibility = View.GONE
            listButton.isEnabled = true
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            TastyToasty.makeText(this, "Your starting date is after the ending date", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()

        } else {
            datesOkay = true
        }

        return datesOkay
    }

    override fun onBackPressed() {
        val titleEditText = findViewById<EditText>(R.id.airdrop_title_edittext)

        //Same check as above
        if (completed){
            val storageRef = storage.reference
            val currentAirdrop = titleEditText.text.toString()
            val imagesRef: StorageReference = storageRef.child("images").child("airdrops").child(currentAirdrop).child("banner")

            imagesRef.delete()
                .addOnFailureListener {
                    TastyToasty.makeText(this, "Failed deleting the image", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }
        }

        //Same checks as above
        val from = intent.getStringExtra("from")
        if (from == "newmain") {
            val intent = Intent(this, NewMainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        } else if (from == "profile") {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }
    }
}
