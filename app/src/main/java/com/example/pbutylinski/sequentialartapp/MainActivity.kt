package com.example.pbutylinski.sequentialartapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Integer.parseInt
import android.text.InputType
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    private var helper: ComicStripHelper = ComicStripHelper()
    private var settings: SettingsFacade = SettingsFacade(this)

    private var currentStripNumber: Int = 1
    private var maxStripNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLayout()

        initialize(this)

        nextStripButton.setOnClickListener {
            this.currentStripNumber++

            if (currentStripNumber > maxStripNumber) {
                currentStripNumber = maxStripNumber
            }

            loadCurrentStrip(this)
        }

        previousStripButton.setOnClickListener {
            currentStripNumber--

            if (currentStripNumber < 1) {
                currentStripNumber = 1
            }

            loadCurrentStrip(this)
        }

        firstStripButton.setOnClickListener {
            currentStripNumber = 1
            loadCurrentStrip(this)
        }

        lastStripButton.setOnClickListener {
            currentStripNumber = maxStripNumber
            loadCurrentStrip(this)
        }

        titleTextView.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(BaseUrlPhp)
            startActivity(openURL)
        }

        currentStripTextView.setOnClickListener {
            val stripNumberTextEdit = EditText(this)

            stripNumberTextEdit.hint = "$currentStripNumber"
            stripNumberTextEdit.inputType = InputType.TYPE_CLASS_NUMBER

            AlertDialog.Builder(this)
                    .setTitle("Go to page")
                    .setMessage("Enter page number you want to navigate to")
                    .setView(stripNumberTextEdit)
                    .setPositiveButton("Go to page") { _, _ ->
                        val stripNumber = parseInt(stripNumberTextEdit.text.toString())

                        if (stripNumber in 1..maxStripNumber) {
                            currentStripNumber = stripNumber
                            loadCurrentStrip(this)
                        } else {
                            Toast.makeText(this.applicationContext, "Invalid page number!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
        }
    }

    private fun setupLayout() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
    }

    private fun setButtons() {
        currentStripTextView.text = "$currentStripNumber"

        if (currentStripNumber == 1) {
            previousStripButton.visibility = View.INVISIBLE
        } else {
            previousStripButton.visibility = View.VISIBLE
        }

        if (currentStripNumber == maxStripNumber) {
            nextStripButton.visibility = View.INVISIBLE
        } else {
            nextStripButton.visibility = View.VISIBLE
        }
    }

    private fun initialize(activity: Activity) {
        GlobalScope.launch {
            maxStripNumber = helper.getLastStripNumber()
            currentStripNumber = settings.getLastReadingStrip(maxStripNumber)

            withContext(Dispatchers.Main) {
                loadCurrentStrip(activity)
                setButtons()
            }
        }
    }

    private fun loadCurrentStrip(activity: Activity) {
        setButtons()

        GlobalScope.launch {
            val imageUrl = helper.getImgUrlFromStrip(currentStripNumber)

            withContext(Dispatchers.Main) {
                Picasso.with(activity)
                        .load(imageUrl)
                        .into(imageView)
            }
        }

        settings.saveReadingStrip(currentStripNumber)
    }
}
