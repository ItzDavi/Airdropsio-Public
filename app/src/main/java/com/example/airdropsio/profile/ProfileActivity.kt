package com.example.airdropsio.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.example.airdropsio.*
import com.example.airdropsio.OnSwipeTouchListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.uk.tastytoasty.TastyToasty
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        database = Firebase.firestore
        storage = FirebaseStorage.getInstance()

        window.statusBarColor = getColor(R.color.ad_blue)

        val listAirdropButton = findViewById<Button>(R.id.profile_list_airdrop_button)
        listAirdropButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, ListAirdropActivity::class.java)
            intent.putExtra("from", "profile")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up)
            finish()
        }

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {

            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, NewMainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }

        val accountLinearLayout = findViewById<LinearLayout>(R.id.account_linear_layout)
        accountLinearLayout.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
        val accountArrowButton = findViewById<ImageButton>(R.id.open_account_details)
        accountArrowButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }

        val notificationsLinearLayout = findViewById<LinearLayout>(R.id.notification_linear_layout)
        notificationsLinearLayout.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
        val notificationArrowButton = findViewById<ImageButton>(R.id.open_notification_details)
        notificationArrowButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }

        val privacyLinearLayout = findViewById<LinearLayout>(R.id.privacy_linear_layout)
        privacyLinearLayout.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, PrivacyActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }
        val privacyArrowButton = findViewById<ImageButton>(R.id.open_privacy_details)
        privacyArrowButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, PrivacyActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            finish()
        }

        val logoutButton = findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            TastyToasty.makeText(this, "Successfully logged out", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()

            val intent = Intent(this, StarterActivity::class.java)
            val sharedPreferences = getSharedPreferences("com.example.airdropsio", MODE_PRIVATE)
            startActivity(intent)
            sharedPreferences.edit().clear().apply()
            finish()
            auth.signOut()
        }

        val profileName = findViewById<EditText>(R.id.username_text)
        profileName.setText(currentUser?.displayName)

        //Listener for taps outside or the profile name edittext to save it without a button
        val profileRootLayout = findViewById<ConstraintLayout>(R.id.profile_scrollview_layout)
        profileRootLayout.setOnClickListener {

            //User profile update request to Firebase
            val profileUpdates = userProfileChangeRequest {
                displayName = profileName.text.toString()
            }

            //User profile update function
            currentUser!!.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    if (profileName.hasFocus()) {
                        hideSoftKeyboard(profileName)
                        profileName.clearFocus()
                        TastyToasty.makeText(this, "Profile name updated", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()
                    }
                }
                .addOnFailureListener {
                    TastyToasty.makeText(this, "Error while updating your profile name", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }

            //Database user update map
            val nameDatabaseUpdate = hashMapOf(
                "name" to profileName.text.toString()
            )

            //User update profile query
            database.collection("Users").document(currentUser.uid).update(nameDatabaseUpdate as Map<String, Any>)
                .addOnFailureListener {
                    TastyToasty.makeText(this, "Failed to update the user from the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }

            profileRootLayout.setOnTouchListener(object : OnSwipeTouchListener(this@ProfileActivity) {
                override fun onSwipeRight() {
                    super.onSwipeRight()

                    profileRootLayout.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                    val intent = Intent(this@ProfileActivity, NewMainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                    finish()
                }
            })
        }



        val profilePicture = findViewById<CircleImageView>(R.id.profile_image)
        if (currentUser != null) {
            //Query to get the user profile picture and show it on the profile imageview
            storage.reference.child("images").child("users").child(currentUser.uid).child("profile_picture").downloadUrl
                .addOnCompleteListener {
                    val imageURL = it.result.toString()
                    Glide.with(this).load(imageURL).into(profilePicture)
                }
                .addOnFailureListener {
                    TastyToasty.makeText(this, "Failed to get the user from the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }
        }

        //Listener for changing the profile picture when the profile imageview is pressed
        profilePicture.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                pickImageFromGallery()
            }
        }
    }

    //Function to open the gallery
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    //Function on image chosen result
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val currentUser = auth.currentUser
        val storageRef = storage.reference
        val imagesRef: StorageReference = storageRef.child("images").child("users").child(currentUser?.uid.toString()).child("profile_picture")

        //If everything's okay with the image
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val profilePicture = findViewById<ImageView>(R.id.profile_image)

            //Put the image in the Storage
            imagesRef.putFile(data?.data!!)
                .addOnCompleteListener {
                    TastyToasty.makeText(this, "Image uploaded", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()
                }
                .addOnFailureListener {
                    TastyToasty.makeText(this, "Error uploading the image", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }

            //update the user profile to Firebase
            val profileUpdates = userProfileChangeRequest {
                photoUri = data.data!!
            }

            //User profile update function
            currentUser!!.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    TastyToasty.makeText(this, "Profile picture updated", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()

                    //Get the download URL of the photo just uploaded
                    imagesRef.downloadUrl
                        .addOnCompleteListener { it1 ->
                            val newPhotoURL = it1.result.toString()

                            //Query to update the user profile image URL to the database
                            database.collection("Users").document(currentUser.uid).update("photoURL", newPhotoURL)
                                .addOnCompleteListener {

                                    //Get user profile from the database
                                    database.collection("Users").document(currentUser.uid).get()
                                        .addOnCompleteListener {

                                            //Download the image URL and put it in the imageview
                                            imagesRef.downloadUrl
                                                .addOnCompleteListener {
                                                    val newImageURL = it.result.toString()

                                                    Glide.with(this).load(newImageURL).into(profilePicture)
                                                }
                                        }
                                        .addOnFailureListener {
                                            TastyToasty.makeText(this, "Failed to get the user from the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                                        }
                                }
                                .addOnFailureListener {
                                    TastyToasty.makeText(this, "Failed to update the user from the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                                }
                        }
                }
                .addOnFailureListener {
                    TastyToasty.makeText(this, "Error while updating your profile picture", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }
        }
    }

    //Companion objects for codes
    companion object {
        const val IMAGE_PICK_CODE = 1000
        const val PERMISSION_CODE = 1001
    }

    //Function to hide the soft keyboard
    private fun Activity.hideSoftKeyboard(editText: EditText){
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this, NewMainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
    }
}