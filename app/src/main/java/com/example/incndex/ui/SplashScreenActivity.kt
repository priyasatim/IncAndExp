package com.example.incndex.ui

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.incndex.R
import com.example.incndex.databinding.ActivityDashboardBinding
import com.example.incndex.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val customFont = Typeface.createFromAsset(assets, "ColonnaMT.ttf")
        binding.tvLogo.typeface = customFont

        if(isBiometricSupported()){
            showBiometricPrompt()
        } else {
            Handler().postDelayed({
                // on below line we are
                // creating a new intent
                val i = Intent(
                    this@SplashScreenActivity,
                    DashboardActivity::class.java
                )
                // on below line we are
                // starting a new activity.
                startActivity(i)

                // on the below line we are finishing
                // our current activity.
                finish()
            }, 2000)
        }
    }

    private fun showBiometricPrompt() {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.layout_custom_toast_message, findViewById(R.id.custom_toast_biometric_layout))

        val biometricText = layout.findViewById<TextView>(R.id.biometricText)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle authentication error
                    finishAffinity()
                    biometricText.text = "Authentication error!"

                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Handle authentication success
                    biometricText.text = "Authentication succeeded!"
                    Handler().postDelayed({
                        // on below line we are
                        // creating a new intent
                        val i = Intent(
                            this@SplashScreenActivity,
                            DashboardActivity::class.java
                        )
                        // on below line we are
                        // starting a new activity.
                        startActivity(i)

                        // on the below line we are finishing
                        // our current activity.
                        finish()
                    }, 2000)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Handle authentication failure
                    biometricText.text = "Authentication failed."
                }
            })

        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()

        biometricPrompt.authenticate(promptInfo)
    }


    private fun isBiometricSupported(): Boolean {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // The user can authenticate with biometrics, continue with the authentication process
                return true
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE, BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Handle the error cases as needed in your app
                return false
            }

            else -> {
                // Biometric status unknown or another error occurred
                return false
            }
        }
    }
}