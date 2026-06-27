# MatMovies Server: Ubuntu Deployment & Connection Guide 🚀

This document provides a comprehensive, step-by-step guide to hosting the **MatMovies FastAPI Backend** on your custom Ubuntu Server (v20.04 or v22.04 LTS), configuring it to run continuously in the background, securing it with Nginx and SSL (HTTPS), and connecting your MatMovies Android app to it!

---

## 📋 Table of Contents
1. [Prerequisites](#1-prerequisites)
2. [⚡ Quick Start: Automated Installation (Highly Recommended)](#-quick-start-automated-installation-highly-recommended)
3. [Step 1: Update System & Install Python Dependencies](#step-1-update-system--install-python-dependencies)
3. [Step 2: Deploy Code to Server](#step-2-deploy-code-to-server)
4. [Step 3: Setup Virtual Environment & Install Requirements](#step-3-setup-virtual-environment--install-requirements)
5. [Step 4: Configure Systemd Service (24/7 Autostart)](#step-4-configure-systemd-service-247-autostart)
6. [Step 5: Install and Configure Nginx (Reverse Proxy)](#step-5-install-and-configure-nginx-reverse-proxy)
7. [Step 6: Secure Your Server with Free SSL (Let's Encrypt HTTPS)](#step-6-secure-your-server-with-free-ssl-lets-encrypt-https)
8. [Step 7: Connect the MatMovies Android App](#step-7-connect-the-matmovies-android-app)

---

## 1. Prerequisites
- An active **Ubuntu Server** with root or `sudo` access.
- A **Public IP Address** assigned to your server.
- A **Domain Name** (e.g., `api.matmovies.com` or `matmovies-backend.yourdomain.com`) pointed to your server's Public IP using an **A Record** in your DNS dashboard (required for SSL/HTTPS).

---

## ⚡ Quick Start: Automated Installation (Highly Recommended)

We have provided a fully interactive bash script `setup.sh` that automates all the manual steps below (Steps 1 through 6). It will automatically:
- Install Python virtual environment, PIP, and system dependencies.
- Copy backend files to `/var/www/matmovies`.
- Create a virtual environment and install all packages.
- Setup a continuous **systemd background daemon** with auto-restart.
- Configure an **Nginx Reverse Proxy** server block.
- Secure everything with a free, auto-renewing **Let's Encrypt SSL/HTTPS** certificate.

### How to use it:

1. Upload the `backend` folder containing `setup.sh` to your Ubuntu Server.
2. Log into your server via SSH, navigate to the folder, and make the script executable:
   ```bash
   chmod +x setup.sh
   ```
3. Run the script with root privileges:
   ```bash
   sudo ./setup.sh
   ```
4. Follow the colorful interactive prompts to enter your **domain name**, **system user**, and a secure **JWT Secret**.

---

## Step 1: Update System & Install Python Dependencies
Log into your Ubuntu Server via SSH:
```bash
ssh username@your_server_ip
```
Update local package definitions and install core packages:
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install python3 python3-pip python3-venv git nginx curl -y
```

---

## Step 2: Deploy Code to Server
Create an application directory at `/var/www/matmovies`:
```bash
sudo mkdir -p /var/www/matmovies
sudo chown -R $USER:$USER /var/www/matmovies
cd /var/www/matmovies
```

You can copy the `backend` folder files from your project workspace using Git or secure SCP transfers:
```bash
# Example if using Git:
# git clone <your-repository-url> .
# OR simply upload backend/main.py, backend/requirements.txt, and backend/schemas/
```
Ensure your folder structure on the server looks like this:
```text
/var/www/matmovies/
├── main.py
├── requirements.txt
└── schemas/
    ├── __init__.py
    ├── movie.py
    └── user.py
```

---

## Step 3: Setup Virtual Environment & Install Requirements
Create a lightweight isolated virtual environment to prevent package version conflicts:
```bash
python3 -m venv venv
source venv/bin/activate
```
Install the package dependencies inside the virtual environment:
```bash
pip install --upgrade pip
pip install -r requirements.txt
```
To test-run the server manually:
```bash
uvicorn main:app --host 127.0.0.1 --port 8000
```
*(Press `CTRL + C` to stop once you verify that it starts up with zero errors!)*

---

## Step 4: Configure Systemd Service (24/7 Autostart)
To ensure the backend runs continuously in the background, auto-starts on system reboots, and self-heals upon unexpected crashes, configure a standard Linux **systemd daemon**:

Create a new service configuration file:
```bash
sudo nano /etc/systemd/system/matmovies.service
```

Paste the following configurations into it:
```ini
[Unit]
Description=MatMovies FastAPI Streaming Daemon
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/var/www/matmovies
ExecStart=/var/www/matmovies/venv/bin/uvicorn main:app --host 127.0.0.1 --port 8000
Restart=always
RestartSec=5
Environment=JWT_SECRET="YOUR_CUSTOM_SECURE_JWT_SECRET_KEY"

[Install]
WantedBy=multi-user.target
```
*(Note: Change `User=ubuntu` to your actual server username, e.g. `root` or `ubuntu`.)*

Save and close nano (`CTRL + O` to write, `Enter`, then `CTRL + X` to exit).

Enable and start the daemon:
```bash
sudo systemctl daemon-reload
sudo systemctl start matmovies
sudo systemctl enable matmovies
```
To check if the service is running successfully:
```bash
sudo systemctl status matmovies
```

---

## Step 5: Install and Configure Nginx (Reverse Proxy)
Nginx acts as an extremely efficient web gateway, intercepting incoming connections and routing them securely to your local FastAPI background daemon.

Remove default configuration page and create a custom proxy definition:
```bash
sudo rm /etc/nginx/sites-enabled/default
sudo nano /etc/nginx/sites-available/matmovies
```

Paste the following server block configuration (replace `yourdomain.com` with your real domain pointed to this server IP):
```nginx
server {
    listen 80;
    server_name yourdomain.com;

    client_max_body_size 100M; # Support large poster uploads

    location / {
        proxy_pass http://127.0.0.1:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Save and close the file, then enable the site and verify Nginx syntax:
```bash
sudo ln -s /etc/nginx/sites-available/matmovies /etc/nginx/sites-enabled/
sudo nginx -t
```
If the syntax test is successful, reload Nginx:
```bash
sudo systemctl restart nginx
```

---

## Step 6: Secure Your Server with Free SSL (Let's Encrypt HTTPS)
Android 9 (API 28) and higher **strictly block cleartext HTTP traffic by default** to protect user credentials. You **MUST** establish a secure HTTPS connection. We will use Certbot to provision a free, globally recognized Let's Encrypt SSL Certificate.

Install Certbot and the Nginx plugin:
```bash
sudo apt install certbot python3-certbot-nginx -y
```

Obtain and configure the SSL certificates:
```bash
sudo certbot --nginx -d yourdomain.com
```
Follow the interactive prompts:
1. Enter your email for notification of renewals.
2. Agree to the Terms of Service.
3. Select whether to redirect HTTP traffic to HTTPS (Select **Redirect** - Option `2` to make all connections secure automatically!).

Your domain is now accessible over HTTPS (e.g. `https://yourdomain.com/docs` to view the beautiful interactive Swagger documentation!).

---

## Step 7: Connect the MatMovies Android App

To route your Android application to your brand-new production server:

1. **Locate your API Config**:
   Look at your Android App's Retrofit interface or network provider configuration (typically under `com/example/data/` or in `MainActivity`).
   
2. **Update the Base URL**:
   Replace the development or local URL with your production domain link:
   ```kotlin
   // Inside your networking file (e.g. Retrofit builder)
   const val BASE_URL = "https://yourdomain.com/api/v1/"
   ```

3. **Verify Manifest Cleartext Settings**:
   Since you are now using HTTPS, no special plain-text bypasses are required! Ensure standard network access permissions are declared in `/app/src/main/AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```

Deploy your app and watch as user records, movies, reviews, and streaming indicators sync securely with your cloud-hosted Ubuntu Server! 🚀
