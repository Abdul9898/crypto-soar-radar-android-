# ⚡ Crypto Soar Radar (Native Android)

A native Android market screener and breakout predictor designed to detect cryptocurrencies preparing to surge or pump — built with complete freedom and **no subscription walls or locked features**.

---

## 🎯 Key Features

* **100% Unlocked Soar (%) Screener:**
  * Displays the full, unrestricted list of coins ranked by their real-time **Soar Probability (%)**.
  * Dynamic target period calculation (e.g. `Target: 15m - 30m`).
  * Instant signals: `⚡ Volume Inflow`, `🚀 Bullish Momentum Breakout`, `🌊 Whale Accumulation`.
* **Market Climate Weather:**
  * Live visual macroeconomic sentiment (☀️ Bullish Surge, ⛅ Range, 🌧️ Bearish Correction).
  * Market surge index and institutional volume velocity metrics.
* **Multi-Category Quick Tabs:**
  * **`🔥 SOAR (%)`**: Ranked by immediate surge & breakout probability.
  * **`📈 GAINERS`**: Ranked by top 24-hour percentage performance.
  * **`⚡ VOLUME`**: Ranked by 24-hour trading turnover and accumulation.
* **Live Exchange Market Tickers:**
  * Live prices and 24h change for `BTC`, `ETH`, `SOL`, `SUI`, `DOGE`, `PEPE`, `NEAR`, `AVAX`, `LINK`, `XRP`, and more.
* **Pull to Refresh:** Instantly rescan exchange orderbooks with a downward swipe.

---

## 🚀 How to Build & Install Your APK (Automated Cloud Build)

This repository includes an automated GitHub Actions workflow (`.github/workflows/build-apk.yml`) that compiles, signs, and releases your `.apk` in 2 minutes on GitHub's cloud runners.

### Step 1: Create a GitHub Repository
1. Open [github.com/new](https://github.com/new) in your phone's browser.
2. Set the repository name to: **`crypto-soar-radar-android`** (choose **Public** or Private).
3. Tap **Create repository**.

### Step 2: Push the Code from Termux
In your Termux terminal, run:
```bash
cd ~/projects/crypto-soar-radar-android
git push -u origin main
```
*(Or simply run `./push.sh`)*

### Step 3: Download & Install Your APK
1. Open your repository on GitHub.
2. Tap the **Actions** tab (or **Releases** on the right side).
3. Tap the latest workflow run $\rightarrow$ scroll to **Artifacts** $\rightarrow$ tap **CryptoSoarRadar-APK** (or go to **Releases** and tap `CryptoSoarRadar.apk`).
4. Tap the downloaded file on your phone to **Install** and explore!
