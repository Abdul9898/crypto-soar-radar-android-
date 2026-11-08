#!/bin/bash
set -e

echo "🚀 Committing and pushing Crypto Soar Radar to GitHub..."
git add .
git commit -m "Update Crypto Soar Radar Android App" || echo "No changes to commit"
git push -u origin main || git push -u origin master
echo "✅ Pushed to GitHub! Check the Actions tab on GitHub to download your new APK."
