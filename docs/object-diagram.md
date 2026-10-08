# UML Object Diagram — Runtime Instance Example

This document provides a concrete runtime snapshot of objects in heap memory during the lifecycle of an urgent manufacturing order.

---

## 1. Runtime Object Graph

```mermaid
classDiagram
    class `customer1 : Customer` {
        id = 1
        name = "Apex Industrial Machinery"
        email = "orders@apexmachinery.com"
        company = "Apex Corp"
    }

    class `order1 : ManufacturingOrder` {
        id = 25
        orderNumber = "ORD-2026-025"
        status = IN_PROGRESS
        priority = URGENT
        requiredDate = 2026-10-18
        totalAmount = 245000.00
    }

    class `item1 : OrderItem` {
        id = 42
        quantity = 20
        unitPrice = 12250.00
        subtotal = 245000.00
    }

    class `prod1 : Product` {
        id = 7
        sku = "ROB-ARM-007"
        name = "6-Axis Robotic Arm Assembly"
        category = "Robotics"
        unitPrice = 12250.00
    }

    class `inv1 : Inventory` {
        id = 7
        currentStock = 50
        allocatedStock = 20
        availableStock = 30
        minStockLevel = 10
    }

    class `task1 : ProductionTask` {
        id = 31
        taskCode = "TASK-ORD-025-1"
        status = IN_PROGRESS
        progressPercentage = 75
        assignedWorker = "Assembly Team Gamma"
    }

    class `history1 : OrderStatusHistory` {
        id = 104
        fromStatus = SCHEDULED
        toStatus = IN_PROGRESS
        remarks = "Started assembly line stage"
        changedBy = "manager"
    }

    class `user1 : AdminUser` {
        id = 1
        username = "admin"
        adminLevel = 1
        canManageUsers = true
    }

    `customer1 : Customer` --> `order1 : ManufacturingOrder` : placed
    `order1 : ManufacturingOrder` --> `item1 : OrderItem` : contains
    `item1 : OrderItem` --> `prod1 : Product` : productRef
    `prod1 : Product` --> `inv1 : Inventory` : stockRef
    `order1 : ManufacturingOrder` --> `task1 : ProductionTask` : activeTask
    `order1 : ManufacturingOrder` --> `history1 : OrderStatusHistory` : statusAudit
    `user1 : AdminUser` --> `order1 : ManufacturingOrder` : supervisedBy
```

---

## 2. PriorityQueue Heap State (DSA Demonstration)

When the production scheduler evaluates the active backlog via `PriorityQueue<ManufacturingOrder>` with `OrderSchedulingComparator`:

```
                    [ Root / Peek ]
                 ORD-2026-025 (URGENT)
                 Deadline: 2026-10-18
                      /        \
                     /          \
           ORD-2026-012        ORD-2026-004
             (URGENT)             (HIGH)
        Deadline: 2026-10-22   Deadline: 2026-10-14
```

### Dequeue Sequence:
1. **ORD-2026-025**: Priority weight = 4 (URGENT), Deadline: 2026-10-18 (processed 1st)
2. **ORD-2026-012**: Priority weight = 4 (URGENT), Deadline: 2026-10-22 (processed 2nd)
3. **ORD-2026-004**: Priority weight = 3 (HIGH), Deadline: 2026-10-14 (processed 3rd)
