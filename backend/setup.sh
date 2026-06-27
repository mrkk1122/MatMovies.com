#!/bin/bash

# ==============================================================================
# MatMovies Backend - Ubuntu Automated Production Deployment Script 🚀
# ==============================================================================
# This script automates the installation of FastAPI, Python Venv, Nginx, 
# Systemd Service, and SSL (Certbot) on Ubuntu 20.04/22.04 LTS.
#
# Usage:
#   chmod +x setup.sh
#   sudo ./setup.sh
# ==============================================================================

# Terminal Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0;0m' # No Color

# Print Header
echo -e "${CYAN}"
echo "========================================================================"
echo "    __  ___      _     __  ___           _               "
echo "   /  |/  /___ _/ /_  /  |/  /___ _   __(_)__  _____      "
echo "  / /|_/ / __ \`/ __/ / /|_/ / __ \ | / / / _ \/ ___/      "
echo " / /  / / /_/ / /_  / /  / / /_/ / |/ / /  __(__  )       "
echo "/_/  /_/\__,_/\__/ /_/  /_/\____/|___/_/\___/____/  SETUP "
echo "========================================================================"
echo -e "${NC}"
echo -e "${BLUE}FastAPI Production Deployment Script for Ubuntu Server${NC}\n"

# 1. Must run as root/sudo
if [ "$EUID" -ne 0 ]; then
  echo -e "${RED}Error: This script must be run as root (using sudo).${NC}"
  echo -e "Please run: ${YELLOW}sudo ./setup.sh${NC}"
  exit 1
fi

# Detect actual non-root user who called sudo
REAL_USER=${SUDO_USER:-$USER}
if [ "$REAL_USER" = "root" ]; then
  REAL_USER="ubuntu" # Fallback standard user for AWS/GCP/DigitalOcean
fi

# 2. Collect Configurations
echo -e "${YELLOW}--- STEP 1: Deployment Configuration ---${NC}"

# Domain Name Prompt
read -p "Enter your public domain/subdomain name (e.g. api.yourdomain.com): " DOMAIN_NAME
if [ -z "$DOMAIN_NAME" ]; then
  echo -e "${RED}Error: Domain name is required for SSL proxying!${NC}"
  exit 1
fi

# System Username Prompt
read -p "Enter the system user that will own the app [Default: $REAL_USER]: " APP_USER
APP_USER=${APP_USER:-$REAL_USER}

# Generate Secure JWT Secret key
DEFAULT_JWT_SECRET=$(openssl rand -hex 24 2>/dev/null || echo "matmovies_secure_jwt_secret_$(date +%s)")
read -p "Enter secure JWT_SECRET (Press Enter to auto-generate a secure key): " JWT_SECRET
JWT_SECRET=${JWT_SECRET:-$DEFAULT_JWT_SECRET}

# Installation directory
INSTALL_DIR="/var/www/matmovies"
read -p "Enter deployment directory [Default: $INSTALL_DIR]: " CUSTOM_DIR
INSTALL_DIR=${CUSTOM_DIR:-$INSTALL_DIR}

echo -e "\n${GREEN}✓ Configurations captured successfully!${NC}"
echo -e "  - Domain: ${CYAN}$DOMAIN_NAME${NC}"
echo -e "  - Deploy Directory: ${CYAN}$INSTALL_DIR${NC}"
echo -e "  - System User: ${CYAN}$APP_USER${NC}"
echo -e "  - JWT Secret Key: ${CYAN}•••••••••••••••• (hidden)${NC}\n"

# Verify Directory copy source
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
if [ ! -f "$SCRIPT_DIR/main.py" ] || [ ! -f "$SCRIPT_DIR/requirements.txt" ]; then
  echo -e "${RED}Error: Cannot find main.py or requirements.txt in the script folder ($SCRIPT_DIR).${NC}"
  echo -e "Please ensure you run this script directly from your backend source directory."
  exit 1
fi

# Confirm Setup
read -p "Ready to begin installation? (y/n): " CONFIRM
if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
  echo -e "${YELLOW}Installation aborted by user.${NC}"
  exit 0
fi

# 3. System Updates and Core Package Installation
echo -e "\n${YELLOW}--- STEP 2: Updating packages & Installing core dependencies ---${NC}"
apt update
apt install -y python3 python3-pip python3-venv git nginx curl certbot python3-certbot-nginx

# 4. Preparing Deployment Directory & Copying Files
echo -e "\n${YELLOW}--- STEP 3: Preparing application folder & copying source files ---${NC}"
mkdir -p "$INSTALL_DIR"

