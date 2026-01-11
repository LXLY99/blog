Here is a refined, professional `README.md` for your project.

It highlights your unique features (like the SMMS "Safe Delete" logic), documents the tech stack, and provides clear deployment instructions for Zeabur, which is crucial given the configuration steps we just went through.

---

# 📝 LXLY Blog System

A lightweight, high-performance personal blogging system built with **Spring Boot 3**. It features a glassmorphism UI, secure JWT authentication, and an intelligent image management system integrated with the **SMMS Cloud API**.

---

## ✨ Key Features

### 🔐 User & Security

* **JWT Authentication:** Stateless, secure login system.
* **Email Verification:** Registration and password resets require email code verification (via SMTP).
* **Role-Based Access:** Distinct User vs. Admin capabilities.

### 🖼️ Smart Image Management (SMMS Integration)

* **Cloud Storage:** All images (avatars, covers) are auto-uploaded to SMMS.
* **Smart Clean-up:**
* Automatically deletes old images from the cloud when a user changes their avatar or cover.
* **🛡️ Safety Lock:** Logic to prevent accidental deletion of the system's "Default Avatar."


* **Database Optimization:** Stores both `URL` and `Deletion Hash` to ensure full control over remote assets.

### 📝 Content Management

* **Markdown Support:** Write posts in Markdown, rendered securely to HTML.
* **Categorization:** Organize posts via Categories and Tags.
* **Draft System:** Save posts as drafts before publishing.

### 📊 Admin Dashboard

* **Server Monitor:** Real-time tracking of CPU, Memory, and System Load.
* **Site Statistics:** Overview of total posts, visitors, and runtime.

---

## 🛠️ Tech Stack

* **Backend:** Java 17, Spring Boot 3.2.5
* **Database:** MySQL 8.0 (JPA/Hibernate)
* **Cache:** Redis (Verification codes, user sessions, system metrics)
* **Security:** Spring Security 6, JWT
* **Storage:** SMMS API (Image Hosting)
* **Frontend:** HTML5, CSS3 (Glassmorphism), Vanilla JS
* **Tools:** Lombok, Gson, OkHttp3

---

## 🚀 Quick Start (Local Development)

### 1. Prerequisites

* JDK 17+
* MySQL 8.0+
* Redis

### 2. Database Setup

Create a database named `blog` and import the schema (tables: `user`, `post`, `settings`, `verify_code`).
*Ensure your tables include the `_hash` columns for image management:*

```sql
ALTER TABLE `user` ADD COLUMN `avatar_hash` VARCHAR(100);
ALTER TABLE `post` ADD COLUMN `cover_hash` VARCHAR(100);

```

### 3. Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog?useSSL=false
    username: root
    password: YOUR_LOCAL_PASSWORD
  data:
    redis:
      host: localhost
      port: 6379

smms:
  token: YOUR_SMMS_API_TOKEN
  default-avatar: https://s2.loli.net/... (Your protected image URL)

```

### 4. Run

Start the application via your IDE or command line:

```bash
mvn spring-boot:run

```

Access the site at `http://localhost:8080`.

---

## ☁️ Deployment (Zeabur)

This project is optimized for deployment on **Zeabur**.

### 1. Services

Create a project in Zeabur and add three services:

1. **MySQL**
2. **Redis**
3. **Spring Boot** (Connect your GitHub repo)

### 2. Environment Variables

In the **Spring Boot Service** -> **Variables** tab, add the following. **Do not** hardcode these in your Java files for production.

| Variable Key | Value (Example) | Description |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql.zeabur.internal:3306/blog...` | Internal MySQL Host |
| `SPRING_DATASOURCE_USERNAME` | `root` | DB User |
| `SPRING_DATASOURCE_PASSWORD` | `******` | DB Password |
| `SPRING_DATA_REDIS_HOST` | `redis.zeabur.internal` | **Critical:** Use internal host, NOT localhost |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis Port |
| `SPRING_DATA_REDIS_PASSWORD` | `******` | Redis Password |
| `SMMS_TOKEN` | `******` | Your SMMS API Token |

### 3. Port Configuration

Ensure the service port in Zeabur settings is set to **8080**.

---

## 📂 Project Structure

```bash
src/main/java/org/lxly/blog
├── config        # Security, Cors, MVC Config
├── controller    # API Endpoints (Auth, Post, User, Upload)
├── dto           # Data Transfer Objects (Req/Res)
├── entity        # JPA Entities (User, Post)
├── repository    # Data Access Layer
├── service       # Business Logic (Auth, Post, SmmsService)
└── utils         # JWT Util, Result Helper

```

---

## 🛡️ Image Deletion Logic

To save cloud space, the system deletes old images when they are replaced. The logic in `SmmsService.java` protects your default assets:

```java
public void delete(String hash, String url) {
    // 1. Safety Check: Never delete the default avatar
    if (url.equals(defaultAvatar)) {
        log.warn("🛡️ SAFETY: Prevented deletion of Default Avatar");
        return;
    }
    // 2. Proceed to delete from SMMS using the hash
    // ...
}

```

---

## 📄 License

This project is licensed under the MIT License.
