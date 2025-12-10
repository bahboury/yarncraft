-- Disable foreign key checks to allow dropping tables if needed
SET
FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- Table: users
-- Module: User Service
-- -----------------------------------------------------
DROP TABLE IF EXISTS users;
CREATE TABLE users
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL COMMENT 'ADMIN, VENDOR, or CUSTOMER'
);

-- -----------------------------------------------------
-- Table: products
-- Module: Product Service
-- -----------------------------------------------------
DROP TABLE IF EXISTS products;
CREATE TABLE products
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description TEXT,
    price       DECIMAL(10, 2) NOT NULL,
    category    VARCHAR(100),
    image_url   VARCHAR(500),
    vendor_id   BIGINT         NOT NULL,
    FOREIGN KEY (vendor_id) REFERENCES users (id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table: inventory
-- Module: Inventory Service
-- Note: Separated from products to follow Class Diagram
-- -----------------------------------------------------
DROP TABLE IF EXISTS inventory;
CREATE TABLE inventory
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    quantity   INT    NOT NULL DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- Table: orders
-- Module: Order Service
-- -----------------------------------------------------
DROP TABLE IF EXISTS orders;
CREATE TABLE orders
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT         NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status      VARCHAR(50)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SHIPPED, DELIVERED',
    created_at  TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users (id)
);

-- -----------------------------------------------------
-- Table: order_items
-- Module: Order Service (Links Orders to Products)
-- -----------------------------------------------------
DROP TABLE IF EXISTS order_items;
CREATE TABLE order_items
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id          BIGINT         NOT NULL,
    product_id        BIGINT         NOT NULL,
    quantity          INT            NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id)
);

-- -----------------------------------------------------
-- Table: customizations
-- Module: Order Service (Specific attributes like "Red Wool")
-- -----------------------------------------------------
DROP TABLE IF EXISTS customizations;
CREATE TABLE customizations
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id   BIGINT       NOT NULL,
    attribute_name  VARCHAR(100) NOT NULL,
    attribute_value VARCHAR(100) NOT NULL,
    FOREIGN KEY (order_item_id) REFERENCES order_items (id) ON DELETE CASCADE
);

-- Re-enable foreign key checks
SET
FOREIGN_KEY_CHECKS = 1;