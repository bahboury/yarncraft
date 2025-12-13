# Yarncraft

A modular monolith Spring Boot application for managing products, inventory, orders, and users.

Project structure (real project structure):

```text
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

Notes:
- This README has been updated to reflect the real project structure you provided.
- If you'd like, I can also add build/run instructions, Docker Compose examples, or a contributors section.
