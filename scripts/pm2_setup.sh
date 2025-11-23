#!/usr/bin/env bash

set -euo pipefail

APP_DIR="$HOME/FutureMeMailApp/server"
CACHE_DIR="$HOME/.npm-codex"

cd "$APP_DIR"

echo "Installing dependencies..."
NPM_CONFIG_CACHE="$CACHE_DIR" npm install

echo "Starting PM2 processes from ecosystem.config.cjs..."
pm2 start ecosystem.config.cjs || pm2 restart ecosystem.config.cjs

echo "Saving PM2 process list..."
pm2 save

echo "Configuring PM2 startup with systemd..."
sudo env PATH=$PATH:/usr/bin pm2 startup systemd -u "$(whoami)" --hp "$HOME"

echo "Done. PM2 will now auto-start on boot."
