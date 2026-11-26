package bd.ac.du.fm.noticetracker.data.scraper

import bd.ac.du.fm.noticetracker.data.model.Notice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.security.MessageDigest

object NoticeScraper {
    private const val HOME_URL = "https://www.fm.du.ac.bd/"
    private const val MBBS_URL = "https://www.fm.du.ac.bd/notice_view/category/1"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    private const val TIMEOUT_MS = 20_000

    suspend fun fetchAllNotices(): List<Notice> = withContext(Dispatchers.IO) {
        val collected = mutableListOf<Notice>()
        val seenLinks = mutableSetOf<String>()

        // 1. Fetch from Homepage Ticker
        try {
            val homeDoc = Jsoup.connect(HOME_URL)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            val tickerSpans = homeDoc.select(".ticker-track span")
            for (span in tickerSpans) {
                val aTag = span.selectFirst("a")
                val href = aTag?.attr("abs:href") ?: ""
                val title = aTag?.text()?.trim() ?: span.text().trim()

                // Extract date if present, e.g. "Date: November 26, 2026"
                val rawText = span.text()
                val dateMatch = Regex("Date:\\s*([A-Za-z]+ \\d{1,2},\\s*\\d{4})").find(rawText)
                val dateStr = dateMatch?.groupValues?.getOrNull(1) ?: "Recent"

                if (title.isNotEmpty() && href.isNotEmpty()) {
                    val normalizedLink = normalizeUrl(href)
                    if (seenLinks.add(normalizedLink)) {
                        collected.add(
                            Notice(
                                id = generateId(normalizedLink, title),
                                title = title,
                                date = dateStr,
                                link = normalizedLink,
                                source = "Homepage Ticker"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Fetch from MBBS Category Page (category 1)
        try {
            val mbbsDoc = Jsoup.connect(MBBS_URL)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get()

            val noticeItems = mbbsDoc.select(".notice-item")
            for (item in noticeItems) {
                val href = item.attr("abs:href")
                val titleEl = item.selectFirst(".notice-title")
                val dateEl = item.selectFirst(".notice-date")

                val title = titleEl?.text()?.trim() ?: ""
                val date = dateEl?.text()?.trim() ?: "Recent"

                if (title.isNotEmpty() && href.isNotEmpty()) {
                    val normalizedLink = normalizeUrl(href)
                    if (seenLinks.add(normalizedLink)) {
                        collected.add(
                            Notice(
                                id = generateId(normalizedLink, title),
                                title = title,
                                date = date,
                                link = normalizedLink,
                                source = "MBBS Category"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        collected
    }

    private fun normalizeUrl(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://www.fm.du.ac.bd/" + url.removePrefix("/")
        }
    }

    private fun generateId(link: String, title: String): String {
        val input = "$link|$title"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
