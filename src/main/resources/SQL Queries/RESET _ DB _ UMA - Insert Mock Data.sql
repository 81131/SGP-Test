
-- 1. DROP CONSTRAINTS & EXISTING OBJECTS

-- Drop foreign key constraints (Order matters!)
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_stock_movements_batch')
    ALTER TABLE stock_movements DROP CONSTRAINT FK_stock_movements_batch;
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_stock_movements_user')
    ALTER TABLE stock_movements DROP CONSTRAINT FK_stock_movements_user;
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_stock_batches_purchase')
    ALTER TABLE stock_batches DROP CONSTRAINT FK_stock_batches_purchase;
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_stock_batches_compartment')
    ALTER TABLE stock_batches DROP CONSTRAINT FK_stock_batches_compartment;
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_purchase_orders_supplier')
    ALTER TABLE purchase_orders DROP CONSTRAINT FK_purchase_orders_supplier;
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_purchase_orders_product')
    ALTER TABLE purchase_orders DROP CONSTRAINT FK_purchase_orders_product;
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_compartments_shelf')
    ALTER TABLE compartments DROP CONSTRAINT FK_compartments_shelf;
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_shelves_aisle')
    ALTER TABLE shelves DROP CONSTRAINT FK_shelves_aisle;
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_users_roles')
    ALTER TABLE users DROP CONSTRAINT FK_users_roles;
GO

-- Drop tables
DROP TABLE IF EXISTS stock_movements;
DROP TABLE IF EXISTS stock_batches;
DROP TABLE IF EXISTS purchase_orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS compartments;
DROP TABLE IF EXISTS shelves;
DROP TABLE IF EXISTS aisles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS income;
DROP TABLE IF EXISTS manual_expenses;
GO

-- Drop stored procedure if exists
DROP PROCEDURE IF EXISTS sp_CreateUser;
GO


-- 2. CREATE TABLES

CREATE TABLE roles (
    id INT PRIMARY KEY IDENTITY(1,1),
    name VARCHAR(255) NOT NULL UNIQUE
);
GO

CREATE TABLE users (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    enabled BIT NOT NULL DEFAULT 1,
    role_id INT NOT NULL,
    company_name VARCHAR(255) NULL, -- For Suppliers
    phone_number VARCHAR(50) NULL,   -- Optional
    email VARCHAR(255) NULL,         -- Optional
    CONSTRAINT FK_users_roles FOREIGN KEY (role_id) REFERENCES roles(id)
);
GO

CREATE TABLE aisles (
    id INT PRIMARY KEY IDENTITY(1,1),
    name VARCHAR(50) NOT NULL UNIQUE
);
GO

CREATE TABLE shelves (
    id INT PRIMARY KEY IDENTITY(1,1),
    aisle_id INT NOT NULL,
    CONSTRAINT FK_shelves_aisle FOREIGN KEY (aisle_id) REFERENCES aisles(id)
);
GO

CREATE TABLE compartments (
    id INT PRIMARY KEY IDENTITY(1,1),
    shelf_id INT NOT NULL,
    level VARCHAR(50) NOT NULL,
    capacity_volume DECIMAL(10, 2) NULL,
    CONSTRAINT FK_compartments_shelf FOREIGN KEY (shelf_id) REFERENCES shelves(id)
);
GO

CREATE TABLE products (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(MAX) NULL,
    unit_of_measure VARCHAR(50) NOT NULL
);
GO

CREATE TABLE purchase_orders (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    supplier_user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    total_quantity_purchased INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    purchase_date DATE NOT NULL,
    CONSTRAINT FK_purchase_orders_supplier FOREIGN KEY (supplier_user_id) REFERENCES users(id),
    CONSTRAINT FK_purchase_orders_product FOREIGN KEY (product_id) REFERENCES products(id)
);
GO

CREATE TABLE stock_batches (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    purchase_order_id BIGINT NOT NULL,
    compartment_id INT NOT NULL,
    quantity INT NOT NULL,
    expiry_date DATE NULL,
    CONSTRAINT FK_stock_batches_purchase FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT FK_stock_batches_compartment FOREIGN KEY (compartment_id) REFERENCES compartments(id)
);
GO

CREATE TABLE stock_movements (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    stock_batch_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    movement_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    movement_date DATETIME NOT NULL DEFAULT GETDATE(),
    notes VARCHAR(MAX) NULL,
    CONSTRAINT FK_stock_movements_batch FOREIGN KEY (stock_batch_id) REFERENCES stock_batches(id),
    CONSTRAINT FK_stock_movements_user FOREIGN KEY (user_id) REFERENCES users(id)
);
GO

CREATE TABLE income (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    amount DECIMAL(10, 2) NOT NULL,
    income_type VARCHAR(255),
    income_date DATE NOT NULL
);
GO

