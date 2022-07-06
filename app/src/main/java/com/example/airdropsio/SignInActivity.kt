package com.example.airdropsio

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.uk.tastytoasty.TastyToasty

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = Firebase.auth
        window.statusBarColor = getColor(R.color.ad_blue)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            val intent = Intent(this, StarterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar_signin)

        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<TextInputEditText>(R.id.editTextTextPassword)

        //Listener for when to loose focus and hide the soft keyboard
        val rootLayout = findViewById<ConstraintLayout>(R.id.signin_scrollview_layout)
        rootLayout.setOnClickListener {
            if (emailEditText.hasFocus()) {
                hideSoftKeyboard(emailEditText)
                emailEditText.clearFocus()

            } else if (passwordEditText.hasFocus()) {
                hideSoftKeyboard(passwordEditText)
                passwordEditText.clearFocus()

            }
        }

        val loginButton = findViewById<Button>(R.id.buttonLogin)
        loginButton.setOnClickListener {

            //Disable the login button until an error or the login completes to prevent the user tapping 300 times
            loginButton.isEnabled = false
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)

            //Show the custom progress bar
            progressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            //Get email and user password from the edittext
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            //If the email or the password edittext are empty
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

                //Remove the progress bar, clear the "NOT_TOUCHABLE" flag and enable the login button
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                loginButton.isEnabled = true
                TastyToasty.makeText(this, "Please provide a valid email or password to continue", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,true).show()

                //If the password length is less than 6 characters
            } else if (password.length < 6) {
                TastyToasty.makeText(this, "Please enter a 6 characters password", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                loginButton.isEnabled = true

            } else  {
                //If everything's okay, login the user
                loginAccount(email, password, progressBar)
            }
        }
    }

    //Override onStart to check if the user session is valid and redirect him directly to the home
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            TastyToasty.makeText(this, "You are already logged in", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()
            val intent = Intent(this, NewMainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Function to login the user using Firebase
    private fun loginAccount(email: String, password: String, progressBar: ProgressBar) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    TastyToasty.makeText(this, "Login Successful", TastyToasty.SHORT, R.drawable.ic_baseline_check_circle_24, com.uk.tastytoasty.R.color.green, R.color.white,false).show()

                    val loginButton = findViewById<Button>(R.id.buttonLogin)
                    loginButton.isEnabled = true

                    val intent = Intent(this, NewMainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                    finish()

                } else {
                    val loginButton = findViewById<Button>(R.id.buttonLogin)
                    loginButton.isEnabled = true
                    TastyToasty.makeText(this, "Authentication Failed\nEmail not present or  wrong credentials", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                val loginButton = findViewById<Button>(R.id.buttonLogin)
                loginButton.isEnabled = true
                TastyToasty.makeText(this, "Error while connecting to the database", TastyToasty.SHORT, R.drawable.ic_baseline_error_24, com.uk.tastytoasty.R.color.red, R.color.white,false).show()
            }
        }

    //Function to hide the soft keyboard
    private fun Activity.hideSoftKeyboard(editText: EditText){
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this, StarterActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
    }
}