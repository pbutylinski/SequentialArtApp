package com.example.pbutylinski.sequentialartapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import java.lang.Integer.parseInt
import android.text.InputType
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast


private const val BaseUrl = "https://www.collectedcurios.com"
private const val BaseUrlPhp = "$BaseUrl/sequentialart.php"

class MainActivity : AppCompatActivity() {

    private var imageUrl: String = "#"
    private var currentStripNumber: Int = 1
    private var maxStripNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)

        loadMaxStripNumber()
        currentStripNumber = getLastReadingStrip()

        loadCurrentStrip(this)

        nextStripButton.setOnClickListener {
            currentStripNumber++

            if (currentStripNumber > maxStripNumber) {
                currentStripNumber = maxStripNumber
            }

            loadCurrentStrip(this)
            saveReadingStrip()
        }

        previousStripButton.setOnClickListener {
            currentStripNumber--

            if (currentStripNumber < 1) {
                currentStripNumber = 1
            }

            loadCurrentStrip(this)
            saveReadingStrip()
        }

        firstStripButton.setOnClickListener {
            currentStripNumber = 1
            loadCurrentStrip(this)
            saveReadingStrip()
        }

        lastStripButton.setOnClickListener {
            currentStripNumber = maxStripNumber
            loadCurrentStrip(this)
            saveReadingStrip()
        }

        currentStripTextView.setOnClickListener {
            var stripNumberTextEdit = EditText(this)
            stripNumberTextEdit.hint = "$currentStripNumber"
            stripNumberTextEdit.inputType = InputType.TYPE_CLASS_NUMBER

            AlertDialog.Builder(this)
                    .setTitle("Go to page")
                    .setMessage("Enter page number you want to navigate to")
                    .setView(stripNumberTextEdit)
                    .setPositiveButton("Go to page") { _, _ ->
                        var stripNumber = parseInt(stripNumberTextEdit.text.toString())

                        if (stripNumber in 1..maxStripNumber) {
                            currentStripNumber = stripNumber
                            loadCurrentStrip(this)
                            saveReadingStrip()
                        } else {
                            Toast.makeText(this.applicationContext, "Invalid page number!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
        }
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

    private fun loadMaxStripNumber() {
        GlobalScope.launch {
            maxStripNumber = parseInt(Jsoup.connect(BaseUrlPhp)
                    .get()
                    .select("input[type=text]")
                    .`val`())

            withContext(Dispatchers.Main) {
                setButtons()
            }
        }
    }

    private fun loadCurrentStrip(activity: Activity) {
        setButtons()

        GlobalScope.launch {
            imageUrl = getImgUrlFromStrip(currentStripNumber)

            withContext(Dispatchers.Main) {
                Picasso.with(activity)
                        .load(imageUrl)
                        .into(imageView)
            }
        }
    }

    private fun getImgUrlFromStrip(number: Int): String {
        var url = getPageUrl(number)

        val imageUrl = Jsoup.connect(url)
                .get()
                .select("#strip")
                .attr("src")

        return "$BaseUrl/$imageUrl"
    }

    private fun getPageUrl(number: Int): String {
        return "$BaseUrlPhp?s=$number"
    }

    private fun saveReadingStrip() {
        with (this.getPreferences(Context.MODE_PRIVATE).edit()) {
            putInt("strip", currentStripNumber)
            commit()
        }
    }

    private fun getLastReadingStrip(): Int {
        return this.getPreferences(Context.MODE_PRIVATE).getInt("strip", maxStripNumber)
    }
}
