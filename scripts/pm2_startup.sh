#!/usr/bin/env bash

set -euo pipefail

if [[ $EUID -eq 0 ]]; then
  echo "Please run this script as the connie user (not root)."
  exit 1
fi

echo "Running PM2 startup registration with sudo..."
sudo env PATH=$PATH:/usr/bin pm2 startup systemd -u "$(whoami)" --hp "$HOME"
echo "Done."
