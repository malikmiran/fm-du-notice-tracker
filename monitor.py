#!/usr/bin/env python3
"""
FM DU Notice Monitor (Termux companion script)
Scrapes homepage ticker + MBBS notices and sends notifications using Termux notify tool.
"""

import sys
import os
import json
import urllib.request
import re
from bs4 import BeautifulSoup
import subprocess

CACHE_FILE = os.path.expanduser("~/.fm_du_seen_notices.json")
HOME_URL = "https://www.fm.du.ac.bd/"
MBBS_URL = "https://www.fm.du.ac.bd/notice_view/category/1"
NOTIFY_BIN = os.path.expanduser("~/.local/bin/notify")

headers = {
    "User-Agent": "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
}

def load_seen():
    if os.path.exists(CACHE_FILE):
        try:
            with open(CACHE_FILE, "r") as f:
                return set(json.load(f))
        except Exception:
            return set()
    return set()

def save_seen(seen_set):
    with open(CACHE_FILE, "w") as f:
        json.dump(list(seen_set), f)

def notify_user(title, message):
    if os.path.exists(NOTIFY_BIN) and os.access(NOTIFY_BIN, os.X_OK):
        subprocess.run([NOTIFY_BIN, "-t", title, message])
    else:
        print(f"[NOTIFICATION] {title}: {message}")

def check_notices():
    seen = load_seen()
    is_first_run = (len(seen) == 0)
    current_notices = []

    # 1. Homepage Ticker
    try:
        req = urllib.request.Request(HOME_URL, headers=headers)
        html = urllib.request.urlopen(req, timeout=15).read().decode("utf-8")
        soup = BeautifulSoup(html, "html.parser")
        for item in soup.select(".ticker-track span"):
            a = item.find("a")
            href = a["href"].strip() if a and a.has_attr("href") else ""
            title = a.get_text(strip=True) if a else item.get_text(" ", strip=True)
            if href and title:
                current_notices.append({"title": title, "link": href, "source": "Homepage Ticker"})
    except Exception as e:
        print(f"Error scraping homepage: {e}")

    # 2. MBBS Category
    try:
        req = urllib.request.Request(MBBS_URL, headers=headers)
        html = urllib.request.urlopen(req, timeout=15).read().decode("utf-8")
        soup = BeautifulSoup(html, "html.parser")
        for item in soup.select(".notice-item"):
            href = item.get("href", "").strip()
            title_el = item.select_one(".notice-title")
            title = title_el.get_text(strip=True) if title_el else ""
            date_el = item.select_one(".notice-date")
            date_str = date_el.get_text(strip=True) if date_el else ""
            if href and title:
                full_title = f"[{date_str}] {title}" if date_str else title
                current_notices.append({"title": full_title, "link": href, "source": "MBBS Category"})
    except Exception as e:
        print(f"Error scraping MBBS category: {e}")

    new_items = []
    for n in current_notices:
        identifier = n["link"]
        if identifier not in seen:
            seen.add(identifier)
            new_items.append(n)

    save_seen(seen)

    if is_first_run:
        print(f"Initial sync complete! Cached {len(seen)} existing notices.")
        return

    if new_items:
        print(f"Found {len(new_items)} new notices!")
        for n in new_items:
            notify_user("FM DU Notice Alert", f"{n['source']}: {n['title']}\n{n['link']}")
    else:
        print("No new notices found.")

if __name__ == "__main__":
    check_notices()
