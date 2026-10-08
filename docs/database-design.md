# Database Design & Relational Schema

## Smart Manufacturing Order Processing System
**Database Engine:** MySQL 8.0 (with H2 embedded fallback)  
**Schema Model:** 3rd Normal Form (3NF) Relational Architecture  

---

## 1. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned
    CUSTOMERS ||--o{ MANUFACTURING_ORDERS : places
    PRODUCTS ||--|| INVENTORY : maintains
    PRODUCTS ||--o{ INVENTORY_TRANSACTIONS : logs
    PRODUCTS ||--o{ ORDER_ITEMS : contains
    MANUFACTURING_ORDERS ||--|{ ORDER_ITEMS : includes
    MANUFACTURING_ORDERS ||--o{ PRODUCTION_TASKS : generates
    MANUFACTURING_ORDERS ||--o{ ORDER_STATUS_HISTORY : tracks
    PRODUCTION_SCHEDULES ||--o{ PRODUCTION_TASKS : schedules
    USERS ||--o{ ORDER_STATUS_HISTORY : audited_by

    USERS {
        bigint id PK
        varchar username UK
        varchar password
        varchar full_name
        varchar email UK
        varchar department
        boolean active
        datetime created_at
    }

    CUSTOMERS {
        bigint id PK
        varchar customer_code UK
        varchar name
        varchar email UK
        varchar phone
        varchar company
        varchar address
        boolean active
    }

    PRODUCTS {
        bigint id PK
        varchar product_code UK
        varchar name
        varchar category
        decimal unit_price
        int production_duration_hours
        int min_stock_level
        boolean active
    }

    INVENTORY {
        bigint id PK
        bigint product_id FK
        int current_stock
        int allocated_stock
        int min_stock_level
    }

    MANUFACTURING_ORDERS {
        bigint id PK
        varchar order_number UK
        bigint customer_id FK
        date order_date
        date required_delivery_date
        varchar priority
        varchar status
        decimal subtotal
        decimal tax_amount
        decimal grand_total
        varchar notes
    }

    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint product_id FK
        int quantity
        decimal unit_price
        decimal subtotal
    }

    PRODUCTION_TASKS {
        bigint id PK
        varchar task_code UK
        bigint order_id FK
        bigint product_id FK
        bigint schedule_id FK
        int quantity
        varchar assigned_team
        varchar priority
        datetime planned_start_date
        datetime planned_end_date
        varchar status
        int progress_percentage
    }

    ORDER_STATUS_HISTORY {
        bigint id PK
        bigint order_id FK
        varchar previous_status
        varchar new_status
        bigint changed_by_user_id FK
        varchar comments
        datetime changed_at
    }
```

---

## 2. Table Specifications & Normalization Details

### 2.1 Table: `users` & `roles`
* Implements a standard Many-to-Many join table `user_roles` to support flexible multi-role authorizations.
* Passwords stored exclusively as BCrypt salted hash strings ($2a$10$...).

### 2.2 Table: `products` & `inventory`
* Strict **1-to-1 relationship** between `products` and `inventory`.
* `inventory.allocated_stock` keeps track of materials committed to approved orders, preventing overselling without needing destructive physical stock drops until manufacturing completes.
* Formula for Available Stock: `available_stock = current_stock - allocated_stock`.

### 2.3 Table: `manufacturing_orders` & `order_items`
* `1-to-Many` relationship with cascade deletion.
* Financial totals (`subtotal`, `tax_amount`, `grand_total`) are stored in `manufacturing_orders` with precision `DECIMAL(12, 2)` to eliminate floating-point rounding inaccuracies.

### 2.4 Table: `order_status_history`
* Append-only audit trail logging every state transition with timestamp and executing staff member ID.

---

## 3. Indexing & Query Optimization Strategy

1. **`idx_customers_name` & `idx_customers_company`**: Speeds up dynamic customer search autocomplete.
2. **`idx_products_category`**: Optimizes category-filtered catalog lookups.
3. **`idx_orders_status` & `idx_orders_priority`**: Accelerates dashboard aggregation counts and queue queries.
4. **`idx_tasks_status`**: Provides rapid lookup of active versus delayed shop floor tasks.
