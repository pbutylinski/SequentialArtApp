package com.example.pbutylinski.sequentialartapp.Helpers

import org.jsoup.Jsoup

const val BaseUrl = "https://www.collectedcurios.com"
const val BaseUrlPhp = "$BaseUrl/sequentialart.php"

class ComicStripHelper {
    fun getLastStripNumber(): Int {
        return Integer.parseInt(Jsoup.connect(BaseUrlPhp)
                .get()
                .select("input[type=text]")
                .`val`())
    }

    fun getImgUrlFromStrip(number: Int): String {
        val url = getPageUrl(number)

        val imageUrl = Jsoup.connect(url)
                .get()
                .select("#strip")
                .attr("src")

        return "$BaseUrl/$imageUrl"
    }

    private fun getPageUrl(number: Int): String {
        return "$BaseUrlPhp?s=$number"
    }
}