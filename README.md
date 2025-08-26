# Yoga App - Full Stack Application

A full-stack web application for yoga session management, built with Angular (frontend) and Spring Boot (backend).

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Application Installation](#application-installation)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Coverage Reports](#coverage-reports)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)

## 🔧 Prerequisites

Before starting, ensure you have the following installed:

- **Node.js** (version 16+ recommended)
- **npm** (comes with Node.js)
- **Java** (version 21)
- **Maven** (version 3.6+)
- **MySQL** (version 8.0+)
- **Git**

## 🗄️ Database Setup

### 1. Install MySQL

Download and install MySQL from [official website](https://dev.mysql.com/downloads/).

### 2. Create Database and User

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database
CREATE DATABASE test;

-- Create user (optional, you can use root)
CREATE USER 'user'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON test.* TO 'user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Initialize Database Schema

The database schema will be automatically created when you first run the Spring Boot application. The initial data includes:

- Default admin user: `yoga@studio.com` / `test!1234`
- Sample teachers: Margot DELAHAYE, Hélène THIERCELIN

## 🚀 Application Installation

### Backend (Spring Boot)

```bash
# Navigate to backend directory
cd back

# Install dependencies and compile
mvn clean install

# Skip tests during installation (optional)
mvn clean install -DskipTests
```

### Frontend (Angular)

```bash
# Navigate to frontend directory
cd front

# Install dependencies
npm install
```

## ▶️ Running the Application

### 1. Start the Backend Server

```bash
cd back
mvn spring-boot:run
```

The backend server will start on `http://localhost:8080`

### 2. Start the Frontend Development Server

```bash
cd front
npm start
```

The frontend application will be available at `http://localhost:4200`

### 3. Access the Application

- **Frontend**: http://localhost:4200
- **Backend API**: http://localhost:8080/api
- **Default Admin Login**: 
  - Email: `yoga@studio.com`
  - Password: `test!1234`

## 🧪 Testing

### Backend Tests (Spring Boot + JUnit)

```bash
cd back

# Run all tests
mvn test

# Run tests with coverage
mvn clean test

# Run specific test class
mvn test -Dtest=SessionServiceTest

# Run integration tests
mvn test -Dtest=*IntegrationTest
```

### Frontend Unit Tests (Jest)

```bash
cd front

# Run all unit tests
npm run test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage
```

### End-to-End Tests (Cypress)

```bash
cd front

# Run e2e tests in headless mode
npm run e2e:ci

# Open Cypress GUI for interactive testing
npm run cypress:open

# Run Cypress tests in CLI
npm run cypress:run
```

## 📊 Coverage Reports

### Backend Coverage (JaCoCo)

```bash
cd back

# Generate coverage report
mvn clean test

# Open coverage report
open target/site/jacoco/index.html
```

The backend coverage report will be available at `back/target/site/jacoco/index.html`

### Frontend Unit Test Coverage (Jest)

```bash
cd front

# Generate unit test coverage
npm run test:coverage

# Open coverage report
open coverage/unit/lcov-report/index.html
```

### Frontend E2E Coverage (Cypress + NYC)

```bash
cd front

# Generate e2e coverage (run tests + generate report)
npm run e2e:ci && npm run e2e:coverage

# Open coverage report
open coverage/e2e/lcov-report/index.html
```

### Coverage Report Locations

- **Backend**: `back/target/site/jacoco/index.html`
- **Frontend Unit**: `front/coverage/unit/lcov-report/index.html`
- **Frontend E2E**: `front/coverage/e2e/lcov-report/index.html`

## 📁 Project Structure

```
├── back/                           # Spring Boot backend
│   ├── src/main/java/             # Application source code
│   ├── src/test/java/             # Test source code
│   ├── src/main/resources/        # Configuration files
│   ├── target/                    # Build output
│   └── pom.xml                    # Maven configuration
├── front/                         # Angular frontend
│   ├── src/app/                   # Application source code
│   ├── cypress/e2e/              # E2E tests
│   ├── coverage/                  # Coverage reports
│   │   ├── unit/                 # Jest coverage
│   │   └── e2e/                  # Cypress coverage
│   ├── package.json               # npm configuration
│   └── angular.json               # Angular CLI configuration
└── README.md                      # This file
```

## 🔗 API Documentation

### Authentication Endpoints

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login

### User Endpoints

- `GET /api/user/{id}` - Get user by ID
- `DELETE /api/user/{id}` - Delete user

### Session Endpoints

- `GET /api/session` - Get all sessions
- `GET /api/session/{id}` - Get session by ID
- `POST /api/session` - Create new session
- `PUT /api/session/{id}` - Update session
- `DELETE /api/session/{id}` - Delete session
- `POST /api/session/{id}/participate/{userId}` - Join session
- `DELETE /api/session/{id}/participate/{userId}` - Leave session

### Teacher Endpoints

- `GET /api/teacher` - Get all teachers
- `GET /api/teacher/{id}` - Get teacher by ID

## 🛠️ Development Commands Summary

```bash
# Backend
cd back
mvn spring-boot:run                    # Start backend server
mvn test                              # Run tests
mvn clean test                        # Run tests with coverage

# Frontend
cd front
npm start                             # Start development server
npm run test                          # Run unit tests
npm run test:coverage                 # Unit test coverage
npm run e2e:ci                        # Run e2e tests
npm run e2e:coverage                  # Generate e2e coverage
npm run cypress:open                  # Open Cypress GUI
```

## 🏗️ Built With

- **Backend**: Spring Boot 3.5, Spring Security, Spring Data JPA, JWT, MySQL
- **Frontend**: Angular 14, Angular Material, RxJS
- **Testing**: JUnit 5, Mockito (backend), Jest, Cypress (frontend)
- **Coverage**: JaCoCo (backend), NYC/Istanbul (frontend)

## 👥 Default Users

The application comes with pre-configured users:

- **Admin User**: 
  - Email: `yoga@studio.com`
  - Password: `test!1234`
  - Role: Administrator (can create/edit/delete sessions)

You can register new regular users through the application interface.