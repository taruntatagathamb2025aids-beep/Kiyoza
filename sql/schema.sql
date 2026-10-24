-- Kiyoza: Smart Food Transparency System
-- Run this file once in MySQL (Workbench or `mysql -u root -p < sql/schema.sql`).
-- WARNING: it drops and recreates all Kiyoza tables.

CREATE DATABASE IF NOT EXISTS kiyoza CHARACTER SET utf8mb4;
USE kiyoza;

DROP TABLE IF EXISTS freshness_logs;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS food_ingredients;
DROP TABLE IF EXISTS ingredients;
DROP TABLE IF EXISTS food_items;
DROP TABLE IF EXISTS restaurants;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  user_id       INT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(120) NOT NULL UNIQUE,
  password_hash CHAR(64)     NOT NULL,
  role          ENUM('ADMIN','CUSTOMER') NOT NULL DEFAULT 'CUSTOMER'
);

CREATE TABLE restaurants (
  restaurant_id INT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  address       VARCHAR(200),
  phone         VARCHAR(20)
);

CREATE TABLE food_items (
  food_id       INT AUTO_INCREMENT PRIMARY KEY,
  restaurant_id INT NOT NULL,
  name          VARCHAR(100) NOT NULL,
  category      VARCHAR(50)  NOT NULL,
  price         DECIMAL(8,2) NOT NULL CHECK (price >= 0),
  prep_details  TEXT,
  is_available  BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE
);

CREATE TABLE ingredients (
  ingredient_id INT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL UNIQUE,
  allergen_type VARCHAR(50),
  supplier      VARCHAR(100)
);

-- many-to-many: a dish has many ingredients, an ingredient is used in many dishes
CREATE TABLE food_ingredients (
  food_id       INT NOT NULL,
  ingredient_id INT NOT NULL,
  PRIMARY KEY (food_id, ingredient_id),
  FOREIGN KEY (food_id)       REFERENCES food_items(food_id)       ON DELETE CASCADE,
  FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE CASCADE
);

CREATE TABLE inventory (
  ingredient_id INT PRIMARY KEY,
  quantity      DECIMAL(10,2) NOT NULL DEFAULT 0,
  unit          VARCHAR(10)   NOT NULL DEFAULT 'kg',
  expiry_date   DATE,
  last_updated  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE CASCADE
);

CREATE TABLE freshness_logs (
  log_id        INT AUTO_INCREMENT PRIMARY KEY,
  ingredient_id INT NOT NULL,
  checked_by    INT NULL,
  check_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status        ENUM('FRESH','NEAR_EXPIRY','SPOILED') NOT NULL,
  remarks       VARCHAR(200),
  FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE CASCADE,
  FOREIGN KEY (checked_by)    REFERENCES users(user_id)             ON DELETE SET NULL
);

-- ---------------------------------------------------------------- sample data
INSERT INTO users (name, email, password_hash, role) VALUES
 ('Kiyoza Admin',  'admin@kiyoza.com',    SHA2('admin123', 256),    'ADMIN'),
 ('Demo Customer', 'customer@kiyoza.com', SHA2('customer123', 256), 'CUSTOMER');

INSERT INTO restaurants (name, address, phone) VALUES
 ('Kiyoza Kitchen', '12 Beach Road, Chennai', '9876543210');

INSERT INTO ingredients (name, allergen_type, supplier) VALUES
 ('Wheat Flour',       'Gluten',    'Annapoorna Mills'),
 ('Mozzarella Cheese', 'Dairy',     'Fresh Dairy Co'),
 ('Tomato',             NULL,       'Local Farm'),
 ('Peanuts',           'Peanuts',   'Nut House'),
 ('Basil',              NULL,       'Local Farm'),
 ('Chicken',            NULL,       'Poultry Direct'),
 ('Prawns',            'Shellfish', 'Coastal Seafood'),
 ('Rice',               NULL,       'Annapoorna Mills'),
 ('Egg',               'Egg',       'Poultry Direct');

INSERT INTO food_items (restaurant_id, name, category, price, prep_details, is_available) VALUES
 (1, 'Margherita Pizza',     'Main',    250.00, 'Dough proofed for 24 hours, topped with fresh tomato sauce and baked at 280 C for 4 minutes.', TRUE),
 (1, 'Chicken Fried Rice',   'Main',    180.00, 'Wok-fried on high heat with egg and diced chicken cooked to 75 C.', TRUE),
 (1, 'Prawn Stir Fry',       'Main',    320.00, 'Prawns marinated for 15 minutes and stir-fried with tomato.', TRUE),
 (1, 'Peanut Chicken Satay', 'Starter', 220.00, 'Chicken grilled on skewers and served with house peanut sauce.', TRUE),
 (1, 'Tomato Basil Soup',    'Starter', 120.00, 'Slow-simmered for 40 minutes with fresh basil, no cream.', TRUE);

INSERT INTO food_ingredients (food_id, ingredient_id) VALUES
 (1,1),(1,2),(1,3),(1,5),
 (2,8),(2,6),(2,9),
 (3,7),(3,3),
 (4,6),(4,4),
 (5,3),(5,5);

INSERT INTO inventory (ingredient_id, quantity, unit, expiry_date) VALUES
 (1, 25,  'kg',  DATE_ADD(CURDATE(), INTERVAL 90 DAY)),
 (2, 8,   'kg',  DATE_ADD(CURDATE(), INTERVAL 6 DAY)),
 (3, 12,  'kg',  DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
 (4, 3,   'kg',  DATE_ADD(CURDATE(), INTERVAL 120 DAY)),
 (5, 1.5, 'kg',  DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
 (6, 15,  'kg',  DATE_ADD(CURDATE(), INTERVAL 2 DAY)),
 (7, 6,   'kg',  DATE_ADD(CURDATE(), INTERVAL 1 DAY)),
 (8, 40,  'kg',  DATE_ADD(CURDATE(), INTERVAL 365 DAY)),
 (9, 120, 'pcs', DATE_ADD(CURDATE(), INTERVAL 10 DAY));

-- Peanuts (4) intentionally has no check yet: the menu shows "Not yet checked".
INSERT INTO freshness_logs (ingredient_id, checked_by, status, remarks) VALUES
 (1, 1, 'FRESH',       'Sealed bag, milled this week'),
 (2, 1, 'FRESH',       'Stored at 4 C'),
 (3, 1, 'FRESH',       'Delivered this morning'),
 (5, 1, 'NEAR_EXPIRY', 'Leaves wilting, use today'),
 (6, 1, 'FRESH',       'Stored at 2 C'),
 (7, 1, 'NEAR_EXPIRY', 'Use today'),
 (8, 1, 'FRESH',       NULL),
 (9, 1, 'FRESH',       'Checked on arrival');
