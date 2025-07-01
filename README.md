
# 🛍️ MarketPlace System – Version 3: Persistency, Robustness & Initialization

## 📌 Project Overview
This is a Spring Boot-based multi-layer e-commerce system built with layered architecture and MVP-based UI using Vaadin. The system supports:

- 🧾 User registration, login, roles (admin, owner, manager, guest)
- 🏬 Store creation and management
- 🛒 Shopping cart and advanced purchase types (auction, lottery/bid)
- 💳 External payment and supply APIs
- 🔁 Initialization via custom DSL file (`dataToInit.txt`)
- 💾 Full persistency using Hibernate + SQL Server
- 📡 Real-time notification system via WebSocket

## 🗂️ Project Structure

```bash
src/
├── ApplicationLayer/           # Business logic coordination (e.g., StoreService)
├── Config/                     # JWT, Interceptors, WebSocketConfig
├── Controllers/                # REST API endpoints
├── DomainLayer/                # Core business logic (not included here)
├── DTOs/                       # Data transfer objects used across layers
├── InfrastructureLayer/       # Persistence, Repositories, JWT, AI, Auth
├── PresentationLayer/          # Connects views to services, UI screens and Request DTOs and ExceptionHandlers
└── DemoApplication.java        # Entry point of the Spring Boot app
```

## 🔧 System Configuration

### `application.properties`
WorkShop\Backend\src\main\resources\application.properties

### `application-db.properties`
WorkShop\Backend\src\main\resources\application-db.properties

## ⚙️ System Initialization

### 1. Initialization File: `dataToInit.txt`
WorkShop\Backend\src\main\resources\dataToInit.txt

### 2. API for Initialization
admin\init 

The system uses:
- `AppSettingsService` to control initialization status
- `AppInitializationInterceptor` to block usage before init
- `DemoApplication` to load context on startup

## 🔐 Authentication & Authorization

- Based on **JWT** tokens (`AuthenticationRepo`)
- Managed by `JwtConfig` and used across all services
- Validated before executing any restricted endpoint
- Role-based access for guests, users, managers, and admins

## 💾 Persistency

- All persistent data stored in SQL Server using **Spring Data JPA**

> Transient data (e.g. guest carts) are kept in memory.

## 🧠 AI Integration

Implemented in `AISearch` using REST to Flask server:
- `GET /get-matches-products`
- `POST /addPairs`

Used to match product names/suggestions during search.

## 🔌 External Systems

All integrations use HTTP POST via the URL:
```
https://damp-lynna-wsep-1984852e.koyeb.app/
```

### Supported actions:
- `handshake`
- `pay`, `cancel_pay`
- `supply`, `cancel_supply`

Implemented in the infrastructure and triggered from `PurchaseService`.

## 📡 WebSocket & Notifications

Real-time support via:
- `WebSocketConfig` – enables STOMP protocol
- `SocketHandler` – routes messages to clients
- Used for: manager updates, store activity, auction outcomes, etc.

## 🎨 UI – Presentation Layer

### 1. `View/`
- Built with **Vaadin**
- Examples: `LoginView`, `MyCartView`
- UI-only, no business logic

### 2. `Presenter/`
- Responsible for:
  - Calling backend APIs via `RestTemplate`
  - Handling success/failure and displaying results
  - Communicating with `NotificationView` on errors

### 3. `Requests/`
- Contains DTOs like `AddToCartRequest`
- `ExceptionHandlers` provides centralized UI-level error capture

## 🧪 Testing & Use Cases

- Initialization is verified using `dataToInit.txt`
- Acceptance tests include:
  - View owned stores, add items, cart operations
  - Auction/lottery flows and refunds upon cancellation
  - User/manager permission handling
- All errors passed through `UIException` and `ApiResponse`

## 📝 Example API Requests

### Register new user
```http
POST /api/users/register?username=joe&password=123&age=25
```

### Add item to cart
```json
POST /api/cart/add
Authorization: Bearer <token>
Body:
{
  "productId": 1,
  "storeId": 2,
  "quantity": 1
}
```

## ✅ Features Summary

| Feature                    | Description                                       |
|---------------------------|---------------------------------------------------|
| Guest and User flows      | Registration, login, carts, bidding              |
| Store management          | Open stores, manage products, permissions        |
| Purchase types            | Standard, Lottery, Auction                       |
| Persistency               | Hibernate with SQL Server                        |
| System initialization     | From file with rollback on failure               |
| JWT Auth                  | Token-based secured access                       |
| Real-time notifications   | Via WebSocket                                     |
| AI product search         | Via local Flask service                          |
| External systems          | Integrated Payment and Supply via REST           |

## 👩‍💻 Authors
- Team: WSEP 2025
- Institution: Software Engineering Workshop
- Version: 3 – Final System Design and Integration
