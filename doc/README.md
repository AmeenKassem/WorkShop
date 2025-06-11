# README – Market System 

## 🧾 Project Overview
This project implements a scalable, modular e-commerce platform designed for the Software Engineering Workshop at Ben-Gurion University. The system provides a complete marketplace environment supporting both buyers and sellers with advanced features such as role management, flexible purchasing flows, real-time notifications, and enforceable business policies.

The project is structured into well-defined architectural layers to support extensibility and maintainability, and it meets the functional and non-functional requirements outlined in versions 0, 1, 2, and 3 of the specification.

---

## 🌐 Core Capabilities
- Multi-role user support: guest, subscriber, store owner, store manager, system admin
- Store and product management
- Multiple purchase types: regular, bid, auction, lottery
- Real-time and deferred notifications
- Comprehensive discount and purchase policy engine
- Administrative controls: user suspension, permission editing, ownership transfers
- Support for extensibility and persistence (init system, mock external integrations)

---

## 🧱 Architecture Summary
The platform is divided into six main layers:

- **Presentation Layer**: User interfaces (Vaadin) following MVP architecture
- **Controller Layer**: REST endpoints for client interactions
- **Application Layer**: Coordinates commands and validation flows between client and domain logic
- **Domain Layer**: Business entities and rules
- **Infrastructure Layer**: Repositories and communication with external systems (e.g., payment/supply mocks)
- **Notification System**: Real-time messaging and alerting framework

---

## 🔄 System Implementation Strategy
The implementation follows a structured and layered approach, enabling clean separation of responsibilities and long-term scalability. Each user interaction follows a clear path:

- Actions start at the **Presentation Layer** using the MVP pattern for UI logic.
- Presenters call the **Controllers**, which expose REST APIs and forward client requests.
- The **Application Layer** interprets these requests, handles coordination logic, and communicates with domain services.
- Core business logic is enforced in the **Domain Layer**, ensuring data integrity and validation.
- Repositories in the **Infrastructure Layer** manage in-memory persistence, identity generation, session tracking, and communication with mock external services.

Subsystems such as cart management, store-role hierarchy, user suspension, and discount enforcement are modeled independently and connected using DTOs and service interfaces.

Examples include:
- Role and permission delegation using dynamic tree structures
- Policy validation through configurable logic
- Token-based session validation
- Full user management from guest creation to admin promotion

This method ensures all system requirements—including concurrency, modularity, extensibility, and persistency—are handled in a consistent and robust way.

---

## 🚀 Launch Instructions
To build and run the project locally:
```bash
# Build and install the project
mvn clean install

# Start the backend server
mvn spring-boot:run

# Open the user interface in your browser
http://localhost:8080/login
```

---

## 📁 Folder Structure (Simplified)
```
Backend/
├── DomainLayer/         ← Core business logic (users, stores, policies...)
├── ApplicationLayer/    ← Coordinates request flows and manages service orchestration
├── Controllers/         ← REST API endpoints for frontend-backend communication
├── DTOs/                ← Data Transfer Objects used across all layers
├── Infrastructure/      ← Repository implementations and in-memory persistence logic
├── PresentationLayer/   ← Views, presenters (Vaadin, MVP), and centralized exception handling
└── Tests/               ← Unit tests, concurrency tests, integration tests, acceptance tests, and demo tests
```

---

## ✅ Requirements Coverage
This implementation covers:
- ✔ Architecture modeling, use case definition
- ✔ Core system and user flows (registration, login, cart, purchases)
- ✔ Real-time notification and advanced policy support
- ✔ Persistent init system, external service integration, extensibility support

---

## 🛠 Technologies Used
- Java 17, Spring Boot 3, Maven
- Vaadin 24 (MVP architecture)
- JWT for authentication
- WebSocket and polling for notifications
- JUnit 5, Mockito, JaCoCo for testing and coverage

---

## 📚 Additional Technical Details
For full breakdowns of each module (StoreUserConnection tree, Cart system, Suspension engine, DiscountPolicies, Repositories, etc.), refer to the technical appendix section below in this README.

---

## 📌 Technical Appendix (Modular Subsystems)

### 🔐 User & Cart Subsystem
Manages guests, registered users, and their interactions with store-specific carts, user sessions, and purchase logic.

### 🌲 Store-User Connection Tree
Models ownership and management hierarchy in stores using a dynamic tree structure that enforces parent-child permissions and secure delegation.
If a manager or owner (e.g., user X) appoints a subordinate (e.g., user Y), then removing user X will also cascade and remove Y from the store structure, maintaining consistent authorization hierarchy.

### ⏸️ User Suspension Engine
Implements timed user suspensions with accurate tracking and control options such as pause and resume. Ensures users cannot bypass suspension periods.

### 🎯 Purchase Policy System
Provides modular and reusable rules for enforcing store policies—such as age restrictions (e.g., alcohol under 18), minimum quantity checks, total cart value constraints, or category-based conditions.

### 🗃 Repository Layer
Implements runtime storage using thread-safe data structures (ConcurrentHashMap, AtomicInteger, etc.), maintaining session data, carts, user states, and store information. It acts as the glue between the Domain and Application Layers.

### 🔔 Notification System
Delivers both real-time and deferred messages based on events such as bids, auction outcomes, ownership changes, or system announcements.

### 🧩 System Initialization Support
The system includes a persistent initialization mechanism that allows loading admin users, default stores, or test data via configuration or external integration. This supports recovery, extensibility, and ensures consistent system state during startup and testing.

---

**Team**: Workshop in Software Engineering, Ben-Gurion University – 2025
