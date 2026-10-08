# Java Syllabus Concept Mapping — CCSE0355

| # | Syllabus Concept | Manufacturing Feature | Java Class / Interface | Explanation |
|---|---|---|---|---|
| 1 | Classes & Objects | Core business entities | `ManufacturingOrder.java`, `Product.java` | Domain representations with state and behaviors |
| 2 | Encapsulation | Guarded field mutation | All entity and DTO classes | `private` variables with validated getters and setters |
| 3 | Inheritance | Specialized user types | `User.java` ← `AdminUser.java`, `ProductionManagerUser.java` | JPA single-table inheritance tree |
| 4 | Abstraction | Business contracts | `OrderService.java`, `AbstractOrderProcessor.java` | Interface and abstract class contracts |
| 5 | Polymorphism | Scheduling algorithms | `SchedulingStrategy.java` & implementations | Subtyping allows strategy swapping at runtime |
| 6 | Interfaces | Core service interfaces | `InventoryService.java`, `SchedulingService.java` | Decoupled contracts from Hibernate implementations |
| 7 | Constructors | Explicit initialization | All domain entities | Default, parameterized, and copy constructors |
| 8 | `this` Keyword | Self-reference & chaining | `AdminUser.java`, `PriorityOrderProcessor.java` | Disambiguation and constructor chaining (`this()`) |
| 9 | `super` Keyword | Parent delegation | `AdminUser.java`, `ValidationOrderProcessor.java` | Invokes parent constructor and overridden parent methods |
| 10 | Method Overloading | Multiple signatures | `StringProcessingUtil.java`, `ProductionMonitor.java` | Compile-time polymorphism (same name, different arguments) |
| 11 | Method Overriding | Subclass behavior | Subclass implementations | Runtime polymorphism with `@Override` |
| 12 | Access Modifiers | Visibility scoping | All source files | `private`, `protected`, `public`, package-private |
| 13 | Association | Order-to-customer reference | `ManufacturingOrder.customer` | `@ManyToOne` association |
| 14 | Aggregation | Batch-to-tasks linkage | `ProductionSchedule.tasks` | Tasks grouped into schedule |
| 15 | Composition | Order items lifecycle | `ManufacturingOrder.orderItems` | Line items tied to order lifecycle (`orphanRemoval=true`) |
| 16 | Generalization | Exception hierarchy | `CustomerNotFoundException` | Specialization of `ResourceNotFoundException` |
| 17 | Realization | Interface fulfillment | `OrderServiceImpl` implements `OrderService` | Implementation fulfills interface contract |
| 18 | Lambda Expressions | Streams & comparators | `OrderServiceImpl.java`, `PrioritySchedulingStrategy.java` | Functional predicates, mappers, and custom comparators |
| 19 | 1D Arrays | Monthly production targets | `ArrayDemoService.java` | Fixed-size array for 12 monthly targets |
| 20 | 2D Arrays | Workstation × Shift matrix | `ArrayDemoService.java` | Capacity grid by workstation and shift |
| 21 | Jagged Arrays | Production line stages | `ArrayDemoService.java` | Unequal stage counts across different assembly lines |
| 22 | `ArrayList` | Batch order processing | `CollectionsDemoServiceImpl.java` | Dynamic array with O(1) indexed lookup |
| 23 | `LinkedList` | FIFO production pipeline | `CollectionsDemoServiceImpl.java` | Sequential queue with efficient insertion/removal |
| 24 | `HashSet` | Unique SKU verification | `CollectionsDemoServiceImpl.java` | O(1) membership check for product SKU uniqueness |
| 25 | `HashMap` | Rapid stock index | `CollectionsDemoServiceImpl.java` | Key-value store mapping product IDs to current inventory |
| 26 | `TreeSet` | Natural category ordering | `CollectionsDemoServiceImpl.java` | Red-black tree maintaining sorted category names |
| 27 | `PriorityQueue` | Urgent order dispatch | `SchedulingServiceImpl.java`, `PrioritySchedulingStrategy.java` | Min/max heap for DSA order prioritization |
| 28 | `Iterator` | Safe collection traversal | `CollectionsDemoServiceImpl.java` | Iteration pattern over backlog lists |
| 29 | Generics | Unified API response | `GenericResponse<T>`, `PagedResponse<T>` | Type-safe reusable data wrappers |
| 30 | Bounded Generics | Mathematical calculations | `PagedResponse.sumValues(List<? extends N>)` | Numeric wildcards (`? extends N`) |
| 31 | Enums | Type-safe domains | `OrderStatus`, `Priority`, `ProductionTaskStatus` | Explicit domain states preventing arbitrary strings |
| 32 | Custom Exceptions | Business failure modes | `InsufficientInventoryException.java`, `InvalidOrderException.java` | Domain-specific checked/unchecked exception handling |
| 33 | Centralized Exceptions | REST advice | `GlobalExceptionHandler.java` | Catches exceptions and returns uniform JSON responses |
| 34 | Try-With-Resources | Resource leak prevention | `CsvExportUtil.java`, `ProductionTcpServer.java` | Auto-closes streams and sockets |
| 35 | `String` & Immutability | Order identifiers | `StringProcessingUtil.java` | Immutable text references |
| 36 | `StringBuilder` | Single-threaded summaries | `StringProcessingUtil.java`, `AbstractOrderProcessor.java` | High-efficiency mutable text assembly |
| 37 | `StringBuffer` | Synchronized text buffer | `StringProcessingUtil.java` | Thread-safe thread-shared logging buffer |
| 38 | `StringTokenizer` | CSV parsing demo | `StringProcessingUtil.java` | Delimited string extraction |
| 39 | Localization | Multilingual UI messages | `messages.properties`, `messages_hi.properties` | ResourceBundle English and Hindi text catalogs |
| 40 | File I/O (Character) | Order and inventory export | `CsvExportUtil.java` | `BufferedWriter`, `FileWriter` |
| 41 | File I/O (Byte) | UDP network payloads | `MachineUdpSender.java`, `MachineUdpReceiver.java` | `byte[]`, `DatagramPacket` |
| 42 | Multithreading | Continuous shop-floor audit | `ProductionMonitor.java`, `ProductionSchedulerWorker.java` | `Thread`, `Runnable`, daemon threads |
| 43 | Thread Lifecycle | Thread state tracking | `ProductionMonitor.java` | Demonstrates RUNNABLE, TIMED_WAITING, TERMINATED |
| 44 | Synchronization | Concurrent safety | `ProductionMonitor.java` | `synchronized` blocks, `AtomicBoolean`, `AtomicInteger` |
| 45 | TCP Sockets | Machine command channel | `ProductionTcpServer.java`, `ProductionTcpClient.java` | Port 9090 `ServerSocket` and `Socket` communication |
| 46 | UDP Sockets | Sensor telemetry feed | `MachineUdpSender.java`, `MachineUdpReceiver.java` | Port 9091 connectionless datagram communication |
| 47 | Swing/AWT GUI | Shop-floor workstation UI | `ProductionTerminal.java` | `JFrame`, `JPanel`, `JButton`, `JTextArea` |
| 48 | GUI Layout Managers | Dynamic window sizing | `ProductionTerminal.java` | `BorderLayout`, `GridBagLayout`, `FlowLayout`, `BoxLayout` |
| 49 | GUI Event Handling | Operator click actions | `ProductionTerminal.java` | `ActionListener`, `WindowAdapter`, `MouseAdapter` |
| 50 | JVM Architecture | Technical runtime doc | `docs/java-runtime.md` | Metaspace, heap spaces, class loaders, JIT, G1GC |