CREATE TABLE manual_expenses (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    description VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    category VARCHAR(255)
);
GO


-- 3. CREATE STORED PROCEDURE

CREATE PROCEDURE sp_CreateUser
    @username VARCHAR(255),
    @password VARCHAR(255),
    @first_name VARCHAR(255),
    @last_name VARCHAR(255),
    @role_name VARCHAR(255),
    @company_name VARCHAR(255) = NULL,
    @phone_number VARCHAR(50) = NULL,
    @email VARCHAR(255) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @role_id INT;
    SELECT @role_id = id FROM roles WHERE name = @role_name;

    IF @role_id IS NOT NULL
    BEGIN
        INSERT INTO users (username, password, first_name, last_name, enabled, role_id, company_name, phone_number, email)
        VALUES (@username, @password, @first_name, @last_name, 1, @role_id, @company_name, @phone_number, @email);
    END
    ELSE
    BEGIN
        PRINT 'Error: Role not found - ' + @role_name;
    END
END
GO


-- 4. INSERT MOCK DATA


-- Roles
INSERT INTO roles (name) VALUES
('BUSINESS_OWNER'), ('INVENTORY_MANAGER'), ('SALES_STAFF'),
('WAREHOUSE_STAFF'), ('SYSTEM_ADMINISTRATOR'), ('SUPPLIER');
GO

-- Users
EXEC sp_CreateUser 'owner', 'owner123', 'Renuka', 'Wijesinghe', 'BUSINESS_OWNER';
EXEC sp_CreateUser 'admin', 'admin123', 'Ramesh', 'Jayawardena', 'SYSTEM_ADMINISTRATOR', NULL, '0112233441', 'ramesh.j@sgd.lk';
EXEC sp_CreateUser 'manager', 'manager123', 'Nadeesha', 'Kumari', 'INVENTORY_MANAGER', NULL, '0112233442', 'nadeesha.k@sgd.lk';
EXEC sp_CreateUser 'sales', 'sales123', 'Tharushi', 'Senanayake', 'SALES_STAFF', NULL, '0112233443', 'tharushi.s@sgd.lk';
EXEC sp_CreateUser 'warehouse', 'warehouse123', 'Sunil', 'Rathnayake', 'WAREHOUSE_STAFF', NULL, '0112233444', NULL;
EXEC sp_CreateUser 'supplier_mahesh', 'mahesh123', 'Mahesh', 'Bandara', 'SUPPLIER', 'Bandara Goods', '0771122334', 'mahesh.b@suppliers.lk';
EXEC sp_CreateUser 'supplier_foods', 'foods123', 'Priya', 'Perera', 'SUPPLIER', 'Quality Foods Ltd', '0719988776', 'priya.p@qualityfoods.lk';
GO

-- Warehouse Structure
INSERT INTO aisles (name) VALUES ('A'), ('B'), ('C');
INSERT INTO shelves (aisle_id) VALUES (1), (1), (2), (3), (3); -- 2 shelves in A, 1 in B, 2 in C
INSERT INTO compartments (shelf_id, level, capacity_volume) VALUES
(1, 'Top', 2.0), (1, 'Bottom', 3.0),     -- Shelf 1 in Aisle A -> Compartments 1, 2
(2, 'Single', 5.0),                      -- Shelf 2 in Aisle A -> Compartment 3
(3, 'Top', 1.5), (3, 'Middle', 1.5), (3, 'Bottom', 1.5), -- Shelf 3 in Aisle B -> Compartments 4, 5, 6
(4, 'Level 1', 4.0), (4, 'Level 2', 4.0), -- Shelf 4 in Aisle C -> Compartments 7, 8
(5, 'Top', 2.5), (5, 'Bottom', 2.5);      -- Shelf 5 in Aisle C -> Compartments 9, 10
GO

-- Products
INSERT INTO products (name, description, unit_of_measure) VALUES
('Sunlight Soap 100g', 'Standard bar soap', 'units'), -- ID 1
('Basmati Rice 5kg Bag', 'Premium long grain rice', 'bags'), -- ID 2
('Red Lentils (Dhal) 1kg', 'Dried red lentils', 'kg'), -- ID 3
('Anchor Milk Powder 400g', 'Full cream milk powder', 'units'); -- ID 4
GO

-- Purchase Orders
DECLARE @supplierMaheshId BIGINT = (SELECT id FROM users WHERE username = 'supplier_mahesh');
DECLARE @supplierFoodsId BIGINT = (SELECT id FROM users WHERE username = 'supplier_foods');
DECLARE @soapId BIGINT = (SELECT id FROM products WHERE name = 'Sunlight Soap 100g');
DECLARE @riceId BIGINT = (SELECT id FROM products WHERE name = 'Basmati Rice 5kg Bag');
DECLARE @dhalId BIGINT = (SELECT id FROM products WHERE name = 'Red Lentils (Dhal) 1kg');
DECLARE @milkId BIGINT = (SELECT id FROM products WHERE name = 'Anchor Milk Powder 400g');