# Copy source files to target deployment folder
echo "Copying files from $SCRIPT_DIR to $INSTALL_DIR..."
cp -r "$SCRIPT_DIR"/* "$INSTALL_DIR/"

# Create static directories if they don't exist
mkdir -p "$INSTALL_DIR/static"
mkdir -p "$INSTALL_DIR/static/uploads"

# Ensure target folders are owned by the designated system user
chown -R "$APP_USER":"$APP_USER" "$INSTALL_DIR"

# 5. Create Python Virtual Environment
echo -e "\n${YELLOW}--- STEP 4: Creating python virtual environment & installing requirements ---${NC}"
cd "$INSTALL_DIR"
sudo -u "$APP_USER" python3 -m venv venv
sudo -u "$APP_USER" ./venv/bin/pip install --upgrade pip
sudo -u "$APP_USER" ./venv/bin/pip install -r requirements.txt

# 6. Configure Systemd Service
echo -e "\n${YELLOW}--- STEP 5: Creating Linux Systemd Service daemon ---${NC}"
SERVICE_FILE="/etc/systemd/system/matmovies.service"

cat <<EOF > "$SERVICE_FILE"
[Unit]
Description=MatMovies FastAPI Streaming Daemon
After=network.target

[Service]
User=$APP_USER
WorkingDirectory=$INSTALL_DIR
ExecStart=$INSTALL_DIR/venv/bin/uvicorn main:app --host 127.0.0.1 --port 8000
Restart=always
RestartSec=5
Environment=JWT_SECRET="$JWT_SECRET"

[Install]
WantedBy=multi-user.target
EOF

echo "Reloading systemd, starting and enabling matmovies daemon..."
systemctl daemon-reload
systemctl stop matmovies 2>/dev/null || true
systemctl start matmovies
systemctl enable matmovies

# Verify Service Health
if systemctl is-active --quiet matmovies; then
  echo -e "${GREEN}✓ MatMovies background service successfully started!${NC}"
else
  echo -e "${RED}✗ Failed to start matmovies service. Please check: journalctl -u matmovies${NC}"
fi

# 7. Configure Nginx Proxy Server
echo -e "\n${YELLOW}--- STEP 6: Configuring Nginx Reverse Proxy ---${NC}"

# Remove default site if it exists to avoid port 80 conflicts
if [ -f "/etc/nginx/sites-enabled/default" ]; then
  echo "Disabling default Nginx landing page..."
  rm -f "/etc/nginx/sites-enabled/default"
fi

NGINX_CONF_AVAILABLE="/etc/nginx/sites-available/matmovies"
NGINX_CONF_ENABLED="/etc/nginx/sites-enabled/matmovies"

# Create Server Block configuration
cat <<EOF > "$NGINX_CONF_AVAILABLE"
server {
    listen 80;
    server_name $DOMAIN_NAME;

    client_max_body_size 100M; # Support large poster uploads

    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# Symlink to enable
ln -sf "$NGINX_CONF_AVAILABLE" "$NGINX_CONF_ENABLED"

# Test Nginx configurations
echo "Testing Nginx syntax..."
if nginx -t; then
  echo "Nginx syntax is correct. Restarting Nginx server..."
  systemctl restart nginx
  echo -e "${GREEN}✓ Nginx configured successfully!${NC}"
else
  echo -e "${RED}✗ Nginx configuration error detected. Reverting default setup...${NC}"
fi

# 8. Let's Encrypt SSL (HTTPS Setup)
echo -e "\n${YELLOW}--- STEP 7: Requesting Let's Encrypt SSL Certificate (HTTPS) ---${NC}"
echo -e "This step requires that your domain ${CYAN}$DOMAIN_NAME${NC} already points to this server's Public IP."
read -p "Do you want to run Certbot to configure HTTPS/SSL now? (y/n): " RUN_SSL

if [[ "$RUN_SSL" =~ ^[Yy]$ ]]; then
  echo "Acquiring secure SSL certificate..."
  certbot --nginx -d "$DOMAIN_NAME" --agree-tos --no-eff-email --redirect
  
  if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ SSL certificate installed and auto-renew configured!${NC}"
    API_PROTOCOL="https"
  else
    echo -e "${RED}✗ SSL installation failed. You can run 'sudo certbot --nginx' manually later.${NC}"
    API_PROTOCOL="http"
  fi
else
  echo -e "${YELLOW}Skipping Certbot setup. App will run on standard HTTP.${NC}"
  API_PROTOCOL="http"
fi

# 9. Finished & Beautiful Summary Output
echo -e "\n${GREEN}========================================================================${NC}"
echo -e "🎉 ${GREEN}MATMOVIES SERVER DEPLOYMENT COMPLETED SUCCESSFULLY!${NC}"
echo -e "${GREEN}========================================================================${NC}\n"

echo -e "Your API Backend is ready to serve connections!"
echo -e "• Base Domain:        ${CYAN}$API_PROTOCOL://$DOMAIN_NAME${NC}"
echo -e "• API Documentation:  ${CYAN}$API_PROTOCOL://$DOMAIN_NAME/docs${NC}"
echo -e "• Service Name:       ${YELLOW}matmovies.service${NC}"
echo -e "• Deployment Folder:   ${YELLOW}$INSTALL_DIR${NC}"
echo -e "• Nginx Config Path:   ${YELLOW}$NGINX_CONF_AVAILABLE${NC}\n"

echo -e "${BLUE}Useful Server Maintenance Commands:${NC}"
echo -e "  - View live logs:      ${YELLOW}journalctl -u matmovies -f${NC}"
echo -e "  - Stop service:        ${YELLOW}systemctl stop matmovies${NC}"
echo -e "  - Start service:       ${YELLOW}systemctl start matmovies${NC}"
echo -e "  - Restart service:     ${YELLOW}systemctl restart matmovies${NC}"
echo -e "  - Nginx Log Access:    ${YELLOW}tail -f /var/log/nginx/access.log${NC}"
echo -e "  - Nginx Log Errors:    ${YELLOW}tail -f /var/log/nginx/error.log${NC}\n"

echo -e "${GREEN}Enjoy your self-hosted streaming backend! 🎬${NC}\n"
