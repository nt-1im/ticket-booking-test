# Deploying RailPass to Render

This guide outlines step-by-step instructions to deploy the RailPass Spring Boot Train Ticket Booking Application to **Render** using a MySQL database.

---

## Prerequisites
1. A **GitHub** account.
2. A **Render** account (https://render.com).
3. A hosted **MySQL Database**. Since Render does not provide a free MySQL database natively, you can easily set up a free MySQL instance on:
   - **Aiven** (https://aiven.io - Recommended, free tier MySQL)
   - **Clever Cloud** (https://clever-cloud.com)
   - **Tidb Cloud** (https://pingcap.com)

---

## Step 1: Push Code to GitHub
1. Initialize git in the root folder:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of RailPass ticket booking system"
   ```
2. Create a repository on GitHub and link it:
   ```bash
   git remote add origin https://github.com/yourusername/ticket-booking.git
   git branch -M main
   git push -u origin main
   ```

---

## Step 2: Set Up MySQL Database
1. Create a MySQL database instance on your cloud provider (e.g., Aiven).
2. Note down the connection parameters:
   - **Host** (e.g., `mysql-xxxx.aivencloud.com`)
   - **Port** (e.g., `12345` or default `3306`)
   - **Database Name** (e.g., `defaultdb` or `ticketdb`)
   - **Username** (e.g., `avnadmin` or `root`)
   - **Password**

---

## Step 3: Deploy on Render
1. Log in to your Render Dashboard and click **New > Web Service**.
2. Connect your GitHub account and select your `ticket-booking` repository.
3. Configure the Web Service:
   - **Name:** `rail-pass` (or any custom name)
   - **Region:** Choose the region closest to you (e.g., Singapore, Oregon)
   - **Branch:** `main`
   - **Runtime:** **Docker** (Render will automatically detect our `Dockerfile` and compile/run the application)
   - **Plan:** Free

4. Scroll down and click **Advanced** to add the following **Environment Variables**:
   
   | Key | Value | Description |
   | :--- | :--- | :--- |
   | `SPRING_PROFILES_ACTIVE` | `prod` | Activates the MySQL database configuration |
   | `SPRING_DATASOURCE_URL` | `jdbc:mysql://YOUR_DB_HOST:YOUR_DB_PORT/YOUR_DB_NAME?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` | Your MySQL JDBC connection string |
   | `SPRING_DATASOURCE_USERNAME` | `YOUR_DB_USERNAME` | Your MySQL database username |
   | `SPRING_DATASOURCE_PASSWORD` | `YOUR_DB_PASSWORD` | Your MySQL database password |

5. Click **Create Web Service**.

---

## Step 4: Access and Verify
1. Render will fetch the code, run the Maven multi-stage Docker build, and deploy the container. This may take 3-5 minutes on the free tier.
2. Once the logs show `Started TicketBookingApplication in X seconds`, click the live URL provided by Render (e.g., `https://rail-pass.onrender.com`).
3. Log in with the pre-seeded accounts:
   - **Admin:** `admin` / `admin123`
   - **Seller:** `seller` / `seller123`
   - **Buyer:** `buyer` / `buyer123`
