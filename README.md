# 🧶 YarnCraft Marketplace — Modular Monolith Platform

A full-stack, modular monolithic marketplace for custom handmade yarn products such as bags, purses, mirrors, tablecloths, and keychains.

YarnCraft supports:

- ✔ Vendor onboarding & admin approval
- ✔ Product customization (attributes like color and size)
- ✔ Customer ordering & cart management
- ✔ Real-time inventory tracking
- ✔ Aspect-Oriented Programming (AOP) for logging & stock checks
- ✔ Dockerized deployment

---

## 📁 Project Structure

This project follows a Modular Monolith approach: a single Spring Boot application with separate packages for each business module to maintain loose coupling.

```
yarncraft/
├── .mvn/wrapper/                       <-- Maven Wrapper
├── docker/
│   └── init.sql                        <-- DB Init Script (Users, tables)
├── src/
│   ├── main/
│   │   ├── java/com/swe2project/yarncraft/
│   │   │   ├── aspect/                 <-- AOP
│   │   │   │   └── LoggingAspect.java  <-- Execution time logging
│   │   │   ├── common/                 <-- Shared Resources
│   │   │   │   ├── dto/                <-- ApiResponse.java (Standard JSON format)
│   │   │   │   ├── exception/          <-- GlobalExceptionHandler.java
│   │   │   │   └── util/               <-- SecurityUtils.java
│   │   │   ├── config/                 <-- System Config
│   │   │   │   ├── OpenApiConfig.java  <-- Swagger UI Setup
│   │   │   │   ├── SecurityConfig.java <-- JWT & Role Security
│   │   │   │   └── WebConfig.java      <-- CORS Settings
│   │   │   ├── modules/                <-- THE MODULAR MONOLITH CORE
│   │   │   │   ├── inventory/          <-- [Inventory Module]
│   │   │   │   │   ├── controller/     <-- InventoryController.java
│   │   │   │   │   ├── entity/         <-- InventoryItem.java (Stock logic)
│   │   │   │   │   ├── repository/     <-- InventoryRepository.java
│   │   │   │   │   └── service/        <-- InventoryService.java (Restock/Reserve)
│   │   │   │   ├── order/              <-- [Order Module]
│   │   │   │   │   ├── controller/     <-- OrderController.java
│   │   │   │   │   ├── dto/            <-- OrderRequest.java
│   │   │   │   │   ├── entity/         <-- Order.java, OrderItem.java
│   │   │   │   │   ├── repository/     <-- OrderRepository.java
│   │   │   │   │   └── service/        <-- OrderService.java (Transaction logic)
│   │   │   │   ├── product/            <-- [Product Module]
│   │   │   │   │   ├── controller/     <-- ProductController.java
│   │   │   │   │   ├── dto/            <-- ProductRequest.java
│   │   │   │   │   ├── entity/         <-- Product.java, Category.java
│   │   │   │   │   ├── repository/     <-- ProductRepository.java
│   │   │   │   │   └── service/        <-- ProductService.java (Calls Inventory)
│   │   │   │   └── user/               <-- [User Module]
│   │   │   │       ├── controller/     <-- Auth/Admin/UserController.java
│   │   │   │       ├── dto/            <-- Login/Register/Profile DTOs
│   │   │   │       ├── entity/         <-- User.java, Role.java, VendorApplication.java
│   │   │   │       ├── repository/     <-- UserRepository.java
│   │   │   │       └── service/        <-- AuthService.java, UserService.java
│   │   │   ├── security/               <-- JWT Implementation
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtService.java
│   │   │   └── YarncraftApplication.java <-- Main Entry Point
│   │   └── resources/
│   │       ├── application.properties  <-- Points to Config Server (Port 8888)
│   │       ├── application.properties.backup <-- Original Config (Safe keeping)
│   │       └── schema.sql              <-- Database Schema
│   └── test/                           <-- Tests
├── compose.yaml                        <-- Docker Compose (MySQL + App)
├── Dockerfile                          <-- App Container Config
├── mvnw / mvnw.cmd                     <-- Maven Wrapper
├── pom.xml                             <-- Dependencies (Spring Cloud Client)
└── README.md                           <-- Project Documentation
```

