# JVM Architecture & Runtime Documentation

## Overview
This document explains the Java Virtual Machine (JVM) internal architecture, runtime data areas, execution mechanics, and garbage collection behavior in the context of the **Smart Manufacturing Order Processing System** (Course: CCSE0355).

---

## 1. JVM High-Level Architecture

```
+-------------------------------------------------------------+
|                Java Source Code (.java)                     |
|  CustomerService.java, OrderServiceImpl.java, etc.          |
+-------------------------------------------------------------+
                               |
                        javac Compiler
                               |
+-------------------------------------------------------------+
|                Bytecode (.class files)                      |
|      Platform-independent intermediate representation       |
+-------------------------------------------------------------+
                               |
+-------------------------------------------------------------+
|              Java Virtual Machine (JVM)                     |
|                                                             |
|  +-------------------------------------------------------+  |
|  |             Class Loader Subsystem                    |  |
|  |    Loading  --->  Linking  --->  Initialization       |  |
|  +-------------------------------------------------------+  |
|                               |                             |
|  +-------------------------------------------------------+  |
|  |               Runtime Data Areas                      |  |
|  |  [Method Area] [Heap] [Java Stacks] [PC] [Native]     |  |
|  +-------------------------------------------------------+  |
|                               |                             |
|  +-------------------------------------------------------+  |
|  |                Execution Engine                       |  |
|  |  Interpreter | JIT Compiler | Garbage Collector (G1)  |  |
|  +-------------------------------------------------------+  |
|                               |                             |
|  +-------------------------------------------------------+  |
|  |          Java Native Interface (JNI) & Libs           |  |
|  +-------------------------------------------------------+  |
+-------------------------------------------------------------+
                               |
                   Host Operating System (OS)
```

---

## 2. Class Loader Subsystem

The Class Loader is responsible for dynamically loading compiled `.class` files into JVM memory at runtime. It follows three key phases:

### A. Loading
Uses the **Delegation Hierarchy Principle**:
1. **Bootstrap ClassLoader**: Loads core Java platform classes (`java.lang.*`, `java.util.*`, `java.io.*`) from JDK runtime image (`jmods`).
2. **Platform / Extension ClassLoader**: Loads platform modular extensions.
3. **Application (System) ClassLoader**: Loads application classes from classpath (all `com.smart.manufacturing.*` classes and external Spring Boot JAR dependencies).

### B. Linking
1. **Verification**: Ensures bytecode conforms to JVM specification and is safe (no stack corruption or illegal casts).
2. **Preparation**: Allocates memory for static variables and initializes them to default values (e.g. `DEFAULT_PORT = 0`).
3. **Resolution**: Replaces symbolic memory references with direct references.

### C. Initialization
Executes static initializers and assigns explicit static values (e.g. `DEFAULT_PORT = 9090` in `ProductionTcpServer`, enum constant creation in `OrderStatus`).

---

## 3. Runtime Data Areas

| Memory Area | Thread Scope | Contents | Manufacturing System Example |
|---|---|---|---|
| **Method Area (Metaspace)** | Shared across all threads | Class metadata, method bytecodes, runtime constant pool, static variables | `OrderStatus` constants, Spring bean definitions, Hibernate entity mappings |
| **Heap Memory** | Shared across all threads | All object instances and arrays | `ManufacturingOrder`, `Customer`, `Product`, `PriorityQueue` objects |
| **Java Thread Stack** | Private per thread | Stack frames for each active method invocation (local variables, operand stack, frame data) | `orderService.createOrder()` method frame with local calculation variables |
| **Program Counter (PC) Register** | Private per thread | Address of the JVM instruction currently being executed | Instruction pointer for `ProductionMonitor` background thread |
| **Native Method Stack** | Private per thread | State of native C/C++ method invocations | Native socket I/O calls executed by MySQL JDBC driver |

---

## 4. JVM Heap Memory & Garbage Collection (GC)

### Heap Partitioning (Generational Hypothesis)
1. **Young Generation**:
   - **Eden Space**: Where new objects are initially allocated (e.g. short-lived `OrderItemDto`, string conversions).
   - **Survivor Spaces (S0 / S1)**: Objects that survive minor GC cycles are copied between S0 and S1 with an aging counter.
2. **Old / Tenured Generation**:
   - Long-lived objects that reached the tenuring threshold (e.g. Spring `@Service` singletons, JPA `EntityManagerFactory`, cached reference data).
3. **Metaspace (Off-Heap)**:
   - Stores class metadata in native memory, growing dynamically up to system limits.

### Garbage Collector Choice: G1GC
In production and containerized environments, the application uses **G1GC** (Garbage-First Garbage Collector, standard default in Java 17+):
```bash
java -Xms256m -Xmx512m -XX:+UseG1GC -jar app.jar
```
- Divides the heap into equal-sized regions (1MB to 32MB).
- Selects regions with the most garbage first ("Garbage First") to achieve predictable pause time targets.

---

## 5. Object Lifecycle & GC Eligibility

An object on the heap becomes eligible for Garbage Collection when it is no longer reachable from any **GC Root** (active thread stack references, static fields, JNI references).

```java
// Example from OrderServiceImpl:
public void notifyStaff(Long orderId) {
    OrderSummaryDto summary = new OrderSummaryDto(...); // Allocated in Eden
    emailService.send(summary);
} // Method returns: 'summary' is no longer reachable from this thread stack.
  // It is immediately eligible for minor GC collection.
```

---

## 6. Why `finalize()` is NOT Used (Academic Context)

| Reason | Explanation |
|---|---|
| **Deprecation** | Deprecated in Java 9, marked for removal in Java 18+. |
| **Unpredictability** | The JVM makes no guarantee when (or even if) `finalize()` will be executed. |
| **Performance Overhead** | Finalizable objects require at least two GC cycles to be reclaimed, increasing heap pressure. |
| **Security Risk** | An exception in `finalize()` can leave partially initialized objects open to subclass resurrection exploits. |

### The Modern Replacement: `AutoCloseable` & Try-With-Resources
Instead of `finalize()`, modern Java applications use deterministic resource cleanup via `try-with-resources`:
```java
// CsvExportUtil & ProductionTcpServer
try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
    writer.write(data);
} // Guaranteed automatic invocation of close() upon block exit
```

---

## 7. Execution Engine: Interpreter & JIT Compiler

1. **Interpreter**: Directly interprets bytecode instruction-by-instruction for fast startup.
2. **JIT (Just-In-Time) Compiler**:
   - Identifies "hot spots" (frequently executed loops and methods, such as `PriorityQueue` comparator checks and inventory quantity deductions).
   - Compiles bytecode into optimized machine code directly executed by the host CPU.
   - Utilizes C1 (client) and C2 (server) compilation tiers with method inlining and loop unrolling.
