# 🚀 Enterprise Messenger — Free Cloud Deployment Guide

Deploy the entire platform for **$0** using free tiers of Render, Vercel, Neon, and Upstash.

---

## Step 1: Push Code to GitHub

```bash
cd /home/ttpl-lnve15-0252/WhatsApp
git init
git add .
git commit -m "Initial commit - Enterprise Messenger"
# Create a new repo on GitHub, then:
git remote add origin https://github.com/YOUR_USERNAME/enterprise-messenger.git
git branch -M main
git push -u origin main
```

---

## Step 2: Sign Up for Free Services

### 2a. Neon.tech (Free PostgreSQL)
1. Go to [neon.tech](https://neon.tech) → Sign up (free)
2. Create a new project → name it `messenger`
3. Create a database called `messenger_db`
4. Copy the connection string, it looks like:
   ```
   postgresql://USER:PASSWORD@ep-xxx.us-east-2.aws.neon.tech/messenger_db?sslmode=require
   ```
5. **Run the schema init SQL** in Neon's SQL editor:
   ```sql
   CREATE SCHEMA IF NOT EXISTS auth_schema;
   CREATE SCHEMA IF NOT EXISTS user_schema;
   CREATE SCHEMA IF NOT EXISTS messaging_schema;
   ```
6. Note down:
   - **JDBC URL**: `jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/messenger_db?sslmode=require`
   - **Username**: from the connection string
   - **Password**: from the connection string

### 2b. Upstash (Free Redis)
1. Go to [upstash.com](https://upstash.com) → Sign up (free)
2. Create a new **Redis** database → region: closest to you
3. Note down:
   - **Host**: `xxx.upstash.io`
   - **Port**: `6379`
   - **Password**: from the dashboard

### 2c. Aiven.io (Free Kafka)
1. Go to [aiven.io](https://aiven.io) → Sign up (free, no credit card)
2. Create a new project → name it `messenger`
3. Click "Create Service" → Select **Apache Kafka**
4. Select **Free Plan** → Cloud: **AWS** → Region: **Mumbai** (ap-south-1)
5. Name your service: `messenger-kafka`
6. Once it's "Running", go to the **"Topics"** tab and create:
   - `message.sent`
   - `message.delivered`
   - `message.read`
   - `user.registered`
7. In **"Overview"** tab, find **"Connection information"**:
   - **Service URI (Bootstrap Server)**: looks like `messenger-kafka-xxx.aivencloud.com:12345`
   - **User**: `avnadmin`
   - **Password**: from the dashboard
8. **Security Note**: Aiven uses SSL by default. For our Spring Boot apps using SASL_SSL/SCRAM, we need to enable it in Aiven console (Service Settings > Advanced configuration > `kafka.sasl_enabled_mechanisms`: SCRAM-SHA-256).

---

## Step 3: Deploy Backend on Render.com

### Option A: Manual Setup (Recommended for first time)

1. Go to [render.com](https://render.com) → Sign up (free)
2. Connect your GitHub account

**Deploy services in this order** (each as a "New Web Service"):

### Service 1: `service-registry`
- **Name**: `messenger-service-registry`
- **Root Directory**: `service-registry`
- **Runtime**: Docker
- **Plan**: Free
- **Environment Variables**:
  ```
  EUREKA_HOSTNAME=messenger-service-registry.onrender.com
  ```

### Service 2: `config-server`
- **Name**: `messenger-config-server`
- **Root Directory**: `config-server`
- **Runtime**: Docker
- **Plan**: Free
- **Environment Variables**:
  ```
  EUREKA_URL=https://messenger-service-registry.onrender.com/eureka/
  ```

### Service 3: `api-gateway`
- **Name**: `messenger-api-gateway`
- **Root Directory**: `api-gateway`
- **Runtime**: Docker
- **Plan**: Free
- **Environment Variables**:
  ```
  EUREKA_URL=https://messenger-service-registry.onrender.com/eureka/
  CORS_ALLOWED_ORIGINS=https://YOUR_VERCEL_APP.vercel.app
  REDIS_HOST=YOUR_UPSTASH_REDIS_HOST
  REDIS_PORT=6379
  REDIS_PASSWORD=YOUR_UPSTASH_REDIS_PASSWORD
  REDIS_SSL=true
  JWT_SECRET=YTJiM2M0ZDVlNmY3ZzhpOWoxazJsM200bjVvNnA3cThyOXMwdDF1MnYzdzR4NXk2ejdBMkIzQzRENUU2Rjc=
  ```

### Service 4: `auth-service`
- **Name**: `messenger-auth-service`
- **Root Directory**: `auth-service`
- **Runtime**: Docker
- **Plan**: Free
- **Environment Variables**:
  ```
  EUREKA_URL=https://messenger-service-registry.onrender.com/eureka/
  DATABASE_URL=jdbc:postgresql://ep-xxx.us-east-2.aws.neon.tech/messenger_db?sslmode=require&currentSchema=auth_schema
  DATABASE_USERNAME=YOUR_NEON_USERNAME
  DATABASE_PASSWORD=YOUR_NEON_PASSWORD
  KAFKA_BOOTSTRAP_SERVERS=YOUR_UPSTASH_KAFKA_ENDPOINT:9092
  KAFKA_SECURITY_PROTOCOL=SASL_SSL
  KAFKA_SASL_MECHANISM=SCRAM-SHA-256
  KAFKA_SASL_JAAS_CONFIG=org.apache.kafka.common.security.scram.ScramLoginModule required username="YOUR_KAFKA_USERNAME" password="YOUR_KAFKA_PASSWORD";
  REDIS_HOST=YOUR_UPSTASH_REDIS_HOST
  REDIS_PASSWORD=YOUR_UPSTASH_REDIS_PASSWORD
  REDIS_SSL=true
  ```

### Service 5: `user-service`
- **Name**: `messenger-user-service`
- **Root Directory**: `user-service`
- **Runtime**: Docker
- **Plan**: Free
- **Environment Variables**: Same as auth-service but change `currentSchema=user_schema`

### Service 6: `messaging-service`
- **Name**: `messenger-messaging-service`
- **Root Directory**: `messaging-service`
- **Runtime**: Docker
- **Plan**: Free
- **Environment Variables**: Same as auth-service but change `currentSchema=messaging_schema`

> ⚠️ **Important**: Render builds from the **Dockerfile** in each service directory. Make sure JARs are built before pushing (`mvn clean package -DskipTests`).

> ⚠️ **Render Free Tier**: Services sleep after 15 min of inactivity. First request takes ~30 seconds to wake up.

---

## Step 4: Deploy Frontend on Vercel

1. Go to [vercel.com](https://vercel.com) → Sign up (free)
2. Import your GitHub repo
3. Set **Root Directory** to `messenger-ui`
4. Set **Framework Preset** to `Vite`
5. Add environment variable:
   ```
   VITE_API_URL=https://messenger-api-gateway.onrender.com
   ```
6. Update `messenger-ui/vercel.json` — replace `YOUR_API_GATEWAY_URL` with your actual Render URL:
   ```json
   {
     "rewrites": [
       { "source": "/api/:path*", "destination": "https://messenger-api-gateway.onrender.com/api/:path*" },
       { "source": "/ws/:path*", "destination": "https://messenger-api-gateway.onrender.com/ws/:path*" }
     ]
   }
   ```
7. Deploy!

---

## Step 5: Update CORS

After Vercel gives you your URL (e.g., `https://enterprise-messenger.vercel.app`):

1. Go to Render → `api-gateway` → Environment → Update:
   ```
   CORS_ALLOWED_ORIGINS=https://enterprise-messenger.vercel.app
   ```
2. Redeploy the API Gateway

---

## ✅ Verification Checklist

- [ ] All 6 services show "Live" on Render dashboard
- [ ] Vercel frontend loads at your URL
- [ ] Can register a new user
- [ ] Can log in
- [ ] Can create a conversation and send messages
- [ ] Real-time messaging works via WebSocket

---

## 🆘 Troubleshooting

| Issue | Solution |
|-------|---------|
| Services won't start | Check Render logs for missing env vars |
| DB connection failed | Verify Neon JDBC URL includes `?sslmode=require` |
| CORS errors | Update `CORS_ALLOWED_ORIGINS` on api-gateway |
| Kafka errors | Verify SASL config and topic names on Upstash |
| WebSocket not connecting | Ensure `VITE_API_URL` points to gateway |
| Services sleeping | Free tier limitation — wake up on first request |
