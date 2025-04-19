package com.example.biometricapps

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.core.content.ContextCompat
import com.example.biometricapps.databinding.ActivityMainBinding
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    companion object {
        const val TAG = "BiometricApp"
        const val KEY_NAME = "my_key"
        const val REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //1. Init
        biometricPrompt = createBiometricPrompt()

        //2. init prompt
        createPromptInfo()

        //3, check is available
        checkBiometricIsAvailable()

        //4. on click
        onClickBiometric()

    }

    private fun checkBiometricIsAvailable() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Log.d(
                "MY_APP_TAG",
                "App can authenticate using biometrics."
            )

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Log.e(
                "MY_APP_TAG",
                "No biometric features available on this device."
            )

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Log.e(
                "MY_APP_TAG",
                "Biometric features are currently unavailable."
            )

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                    )
                }
                startActivityForResult(enrollIntent, REQUEST_CODE)
            }
        }

    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(
                        applicationContext, "Authentication error Negative: $errString", Toast
                            .LENGTH_SHORT
                    ).show()
                }
                Toast.makeText(
                    applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(
                    applicationContext, "Authentication failed", Toast.LENGTH_SHORT
                ).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(
                    applicationContext, "Authentication succeeded!", Toast.LENGTH_SHORT
                ).show()
            }
        }

        return BiometricPrompt(this, executor, callback)
    }


    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setDescription("Prepare yourself")
            .setConfirmationRequired(false)
            .setNegativeButtonText("Use account password")
            .build()
    }

    private fun onClickBiometric() {
        binding.biometricLoginButton.setOnClickListener {
            biometricPrompt.authenticate(createPromptInfo())
        }
    }

}
