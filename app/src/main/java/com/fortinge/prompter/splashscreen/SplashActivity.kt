package com.fortinge.prompter.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.*
import androidx.appcompat.app.AppCompatActivity
import com.fortinge.prompter.MainActivity
import com.fortinge.forprompt.R


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java )
            startActivity(intent)
            finish()
        },1000
        )
    }
}