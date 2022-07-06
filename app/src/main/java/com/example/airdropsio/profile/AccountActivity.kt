package com.example.airdropsio.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.airdropsio.OnSwipeTouchListener
import com.example.airdropsio.R
import com.example.airdropsio.StarterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.uk.tastytoasty.TastyToasty

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        window.statusBarColor = getColor(R.color.ad_blue)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        database = Firebase.firestore
        storage = FirebaseStorage.getInstance()

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)

            //Override the default transition
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }

        val newEmail = findViewById<EditText>(R.id.change_email_edittext)
        val passwordEmail = findViewById<EditText>(R.id.change_email_password)

        val changeEmailButton = findViewById<Button>(R.id.change_email_button)
        changeEmailButton.setOnClickListener {
            changeEmailButton.isEnabled = false
            //Perform haptic feedback to let the user know
            // the button has been pressed correctly
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

                //Checks for the email, user and password validity
                if (checkEmail(newEmail, currentUser!!, passwordEmail)) {

                    //If everything's okay, change it
                    changeEmail(newEmail, currentUser, passwordEmail)
                }
            }

        val newPassword = findViewById<EditText>(R.id.change_password_edittext)
        val oldPassword = findViewById<EditText>(R.id.change_password_old_edittext)

        val changePasswordButton = findViewById<Button>(R.id.change_password_button)
        changePasswordButton.setOnClickListener {
            changePasswordButton.isEnabled = false
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

                //Checks for old and new password validity
                if (checkPassword(newPassword, oldPassword)) {

                    //If everything's okay, change it
                    changePassword(newPassword, oldPassword, currentUser!!)
                }
            }

        val deletePassword = findViewById<EditText>(R.id.delete_account_password)

        val deletePasswordButton = findViewById<Button>(R.id.delete_account_button)
        deletePasswordButton.setOnClickListener {
            deletePasswordButton.isEnabled = false
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

                //Checks for current password validity
                if (checkDeletePassword(deletePassword)) {

                    //If everything's okay, delete the account and logout
                    deleteAccount(deletePassword, currentUser!!)
                }
            }

        val accountSettingsConstraint = findViewById<ConstraintLayout>(R.id.account_settings_constraintlayout)
        accountSettingsConstraint.setOnClickListener {
            hideSoftKeyboard(newEmail)
        }

        //Loose focus and close keyboard when pressing the screen
        val rootConstraintLayout = findViewById<ScrollView>(R.id.account_settings_scrollview)
        swipeHandler(rootConstraintLayout)
        rootConstraintLayout.setOnClickListener {
            if (newEmail.hasFocus()) {
                hideSoftKeyboard(newEmail)
                newEmail.clearFocus()
            }

            if (passwordEmail.hasFocus()) {
                hideSoftKeyboard(passwordEmail)
                passwordEmail.clearFocus()
            }

            if (newPassword.hasFocus()) {
                hideSoftKeyboard(newPassword)
                newPassword.clearFocus()
            }

            if (oldPassword.hasFocus()) {
                hideSoftKeyboard(oldPassword)
                oldPassword.clearFocus()
            }
        }

        //If the user press in the cardview layout or the imagebutton
        //expand or hide the cardview from the screen
        val test = findViewById<ConstraintLayout>(R.id.change_email_cardview_layout)
        val hiddenViewChangeEmail = findViewById<LinearLayout>(R.id.hidden_view_change_email)
        val expandCardViewChangeEmail = findViewById<ImageView>(R.id.change_email_expand_button)
        expandCardViewChangeEmail.setOnClickListener {
            if (hiddenViewChangeEmail.visibility == View.GONE) {
                expandCardViewChangeEmail.setImageResource(R.drawable.ic_baseline_expand_less_24)
                hiddenViewChangeEmail.visibility = View.VISIBLE

            } else if (hiddenViewChangeEmail.visibility == View.VISIBLE){
                expandCardViewChangeEmail.setImageResource(R.drawable.ic_baseline_expand_more_24)
                hiddenViewChangeEmail.visibility = View.GONE
            }
        }

        test.setOnClickListener {
            if (hiddenViewChangeEmail.visibility == View.GONE) {
                expandCardViewChangeEmail.setImageResource(R.drawable.ic_baseline_expand_less_24)
                hiddenViewChangeEmail.visibility = View.VISIBLE

            } else if (hiddenViewChangeEmail.visibility == View.VISIBLE){
                expandCardViewChangeEmail.setImageResource(R.drawable.ic_baseline_expand_more_24)
                hiddenViewChangeEmail.visibility = View.GONE
            }
        }

        val test2 = findViewById<ConstraintLayout>(R.id.change_password_cardview_layout)
        val hiddenViewChangePassword = findViewById<LinearLayout>(R.id.hidden_view_change_password)
        val expandCardViewChangePassword = findViewById<ImageView>(R.id.change_password_expand_button)
        expandCardViewChangePassword.setOnClickListener {
            if (hiddenViewChangePassword.visibility == View.GONE) {
                expandCardViewChangePassword.setImageResource(R.drawable.ic_baseline_expand_less_24)
                hiddenViewChangePassword.visibility = View.VISIBLE

            } else {
                expandCardViewChangePassword.setImageResource(R.drawable.ic_baseline_expand_more_24)
                hiddenViewChangePassword.visibility = View.GONE
            }
        }

        test2.setOnClickListener {
            if (hiddenViewChangePassword.visibility == View.GONE) {
                expandCardViewChangePassword.setImageResource(R.drawable.ic_baseline_expand_less_24)
                hiddenViewChangePassword.visibility = View.VISIBLE

            } else {
                expandCardViewChangePassword.setImageResource(R.drawable.ic_baseline_expand_more_24)
                hiddenViewChangePassword.visibility = View.GONE
            }
        }

        val test3 = findViewById<ConstraintLayout>(R.id.delete_account_cardview_layout)
        val hiddenViewDeleteAccount = findViewById<LinearLayout>(R.id.hidden_view_delete_account)
        val expandCardViewDeleteAccount = findViewById<ImageView>(R.id.delete_account_expand_button)
        expandCardViewDeleteAccount.setOnClickListener {
            if (hiddenViewDeleteAccount.visibility == View.GONE) {
                expandCardViewDeleteAccount.setImageResource(R.drawable.ic_baseline_expand_less_24)
                hiddenViewDeleteAccount.visibility = View.VISIBLE

            } else {
                expandCardViewDeleteAccount.setImageResource(R.drawable.ic_baseline_expand_more_24)
                hiddenViewDeleteAccount.visibility = View.GONE
            }
        }

        test3.setOnClickListener {
            if (hiddenViewDeleteAccount.visibility == View.GONE) {
                expandCardViewDeleteAccount.setImageResource(R.drawable.ic_baseline_expand_less_24)
                hiddenViewDeleteAccount.visibility = View.VISIBLE

            } else {
                expandCardViewDeleteAccount.setImageResource(R.drawable.ic_baseline_expand_more_24)
                hiddenViewDeleteAccount.visibility = View.GONE
            }
        }
    }

    //Function to hide the soft keyboard on the screen
    private fun Activity.hideSoftKeyboard(editText: EditText){
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    //Function to check the email, user and password validity
    private fun checkEmail(newEmail: EditText, currentUser: FirebaseUser, password: EditText) : Boolean {
        val changeEmailButton = findViewById<Button>(R.id.change_email_button)
        val checked: Boolean =
            //If the email edittext is empty
            if (TextUtils.isEmpty(newEmail.text)) {
                changeEmailButton.isEnabled = true
                TastyToasty.makeText(this, "You must enter a valid email before changing it", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                false

                //If the password edittext is empty
            } else if (TextUtils.isEmpty(password.text)) {
                changeEmailButton.isEnabled = true
                TastyToasty.makeText(this, "You must enter the password before changing the email", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                false

                //If the new email edittext is empty
            } else if (newEmail.text == currentUser) {
                changeEmailButton.isEnabled = true
                TastyToasty.makeText(this, "Your new email must be different from the previous one", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                false

            } else {
                true

            }

        return checked
    }

    //Change email function
    private fun changeEmail(newEmail: EditText, currentUser: FirebaseUser, passwordEmail: EditText) {
        val changeEmailButton = findViewById<Button>(R.id.change_email_button)

        //Reitherate the sign in function to set the new email
        auth.signInWithEmailAndPassword(currentUser.email.toString(), passwordEmail.text.toString())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    currentUser.updateEmail(newEmail.text.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Your email has been updated", Toast.LENGTH_SHORT).show()

                                val userUpdate = hashMapOf(
                                    "email" to newEmail.text.toString()
                                )

                                //Update the email in the database
                                database.collection("Users").document(currentUser.uid).update(userUpdate as Map<String, Any>)
                                    .addOnCompleteListener {
                                        changeEmailButton.isEnabled = true
                                    }
                                    .addOnFailureListener {
                                        changeEmailButton.isEnabled = true
                                        TastyToasty.makeText(this, "Failed updating your email in the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                                    }

                            } else {
                                changeEmailButton.isEnabled = true
                                TastyToasty.makeText(this, "Error while updating your email, try again later", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                            }
                        }
                        .addOnFailureListener {
                            changeEmailButton.isEnabled = true
                            TastyToasty.makeText(this, "Error while updating your email, try again later", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                        }
                }
            }
            .addOnFailureListener {
                changeEmailButton.isEnabled = true
                TastyToasty.makeText(this, "Failed while authenticating your email and password, try again later", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }
    }

    //Function to check the password validity
    private fun checkPassword(newPassword: EditText, oldPassword: EditText) : Boolean {
        val changePasswordButton = findViewById<Button>(R.id.change_password_button)
        val checked : Boolean =
            //If the new password edittext is empty
            if (TextUtils.isEmpty(newPassword.text)) {
                changePasswordButton.isEnabled = true
                TastyToasty.makeText(this, "You must enter a new password before updating it", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                false

                //If the old password edittext is empty
            } else if (TextUtils.isEmpty(oldPassword.text)) {
                changePasswordButton.isEnabled = true
                TastyToasty.makeText(this, "You must enter your old password before updating it", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                false

                //If the new password is equal to the old password
            } else if (newPassword.text.toString() == oldPassword.text.toString()) {
                changePasswordButton.isEnabled = true
                TastyToasty.makeText(this, "Your new password must be different from the previous one", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                false

                //If the new password lentgh is less than 6 characters
            } else if (newPassword.text.length < 6) {
                changePasswordButton.isEnabled = true
                TastyToasty.makeText(this, "Your new password must be at least 6 characters long", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                false

                //If everything's okay return true
            } else {
                true
            }

        return checked
    }

    //Function to change the password
    private fun changePassword(newPassword: EditText, oldPassword: EditText, currentUser: FirebaseUser) {
        val changePasswordButton = findViewById<Button>(R.id.change_password_button)

        //Reitherate the sign in to change the password
        auth.signInWithEmailAndPassword(currentUser.email.toString(), oldPassword.text.toString())
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    //Update the current user password
                    currentUser.updatePassword(newPassword.text.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                changePasswordButton.isEnabled = true
                                hideSoftKeyboard(oldPassword)
                                TastyToasty.makeText(this, "Your password has been updated", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()
                            }
                        }
                        .addOnFailureListener {
                            changePasswordButton.isEnabled = true
                            TastyToasty.makeText(this, "Failed while updating your password, try again later", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                        }
                }
            }
            .addOnFailureListener {
                changePasswordButton.isEnabled = true
                TastyToasty.makeText(this, "Error while getting your account from the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }
    }

    //Function to check if the password is valid
    private fun checkDeletePassword(deletePassword: EditText) : Boolean {
        val deleteAccountButton = findViewById<Button>(R.id.delete_account_button)
        val checked : Boolean =

            //If the password edittext is empty
            if (TextUtils.isEmpty(deletePassword.text)) {
                deleteAccountButton.isEnabled = true
                TastyToasty.makeText(this, "You must enter your password before deleting your account", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                false

                //If the password length is less than 6 characters
            } else if (deletePassword.text.length < 6) {
                deleteAccountButton.isEnabled = true
                TastyToasty.makeText(this, "Your password must be at least 6 characters", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                false

            } else {
                true

            }

        return checked
    }

    //Function to delete the account from the database
    private fun deleteAccount(deletePassword: EditText, currentUser: FirebaseUser) {
        val deleteAccountButton = findViewById<Button>(R.id.delete_account_button)
        auth.signInWithEmailAndPassword(currentUser.email.toString(), deletePassword.text.toString())
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    //Get the user from the database and delete it
                    database.collection("Users").document(currentUser.uid).delete()
                        .addOnCompleteListener {

                            //Get the user files from the storage and delete it
                            storage.reference.child("images").child("users").child(currentUser.uid).child("profile_picture").delete()
                                .addOnCompleteListener {

                                    //Delete also this user information from Firebase auth servers
                                    currentUser.delete()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                TastyToasty.makeText(this, "User account deleted successfully", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()

                                                deleteAccountButton.isEnabled = true

                                                val intent = Intent(this, StarterActivity::class.java)
                                                startActivity(intent)
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                                                finish()
                                            }
                                        }
                                        .addOnFailureListener {
                                            deleteAccountButton.isEnabled = true
                                            TastyToasty.makeText(this, "Error while deleting your account", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                                        }
                                }
                                .addOnFailureListener {
                                    deleteAccountButton.isEnabled = true
                                    TastyToasty.makeText(this, "Error while deleting your photos", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                                }
                        }
                        .addOnFailureListener {
                            deleteAccountButton.isEnabled = true
                            TastyToasty.makeText(this, "Error while deleting your account from the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                        }
                }
            }
            .addOnFailureListener {
                deleteAccountButton.isEnabled = true
                TastyToasty.makeText(this, "Error while getting your account info", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun swipeHandler(view: ScrollView) {
        view.setOnTouchListener(object : OnSwipeTouchListener(this) {
            override fun onSwipeRight() {
                super.onSwipeRight()

                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
                val intent = Intent(this@AccountActivity, ProfileActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                finish()
            }
        })
    }

    //Override the onBackPressed with a custom transition
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
    }
}