# Java OOP Concepts Implementation Guide — CCSE0355

This document details the rigorous object-oriented techniques and Java concepts embedded into the Smart Manufacturing Order Processing System.

---

## Unit 1: OOP Fundamentals & Class Design

### 1. Classes and Objects
- Domain entities: `ManufacturingOrder`, `Customer`, `Product`, `Inventory`, `OrderItem`, `ProductionTask`, `ProductionSchedule`.
- Distinct object responsibilities separating business models from presentation and persistence.

### 2. Encapsulation
- All instance variables in models, services, and DTOs are declared `private`.
- State mutation is gated behind verified business logic (e.g., `Inventory.allocate()` ensures stock does not drop below zero).
- Defensive copying is used when returning collection lists (e.g. `Collections.unmodifiableList()` or `new ArrayList<>(list)`).

### 3. Inheritance & Generalization
- **JPA Single-Table Inheritance Hierarchy**:
  ```
  User (BaseEntity)
    ├── AdminUser (adminLevel, canManageUsers)
    ├── ProductionManagerUser (department, maxConcurrentTasks)
    ├── OrderStaffUser (ordersProcessedToday)
    └── InventoryCoordinatorUser (warehouseZone)
  ```
- **MappedSuperclass**: `BaseEntity` centralizes `id`, `createdAt`, and `updatedAt` audit properties.
- **Exception Inheritance**: `ResourceNotFoundException extends RuntimeException`, with specializations `OrderNotFoundException`, `CustomerNotFoundException`, `ProductNotFoundException`.

### 4. Abstraction & Interfaces
- **Interface Segregation**: `OrderService`, `InventoryService`, `ProductionService`, `SchedulingService`, `ReportService`, `CollectionsDemoService`.
- **Abstract Classes**: `AbstractOrderProcessor` encapsulates algorithm steps via the Template Method pattern.

### 5. Polymorphism
- **Subtype Polymorphism**: `User user = new AdminUser(); user.getDisplayRole();` dynamically dispatches to the subclass implementation.
- **Strategy Pattern Polymorphism**:
  ```java
  SchedulingStrategy strategy = new PrioritySchedulingStrategy();
  PriorityQueue<ManufacturingOrder> queue = strategy.schedule(orders);
  ```
- Dynamic substitution of `DeadlineSchedulingStrategy` or `BalancedSchedulingStrategy` without altering client services.

---

## Unit 2: Java Language Mechanics

### 1. `this` and `super` Keywords
- `this.propertyName = propertyName` disambiguates arguments in constructors.
- `this(7)` demonstrates explicit constructor delegation.
- `super(username, email, password)` passes core credentials to the parent `User` entity constructor.
- `super.postProcess(order, result)` invokes base logic before specialized subclass handling.

### 2. Method Overloading & Method Overriding
- **Method Overriding**: Every subclass implements `@Override` for `getDisplayRole()`, `hasPermission()`, and `validate()`.
- **Method Overloading**:
  - `StringProcessingUtil.formatCurrency(double)` vs `formatCurrency(double, String)`
  - `ProductionMonitor.getStats()` vs `getStats(boolean includeThreadInfo)`
  - `AbstractOrderProcessor.getSummary()` vs `getSummary(boolean detailed)`

### 3. Access Modifiers
- `private`: Encapsulated instance state and helper methods.
- `protected`: Template methods and logs in `AbstractOrderProcessor`.
- `public`: Service interfaces and REST controller endpoints.
- Package-private: Inner client handlers and helper components.

---

## Unit 3: Relationships & Generics

### 1. Object Relationships
- **Association**: `Customer` places `ManufacturingOrder` (loose reference via `@ManyToOne`).
- **Aggregation**: `ProductionSchedule` schedules `ProductionTask` (tasks retain identity independent of daily batch schedule).
- **Composition**: `ManufacturingOrder` owns `OrderItem` with cascade lifecycle (`CascadeType.ALL`, `orphanRemoval=true`).
- **Realization**: `OrderServiceImpl implements OrderService`.

### 2. Generics & Wildcards
- `GenericResponse<T>` provides type-safe unified API wrappers.
- Bounded type parameters: `<N extends Number> GenericResponse<N> numericSuccess(N value, String label)`.
- Upper-bounded wildcard: `<N extends Number> double sumValues(List<? extends N> values)`.

---

## Unit 4: Collections, Lambdas & I/O Streams

### 1. Comprehensive Collections Implementation
| Collection Class | File / Location | Manufacturing Purpose |
|---|---|---|
| `ArrayList<T>` | `CollectionsDemoServiceImpl` | Indexed, resizable lists of orders for report iteration |
| `LinkedList<T>` | `CollectionsDemoServiceImpl` | FIFO shop-floor production queue (efficient enqueue/dequeue) |
| `HashSet<T>` | `CollectionsDemoServiceImpl` | Fast O(1) set of unique product SKUs |
| `HashMap<K,V>` | `CollectionsDemoServiceImpl` | High-performance stock lookups (`productId -> quantity`) |
| `TreeSet<T>` | `CollectionsDemoServiceImpl` | Naturally sorted product categories |
| `PriorityQueue<T>` | `SchedulingServiceImpl` | DSA heap ordering urgent orders before normal backlog |
| `Iterator<T>` | `CollectionsDemoServiceImpl` | Safe traversal and item removal during backlog scanning |

### 2. Lambda Expressions & Streams
- Functional filtering, sorting, and transformation:
  ```java
  List<OrderResponseDto> urgentOrders = orders.stream()
      .filter(o -> o.getPriority() == Priority.URGENT)
      .sorted(Comparator.comparing(ManufacturingOrder::getRequiredDate))
      .map(this::toResponseDto)
      .collect(Collectors.toList());
  ```

### 3. File I/O & Character/Byte Streams
- **Character Streams**: `BufferedWriter`, `BufferedReader`, `PrintWriter` for CSV export and network socket commands.
- **Byte Streams**: `byte[]`, `DatagramPacket` for UDP telemetry.
- **Deterministic Cleanup**: `try-with-resources` ensures files and sockets are closed without leaks.

---

## Unit 5: Multithreading, Networking & Desktop GUI

### 1. Multithreading & Synchronization
- `ProductionMonitor`: Daemon thread executing continuous health checks (`TIMED_WAITING`).
- `ProductionSchedulerWorker`: Worker processing orders from a thread-safe `BlockingQueue`.
- `AtomicBoolean` and `AtomicInteger` guarantee thread-safe status flags and metrics.

### 2. Network Programming (TCP & UDP)
- **TCP Architecture**: `ProductionTcpServer` (ServerSocket port 9090) and `ProductionTcpClient` handle reliable shop-floor commands (`START_TASK`, `PAUSE_TASK`, `COMPLETE_TASK`).
- **UDP Architecture**: `MachineUdpSender` and `MachineUdpReceiver` (DatagramSocket port 9091) broadcast lightweight telemetry.

### 3. Swing/AWT Desktop Terminal
- `desktop-client` module implements `ProductionTerminal`:
  - `JFrame`, `JPanel`, `JButton`, `JTextArea`, `JTextField`, `JScrollPane`.
  - Layout Managers: `BorderLayout`, `GridBagLayout`, `FlowLayout`, `BoxLayout`.
  - Event Listeners: `ActionListener`, `WindowAdapter`, `MouseAdapter`.
  - Thread-safe GUI rendering via `SwingUtilities.invokeLater()`.