INSERT INTO purchase_orders (supplier_user_id, product_id, total_quantity_purchased, unit_price, purchase_date) VALUES
(@supplierMaheshId, @soapId, 200, 0.50, '2025-10-01'), -- PO 1
(@supplierFoodsId, @riceId, 100, 10.00, '2025-10-02'), -- PO 2
(@supplierFoodsId, @dhalId, 300, 1.50, '2025-10-03'),  -- PO 3
(@supplierMaheshId, @milkId, 50, 4.00, '2025-10-05'),  -- PO 4
(@supplierMaheshId, @soapId, 150, 0.55, '2025-10-15'); -- PO 5 (different price)
GO

-- Stock Batches
DECLARE @po1 BIGINT = (SELECT id FROM purchase_orders WHERE product_id = (SELECT id FROM products WHERE name = 'Sunlight Soap 100g') AND purchase_date = '2025-10-01');
DECLARE @po2 BIGINT = (SELECT id FROM purchase_orders WHERE product_id = (SELECT id FROM products WHERE name = 'Basmati Rice 5kg Bag') AND purchase_date = '2025-10-02');
DECLARE @po3 BIGINT = (SELECT id FROM purchase_orders WHERE product_id = (SELECT id FROM products WHERE name = 'Red Lentils (Dhal) 1kg') AND purchase_date = '2025-10-03');
DECLARE @po4 BIGINT = (SELECT id FROM purchase_orders WHERE product_id = (SELECT id FROM products WHERE name = 'Anchor Milk Powder 400g') AND purchase_date = '2025-10-05');
DECLARE @po5 BIGINT = (SELECT id FROM purchase_orders WHERE product_id = (SELECT id FROM products WHERE name = 'Sunlight Soap 100g') AND purchase_date = '2025-10-15');

INSERT INTO stock_batches (purchase_order_id, compartment_id, quantity, expiry_date) VALUES
(@po1, 1, 100, '2026-10-01'), -- Batch 1: 100 soap in Compartment 1 (A/1/Top)
(@po1, 2, 100, '2026-10-01'); -- Batch 2: 100 soap in Compartment 2 (A/1/Bottom)
INSERT INTO stock_batches (purchase_order_id, compartment_id, quantity, expiry_date) VALUES
(@po2, 7, 100, '2027-04-01'); -- Batch 3: 100 rice in Compartment 7 (C/4/L1)
INSERT INTO stock_batches (purchase_order_id, compartment_id, quantity) VALUES
(@po3, 8, 300); -- Batch 4: 300 dhal in Compartment 8 (C/4/L2)
INSERT INTO stock_batches (purchase_order_id, compartment_id, quantity, expiry_date) VALUES
(@po4, 4, 50, '2026-12-01'); -- Batch 5: 50 milk in Compartment 4 (B/3/Top)
INSERT INTO stock_batches (purchase_order_id, compartment_id, quantity, expiry_date) VALUES
(@po5, 3, 150, '2026-11-01'); -- Batch 6: 150 soap in Compartment 3 (A/2/Single)
GO

-- Stock Movements
-- Declare user and batch IDs *within this batch*
DECLARE @salesUserId BIGINT = (SELECT id FROM users WHERE username = 'sales');
DECLARE @warehouseUserId BIGINT = (SELECT id FROM users WHERE username = 'warehouse');
-- Assuming Batch IDs start from 1 and are sequential as inserted above
DECLARE @batch1 BIGINT = 1;
DECLARE @batch2 BIGINT = 2;
DECLARE @batch3 BIGINT = 3;
DECLARE @batch4 BIGINT = 4;
DECLARE @batch5 BIGINT = 5;
DECLARE @batch6 BIGINT = 6;

INSERT INTO stock_movements (stock_batch_id, user_id, movement_type, quantity, movement_date, notes) VALUES
(@batch1, @salesUserId, 'SALE', -10, '2025-10-10 09:30:00', 'Customer Sale');
UPDATE stock_batches SET quantity = quantity - 10 WHERE id = @batch1;

INSERT INTO stock_movements (stock_batch_id, user_id, movement_type, quantity, movement_date, notes) VALUES
(@batch3, @salesUserId, 'SALE', -5, '2025-10-11 11:00:00', 'Customer Sale');
UPDATE stock_batches SET quantity = quantity - 5 WHERE id = @batch3;