---

## 🏗 System Architecture

This is a single deployable unit that connects to a centralized MySQL database.

Core modules:

1. **User Module:** Registration, login (JWT), role-based access control (Admin / Vendor / Customer).
2. **Product Module:** Product CRUD, filtering, categories, vendor-product association.
3. **Order Module:** Cart logic, placing orders, item customization.
4. **Inventory Module:** Stock level management and prevention of over-selling.

Technical highlights:

- **Database:** Single MySQL instance with foreign keys connecting User ⇄ Product ⇄ Order.
- **Security:** Spring Security with a JWT filter chain.
- **AOP:** Cross-cutting concerns like execution-time logging and stock validation are implemented with Spring AOP.
- **Docker:** App and DB can run in containers for consistent deployment.

---

## 🌿 Branch Strategy

- `main` → Production-ready code
- `dev` → Shared development branch
- `module/user` → User module implementation
- `module/order` → Order module implementation
- `feature/*` → Feature branches

---

## 🚀 Getting Started (How to Run)

Two modes are supported: Coding Mode (fast iteration during development) and Production Mode (full Dockerized run).

### Prerequisites

- Java 17+
- Docker Desktop (running)
- MySQL client (MySQL Workbench / DBeaver optional)

### 🛠 Option 1 — Coding Mode (Recommended for development)

Use this when actively editing the code and running locally from your IDE.

1. Clone the repository:

```bash
git clone https://github.com/your-username/yarncraft.git
cd yarncraft
```

2. Start only the database:

- Runs MySQL in the background on port `3306`.
- Ensure you do not have another MySQL instance running on the same port.

```bash
docker compose up -d mysql
```

3. Run the backend from your IDE:

- Open the project in IntelliJ IDEA (or your preferred IDE).
- Run `YarncraftApplication.java`.
- The app will be available at `http://localhost:8080`.

### 🐳 Option 2 — Production Mode (Full Docker)

Use this to test the final Dockerized setup (app + DB in containers).

1. Stop any running compose services:

```bash
docker compose down
```

2. Build and run everything:

Windows:

```powershell
.\mvnw clean package -DskipTests
docker compose up --build
```

Mac / Linux:

```bash
./mvnw clean package -DskipTests
docker compose up --build
```

3. Access the app:

- Backend: `http://localhost:8080`

---

## 🧩 Task Division

### 🟦 Task 1 — Project Setup & Architecture
Assigned to: Anthony Ashraf & Bahy Mohy
- Project skeleton, Docker setup, DB config, AOP setup, SRS & diagrams.

### 🟩 Task 2 — User Module
Assigned to: Bahy Mohy
- User entity, JWT logic, vendor application flow, admin approval process.

### 🟧 Task 3 — Product Module
Assigned to: Eslam Ahmed
- Product CRUD, category filtering, vendor-product linkage.

### 🟫 Task 4 — Order Module
Assigned to: Seif Emad
- Cart logic, order placement, customization attributes (color, size).

### 🟨 Task 5 — Inventory Module & AOP
Assigned to: Aser ElSayed
- Stock deduction logic; AOP aspects for logging and stock checks.

### 🟪 Task 6 — Frontend (React.js)
Assigned to: Aser ElSayed
- Customer storefront (browse/order) and vendor dashboard (manage products).

---

## 📝 Future Enhancements

- [ ] Email notifications on order placement
- [ ] Payment gateway simulation
- [ ] Advanced analytics dashboard for admins

---

## Contributing

If you want to contribute, please:

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Open a pull request against `dev`
4. Ensure linting and tests (if any) pass
