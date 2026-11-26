# FM DU Notice Tracker (Android App)

An Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, and **WorkManager** to track and notify you about new notices published by the **Faculty of Medicine, University of Dhaka** (`https://www.fm.du.ac.bd/`).

## Key Features

- **Dual-Source Scraping**: Automatically monitors both the **Homepage Ticker** and the **MBBS Notices Category** (`/notice_view/category/1`).
- **Background Polling with WorkManager**: Runs a background worker every 15 minutes (even when the app is closed) to detect newly posted notices.
- **Instant System Notifications**: Alerts you with high-priority Android notifications whenever an unread notice appears, with single-tap action to view the notice PDF.
- **Modern Jetpack Compose UI**:
  - Filter notices by *All*, *Homepage Ticker*, or *MBBS Category*.
  - Manual pull/button refresh on demand.
  - Mark as read / mark all as read.
  - Direct "Open PDF" and "Share" options.
  - Dark mode and Material 3 design matching Dhaka University's academic branding.
- **Offline Storage**: Uses Room Database to cache notice history offline and prevent duplicate alerts.

---

## How to Build the APK using GitHub Actions

You don't need Android Studio or heavy tools installed on your phone. **GitHub Actions** will compile the `.apk` for free in the cloud:

### Step 1: Initialize Git and Push to GitHub

In Termux:

```bash
cd ~/fm-du-notice-tracker
git init
git add .
git commit -m "Initial commit for FM DU Notice Tracker"
```

Create a new repository on [GitHub](https://github.com/new) (e.g. `fm-du-notice-tracker`), then push your code:

```bash
git branch -M main
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/fm-du-notice-tracker.git
git push -u origin main
```

### Step 2: Download your APK

1. Go to your repository on GitHub in your phone or PC browser.
2. Click on the **Actions** tab.
3. You will see the **Build Android APK** workflow running. It takes about 2 to 3 minutes.
4. Once completed (green checkmark), click on the run.
5. Under **Artifacts** at the bottom, tap **`FM-DU-Notice-Tracker-APK`** to download `app-debug.apk`.
6. Install it on your Android phone and enable notifications when prompted!

---

## Project Architecture

- **`bd.ac.du.fm.noticetracker.data.scraper.NoticeScraper`**: Jsoup HTML parser that parses `.ticker-track span` and `.notice-item` links and metadata.
- **`bd.ac.du.fm.noticetracker.data.local.AppDatabase`**: Room Database caching notices so you are only notified of new ones.
- **`bd.ac.du.fm.noticetracker.worker.NoticeCheckWorker`**: WorkManager CoroutineWorker scheduled periodically to check the site and dispatch notifications.
- **`bd.ac.du.fm.noticetracker.ui.MainActivity`**: Jetpack Compose UI with reactive StateFlows.
