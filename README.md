# Careerthon.AI — LinkedIn Profile Review SaaS

> A full-stack LinkedIn Profile Review platform built with Java Spring Boot.  
> **Developed by Priyanshu Shekhar** | Team: Priyanshu Shekhar, Md Afroz Hassan, Altamash Mallick, Pratikssha Kumari

## Features
- 🎯 Profile Scoring (0-100)
- 🔬 Detailed 15-point Analysis  
- 🤖 ATS Optimization & Skill Gap Analyzer
- 🔑 Keyword Enhancement & Categorization
- 🪄 AI Resume Bullet Generator
- ⚡ 1-Click Fast Test Presets
- ✍️ Headline Optimization
- 📝 About Section Improvement
- ⚙️ Skills Optimization
- 💼 Experience Enhancement
- 🚀 Visibility Boost
- 🎯 Recruiter Targeting
- 📈 Industry Benchmarking
- ⚡ Actionable Insights
- 📋 Performance Report
- 📧 Email Delivery
- 💳 **Payment Bypassed — 100% Free**

## Tech Stack
- Java 21+, Spring Boot 3.4
- Thymeleaf, Vanilla CSS, Vanilla JS
- H2 Database (embedded), Spring Data JPA

## Quick Start

```bash
# Run (no Maven install needed — uses wrapper)
.\mvnw.cmd spring-boot:run

# Build JAR
.\mvnw.cmd clean package

# Run JAR
java -jar target\Careerthon.AI-1.0.0.jar
```

Open: http://localhost:8080

## Deploy with Docker
```bash
.\mvnw.cmd clean package
docker build -t Careerthon.AI .
docker run -p 8080:8080 Careerthon.AI
```

## Deploy to Railway / Render
1. Push to GitHub
2. Connect repo to Railway or Render
3. Set Start command: `java -jar target/Careerthon.AI-1.0.0.jar`
4. Done — live URL in minutes!
