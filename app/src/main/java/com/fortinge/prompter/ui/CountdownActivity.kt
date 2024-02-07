package com.fortinge.prompter.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import com.fortinge.forprompt.R
import com.fortinge.forprompt.databinding.ActivityCountdownBinding

class CountdownActivity : AppCompatActivity() {

    private var _binding: ActivityCountdownBinding? = null
    private val binding get() = _binding!!

    var runnable: Runnable = Runnable { }
    var handler: Handler = Handler(Looper.getMainLooper())

    var timer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)

        _binding = ActivityCountdownBinding.inflate(layoutInflater)
        setContentView(binding.root)
        timer = intent?.getIntExtra("timer",10)!!
        binding.timerLbl.setText(timer.toString())

        runnable = object : Runnable {
            override fun run() {
                binding.timerLbl.setText(timer.toString())
                timer--
                handler.postDelayed(this,1000)
                if (timer <= 0) {
                    this@CountdownActivity.finish()
                }
            }

        }

        handler.post(runnable)

        binding.countdownActivityBackground.setOnClickListener {
            finish()
        }
    }

}