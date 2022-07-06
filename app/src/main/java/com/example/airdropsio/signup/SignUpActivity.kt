package com.example.airdropsio.signup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.airdropsio.NewMainActivity
import com.example.airdropsio.R
import com.example.airdropsio.StarterActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.uk.tastytoasty.TastyToasty

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = Firebase.auth
        database = Firebase.firestore
        storage = FirebaseStorage.getInstance()

        window.statusBarColor = getColor(R.color.ad_blue)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, StarterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }

        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<TextInputEditText>(R.id.editTextTextPassword)
        val passwordConfirmEditText = findViewById<TextInputEditText>(R.id.editTextTextPasswordConfirm)

        //Listener to loose the focus
        val rootLayout = findViewById<ConstraintLayout>(R.id.signup_scrollview_layout)
        rootLayout.setOnClickListener {
            if (emailEditText.hasFocus()) {
                hideSoftKeyboard(emailEditText)
                emailEditText.clearFocus()
            } else if (passwordEditText.hasFocus()) {
                hideSoftKeyboard(passwordEditText)
                passwordEditText.clearFocus()
            } else if (passwordConfirmEditText.hasFocus()) {
                hideSoftKeyboard(passwordConfirmEditText)
                passwordConfirmEditText.clearFocus()
            }
        }

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar_signup)

        val registerButton = findViewById<Button>(R.id.buttonRegister)

        registerButton.setOnClickListener {

            //Disable the button until the operation completes or until an error shows up
            registerButton.isEnabled = false
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            //Show the progressbar
            progressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = passwordConfirmEditText.text.toString()

            //If the email / password / password confirmation are empty
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                TastyToasty.makeText(this, "Please provide a valid email or password to continue", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                registerButton.isEnabled = true

                //If the password length is less than 6 characters
            } else if (password.length < 6) {
                TastyToasty.makeText(this, "Please enter a 6 characters password", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                registerButton.isEnabled = true

                //If the confirmation password length is less than 6 characters
            } else if (confirmPassword.length < 6) {
                TastyToasty.makeText(this, "Please enter a 6 characters confirm password", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                registerButton.isEnabled = true

                //If the password and the password confirmation don't match
            } else if (password != confirmPassword) {
                TastyToasty.makeText(this, "Your passwords don't match, try again", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                registerButton.isEnabled = true

            } else {
                //If everything's okay create the account
                createAccount(email, password, registerButton)
            }
        }
    }

    private fun createAccount(email: String, password: String, registerButton: Button) {

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar_signup)

        //Create the account using Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    TastyToasty.makeText(this, "Registration successful", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()

                    //If the registration is okay, add the user to the database
                    addUserDatabase(database)

                    val intent = Intent(this, NewMainActivity::class.java)
                    startActivity(intent)

                    //Override defaul activities transactions
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                    finish()

                } else {
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    registerButton.isEnabled = false

                    TastyToasty.makeText(this, "Authentication Failed\nEmail already present or wrong credentials", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                registerButton.isEnabled = false

                TastyToasty.makeText(this, "Registration failed", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }
    }

    //Function to add the newly created user to the database
    private fun addUserDatabase (database: FirebaseFirestore) {
        val currentUser = auth.currentUser

        //Set a default user profile name
        currentUser!!.updateProfile(userProfileChangeRequest {
            displayName = "User"
        })

        //Create a map for the user uid
        val userMap = hashMapOf(
            "uid" to currentUser.uid,
        )

        //Query to add the user to the database
        database.collection("Users").document(currentUser.uid).set(userMap)
            .addOnSuccessListener {

                //Get storage and profile picture references
                val storageRef = storage.reference
                //Assign the user a default profile picture
                val imagesRef: StorageReference = storageRef.child("images").child("users").child(currentUser.uid).child("profile_picture")

                //Get default image uri
                val defaultUri = this.drawableToUri(R.drawable.airdropsio_logo)

                //Put the file in the storage
                imagesRef.putFile(defaultUri)
                    .addOnCompleteListener {

                        //Update the user profile infos
                        currentUser.updateProfile(userProfileChangeRequest {
                            photoUri = Uri.parse(it.result.toString())
                        })

                        //Get the profile picture URL to save it in the database
                        imagesRef.downloadUrl
                            .addOnCompleteListener { uri ->
                                val downloadUri = uri.result

                                //User hash map
                                val userMap2 = hashMapOf(
                                    "uid" to currentUser.uid,
                                    "email" to currentUser.email,
                                    "name" to "User",
                                    "photoURL" to downloadUri.toString()
                                )

                                //Query to update the user profile in the database
                                database.collection("Users").document(currentUser.uid).set(userMap2)
                                    .addOnCompleteListener {
                                        val registerButton = findViewById<Button>(R.id.buttonRegister)
                                        registerButton.isEnabled = true

                                        TastyToasty.makeText(this, "User added to database", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()
                                    }
                                    .addOnFailureListener {
                                        TastyToasty.makeText(this, "Error while creating the user in the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                                    }
                            }
                            .addOnFailureListener {
                                TastyToasty.makeText(this, "Error while getting the image just uploaded", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                            }
                    }
                    .addOnFailureListener {
                        TastyToasty.makeText(this, "Error while uploading your default profile picture", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                    }
            }
            .addOnFailureListener {
                TastyToasty.makeText(this, "Failed to add User to the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }
    }

    //Function to hide the soft keyboard
    private fun Activity.hideSoftKeyboard(editText: EditText){
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    //Function extension to convert drawable to URI
    private fun Context.drawableToUri(drawable: Int): Uri{
        return Uri.parse("android.resource://$packageName/$drawable")
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this, StarterActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
    }
}