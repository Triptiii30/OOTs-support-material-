# REST API Specification

## Smart Manufacturing Order Processing System

Base URL: `http://localhost:8080/api`  
Data Format: `application/json`  
Authentication: HTTP Basic or Form Session Authentication  

---

## 1. Response Envelope Format

All REST responses adhere to a consistent generic envelope:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-10-08T19:00:00"
}
```

---

## 2. Customer Endpoints

### `GET /api/customers`
* **Query Parameters:** `query` (optional string search)
* **Response (200 OK):**
```json
{
  "success": true,
  "message": "Customers retrieved successfully",
  "data": [
    {
      "id": 1,
      "customerCode": "CUST-1001",
      "name": "Acme Industrial Corp",
      "company": "Acme Industrial Ltd",
      "email": "purchasing@acmeind.com",
      "phone": "+91 98200 11221",
      "active": true
    }
  ]
}
```

### `POST /api/customers`
* **Roles:** `ROLE_ADMIN`, `ROLE_ORDER_STAFF`
* **Payload:**
```json
{
  "customerCode": "CUST-1050",
  "name": "Alpha Manufacturing",
  "email": "procurement@alphamfg.com",
  "phone": "+91 98111 22233",
  "company": "Alpha Group Ltd",
  "address": "Electronic City, Bengaluru",
  "active": true
}
```

---

## 3. Product Endpoints

### `GET /api/products`
* **Query Parameters:** `query` (string), `category` (string)
* **Response (200 OK):** Returns array of products with real-time stock levels.

### `POST /api/products`
* **Roles:** `ROLE_ADMIN`, `ROLE_INVENTORY_COORDINATOR`
* **Payload:**
```json
{
  "productCode": "PRD-R05",
  "name": "Cartesian Gantry Robot",
  "description": "3-axis precision gantry unit",
  "category": "Robotics",
  "unitPrice": 12500.00,
  "productionDurationHours": 16,
  "minStockLevel": 5,
  "initialStock": 10,
  "active": true
}
```

---

## 4. Order Management Endpoints

### `GET /api/orders`
* **Query Parameters:** `status`, `priority`, `query`, `startDate`, `endDate`
* **Response (200 OK):** Returns filtered list of orders with line item subtotals, tax (8%), and grand totals.

### `POST /api/orders`
* **Roles:** `ROLE_ADMIN`, `ROLE_ORDER_STAFF`
* **Payload:**
```json
{
  "customerId": 1,
  "requiredDeliveryDate": "2026-10-25",
  "priority": "HIGH",
  "notes": "Expedited delivery for production line upgrade",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 5
    }
  ]
}
```

### Order Lifecycle Action Endpoints
* `POST /api/orders/{id}/validate`: Validates customer, active products, and preliminary stock feasibility.
* `POST /api/orders/{id}/approve`: Approves order and reserves inventory via `allocateStock`.
* `POST /api/orders/{id}/schedule`: Triggers scheduling service, instantiating production tasks.
* `POST /api/orders/{id}/start-production`: Commences physical manufacturing.
* `POST /api/orders/{id}/complete`: Fulfills reserved stock and marks finished.
* `DELETE /api/orders/{id}`: Cancels order and releases reserved stock.

---

## 5. Production & Scheduling Endpoints

### `GET /api/production/queue`
* Returns the in-memory **DSA PriorityQueue** ordered list evaluated via `OrderSchedulingComparator` (Urgent &gt; High &gt; Normal &gt; Low with deadline tie-breaking).

### `PUT /api/production/tasks/{id}`
* **Roles:** `ROLE_ADMIN`, `ROLE_PRODUCTION_MANAGER`
* **Query Parameters:** `progress` (int 0-100), `status` (`ProductionTaskStatus`), `notes` (string)

---

## 6. Reports & File Export Endpoints

* `GET /api/reports/dashboard`: Returns aggregate dashboard telemetry.
* `GET /api/reports/orders/csv`: Streams CSV file of orders.
* `GET /api/reports/inventory/csv`: Streams CSV file of inventory ledger.
* `GET /api/reports/production/csv`: Streams CSV file of production tasks.
