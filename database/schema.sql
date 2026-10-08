-- ===================================================================
-- Smart Manufacturing Order Processing System
-- MySQL 8.0 Normalized Relational Schema
-- Course: Object Oriented Techniques using Java (CCSE0355)
-- Capstone PBL Project - Team 91
-- ===================================================================

CREATE DATABASE IF NOT EXISTS `smart_manufacturing_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `smart_manufacturing_db`;

-- Drop tables in reverse foreign-key order
DROP TABLE IF EXISTS `order_status_history`;
DROP TABLE IF EXISTS `production_tasks`;
DROP TABLE IF EXISTS `production_schedules`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `manufacturing_orders`;
DROP TABLE IF EXISTS `inventory_transactions`;
DROP TABLE IF EXISTS `inventory`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `customers`;
DROP TABLE IF EXISTS `user_roles`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `users`;

-- 1. Users Table
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `department` VARCHAR(100) NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Roles Table
CREATE TABLE `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(200) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. User Roles Join Table (Many-to-Many)
CREATE TABLE `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Customers Table
CREATE TABLE `customers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `customer_code` VARCHAR(30) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone` VARCHAR(30) NULL,
    `company` VARCHAR(100) NULL,
    `address` VARCHAR(255) NULL,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_customers_name` (`name`),
    INDEX `idx_customers_company` (`company`)
) ENGINE=InnoDB;

-- 5. Products Table
CREATE TABLE `products` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_code` VARCHAR(50) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `description` VARCHAR(500) NULL,
    `category` VARCHAR(80) NOT NULL,
    `unit_price` DECIMAL(12,2) NOT NULL,
    `production_duration_hours` INT NOT NULL DEFAULT 8,
    `min_stock_level` INT NOT NULL DEFAULT 10,
    `active` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_products_category` (`category`)
) ENGINE=InnoDB;

-- 6. Inventory Table (1-to-1 with Product)
CREATE TABLE `inventory` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_id` BIGINT NOT NULL UNIQUE,
    `current_stock` INT NOT NULL DEFAULT 0,
    `allocated_stock` INT NOT NULL DEFAULT 0,
    `min_stock_level` INT NOT NULL DEFAULT 10,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inventory_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Inventory Transactions Table (Audit Trail)
CREATE TABLE `inventory_transactions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `product_id` BIGINT NOT NULL,
    `transaction_type` VARCHAR(30) NOT NULL,
    `quantity` INT NOT NULL,
    `stock_after` INT NOT NULL,
    `reference_order_id` BIGINT NULL,
    `notes` VARCHAR(255) NULL,
    `performed_by` VARCHAR(100) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inv_tx_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Manufacturing Orders Table (Aggregate Root)
CREATE TABLE `manufacturing_orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_number` VARCHAR(50) NOT NULL UNIQUE,
    `customer_id` BIGINT NOT NULL,
    `order_date` DATE NOT NULL,
    `required_delivery_date` DATE NOT NULL,
    `priority` VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    `status` VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `tax_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `grand_total` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `notes` VARCHAR(500) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_orders_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
    INDEX `idx_orders_status` (`status`),
    INDEX `idx_orders_priority` (`priority`),
    INDEX `idx_orders_date` (`order_date`)
) ENGINE=InnoDB;

-- 9. Order Items Table
CREATE TABLE `order_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(12,2) NOT NULL,
    `subtotal` DECIMAL(12,2) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `manufacturing_orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB;

-- 10. Production Schedules Table
CREATE TABLE `production_schedules` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `schedule_code` VARCHAR(50) NOT NULL UNIQUE,
    `schedule_date` DATE NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    `notes` VARCHAR(500) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 11. Production Tasks Table
CREATE TABLE `production_tasks` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_code` VARCHAR(50) NOT NULL UNIQUE,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `schedule_id` BIGINT NULL,
    `quantity` INT NOT NULL,
    `assigned_team` VARCHAR(100) NOT NULL,
    `priority` VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    `planned_start_date` DATETIME NOT NULL,
    `planned_end_date` DATETIME NOT NULL,
    `actual_completion_date` DATETIME NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `progress_percentage` INT NOT NULL DEFAULT 0,
    `notes` VARCHAR(500) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_tasks_order` FOREIGN KEY (`order_id`) REFERENCES `manufacturing_orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_tasks_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
    CONSTRAINT `fk_tasks_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `production_schedules` (`id`) ON DELETE SET NULL,
    INDEX `idx_tasks_status` (`status`)
) ENGINE=InnoDB;

-- 12. Order Status History Table (Audit Log)
CREATE TABLE `order_status_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `previous_status` VARCHAR(30) NULL,
    `new_status` VARCHAR(30) NOT NULL,
    `changed_by_user_id` BIGINT NULL,
    `comments` VARCHAR(500) NULL,
    `changed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_history_order` FOREIGN KEY (`order_id`) REFERENCES `manufacturing_orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_history_user` FOREIGN KEY (`changed_by_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB;