INSERT INTO stock_movements (stock_batch_id, user_id, movement_type, quantity, movement_date, notes) VALUES
(@batch4, @salesUserId, 'SALE', -20, '2025-10-11 14:15:00', 'Customer Sale');
UPDATE stock_batches SET quantity = quantity - 20 WHERE id = @batch4;

INSERT INTO stock_movements (stock_batch_id, user_id, movement_type, quantity, movement_date, notes) VALUES
(@batch5, @salesUserId, 'SALE', -5, '2025-10-12 10:00:00', 'Customer Sale');
UPDATE stock_batches SET quantity = quantity - 5 WHERE id = @batch5;

INSERT INTO stock_movements (stock_batch_id, user_id, movement_type, quantity, movement_date, notes) VALUES
(@batch2, @warehouseUserId, 'ADJUSTMENT_OUT', -2, '2025-10-13 08:00:00', 'Damaged Stock');
UPDATE stock_batches SET quantity = quantity - 2 WHERE id = @batch2;

INSERT INTO stock_movements (stock_batch_id, user_id, movement_type, quantity, movement_date, notes) VALUES
(@batch6, @salesUserId, 'SALE', -15, '2025-10-16 16:30:00', 'Customer Sale');
UPDATE stock_batches SET quantity = quantity - 15 WHERE id = @batch6;
GO

-- Income Records
INSERT INTO income (amount, income_type, income_date) VALUES
(7.50, 'Sale', '2025-10-10'),
(60.00, 'Sale', '2025-10-11'),
(40.00, 'Sale', '2025-10-11'),
(25.00, 'Sale', '2025-10-12'),
(12.00, 'Sale', '2025-10-16');
GO

-- Manual Expenses
INSERT INTO manual_expenses (description, amount, expense_date, category) VALUES
('October Office Rent', 500.00, '2025-10-05', 'Rent'),
('Electricity Bill', 150.00, '2025-10-10', 'Utilities'),
('Staff Transport Oct W1', 80.00, '2025-10-08', 'Transport'),
('Staff Transport Oct W2', 85.00, '2025-10-15', 'Transport');
GO


-- 5. VERIFY DATA

PRINT '--- ROLES ---';
SELECT * FROM roles ORDER BY id;
PRINT '';
PRINT '--- USERS ---';
SELECT u.id, u.username, u.first_name, u.last_name, r.name as role_name, u.company_name, u.phone_number, u.email
FROM users u JOIN roles r ON u.role_id = r.id ORDER BY u.id;
PRINT '';
PRINT '--- WAREHOUSE STRUCTURE ---';
SELECT c.id as comp_id, a.name as aisle, s.id as shelf, c.level
FROM compartments c JOIN shelves s ON c.shelf_id = s.id JOIN aisles a ON s.aisle_id = a.id ORDER BY a.name, s.id, c.id;
PRINT '';
PRINT '--- PRODUCTS ---';
SELECT * FROM products ORDER BY id;
PRINT '';
PRINT '--- PURCHASE ORDERS ---';
SELECT po.id as po_id, p.name as product, u.username as supplier, po.total_quantity_purchased, po.unit_price, po.purchase_date
FROM purchase_orders po JOIN products p ON po.product_id = p.id JOIN users u ON po.supplier_user_id = u.id ORDER BY po.purchase_date, po.id;
PRINT '';
PRINT '--- CURRENT STOCK BATCHES (Quantity > 0) ---';
SELECT sb.id as batch_id, sb.purchase_order_id as po_id, p.name as product, c.id as comp_id, a.name as aisle, s.id as shelf, c.level, sb.quantity, sb.expiry_date
FROM stock_batches sb JOIN purchase_orders po ON sb.purchase_order_id = po.id JOIN products p ON po.product_id = p.id JOIN compartments c ON sb.compartment_id = c.id JOIN shelves s ON c.shelf_id = s.id JOIN aisles a ON s.aisle_id = a.id
WHERE sb.quantity > 0 ORDER BY sb.id;
PRINT '';
PRINT '--- STOCK MOVEMENTS ---';

SELECT sm.id as move_id, sb.id as batch_id, p.name as product, sm.quantity, sm.movement_type, u.username as performed_by_user, sm.movement_date
FROM stock_movements sm JOIN stock_batches sb ON sm.stock_batch_id = sb.id JOIN purchase_orders po ON sb.purchase_order_id = po.id JOIN products p ON po.product_id = p.id LEFT JOIN users u ON sm.user_id = u.id ORDER BY sm.movement_date, sm.id;
PRINT '';
PRINT '--- INCOME ---';
SELECT * FROM income ORDER BY income_date;
PRINT '';
PRINT '--- MANUAL EXPENSES ---';
SELECT * FROM manual_expenses ORDER BY expense_date;
GO