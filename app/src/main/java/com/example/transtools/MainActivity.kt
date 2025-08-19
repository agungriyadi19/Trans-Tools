package com.example.transtools

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.fragmentContainerView)
        when (navController.currentDestination?.id) {
            R.id.loginFragment -> {
                if (doubleBackToExitPressedOnce) {
                    finish()
                } else {
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT)
                        .show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            }

            R.id.bebasExpiredFragment -> {
                navController.navigate(R.id.action_bebasExpiredFragment_to_homeFragment)
            }

            R.id.tarikBarangFragment -> {
                navController.navigate(R.id.action_tarikBarangFragment_to_bebasExpiredFragment)
            }
            
            R.id.homeFragment -> {
                moveTaskToBack(true)
            }

            else -> {
                super.onBackPressed()
            }
        }
    }
}
