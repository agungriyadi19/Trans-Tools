package com.example.transtools

import android.os.Bundle
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        val imgLogo: ImageView = findViewById(R.id.logo)

        val zoomInAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        imgLogo.startAnimation(zoomInAnimation)

        val sharedPreferences = getSharedPreferences("MySession", Context.MODE_PRIVATE)
        val userId = sharedPreferences?.getString("userId", null)
        Log.d("ini mysession", userId.toString())

        if (userId != null) {
            // Jika session (userId) tidak null, langsung ke MainActivity
//            Log.d("ini mysession", "UserId ada: $userId")
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        } else {
            // Jika session null, tampilkan splash screen dulu
            Log.d("ini mysession", "UserId tidak ditemukan")

            Handler().postDelayed({
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java)) // Arahkan ke Login jika tidak ada userId
                finish()
            }, SPLASH_TIME_OUT)
        }
    }


    companion object {
        var SPLASH_TIME_OUT: Long = 5000
    }
}