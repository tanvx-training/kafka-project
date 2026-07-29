### author: TanVX

# Package Structure Analysis & Design

> Phân tích kiến trúc package theo Hexagonal Architecture (Ports & Adapters) kết hợp Domain-Driven Design (DDD)

---

## 1. Cấu trúc hiện tại

```
com.example.<service_name>
├── <ServiceName>Application
├── common
│   ├── exception
│   ├── advice
│   ├── response
│   └── util
├── config
├── application
│   └── <bounded_context>
│       ├── usecase
│       ├── dto
│       └── mapper
├── domain
│   └── <bounded_context>
│       ├── model
│       ├── repository
│       └── service
├── infrastructure
│   ├── persistence
│   │   ├── entity
│   │   └── repository
│   ├── messaging
│   │   └── kafka
│   │       ├── producer
│   │       ├── consumer
│   │       ├── event
│   │       └── message
│   └── external
└── interfaces
    └── rest
        └── <bounded_context>
            └── <Context>Controller
```

---

## 2. Phân tích ưu điểm

### 2.1 Tách biệt rõ ràng các tầng (Layer Separation)
Cấu trúc tuân thủ nghiêm ngặt Hexagonal Architecture với 4 tầng tách biệt hoàn toàn:
`interfaces` → `application` → `domain` → `infrastructure`.
Mỗi tầng có một trách nhiệm duy nhất (Single Responsibility), giúp code dễ đọc, dễ maintain và dễ onboard thành viên mới.

### 2.2 Domain độc lập hoàn toàn
Package `domain` không phụ thuộc vào bất kỳ framework hay thư viện nào bên ngoài.
`domain.repository` chỉ chứa **interface** (port), implementation nằm ở `infrastructure.persistence`.
Điều này cho phép thay đổi database (MySQL → MongoDB) hoặc framework mà không đụng đến business logic.

### 2.3 Tổ chức theo Bounded Context
Cả `application`, `domain` và `interfaces.rest` đều được nhóm theo `<bounded_context>`, phản ánh đúng tư tưởng DDD.
Dễ dàng tách service thành microservice độc lập khi cần scale.

### 2.4 Infrastructure tường minh
`infrastructure` chia rõ 3 nhóm adapter ra ngoài:
- `persistence` — adapter tới database
- `messaging.kafka` — adapter tới message broker
- `external` — adapter tới third-party API

### 2.5 Common module tập trung
`common` gom toàn bộ cross-cutting concerns (`exception`, `advice`, `response`, `util`) vào một chỗ, tránh duplicate code giữa các bounded context.

---

## 3. Phân tích nhược điểm

### 3.1 `config` không có tổ chức nội bộ
Package `config` phẳng, không phân loại. Khi service lớn lên, đây sẽ trở thành nơi tập kết hỗn độn của `SecurityConfig`, `KafkaConfig`, `SwaggerConfig`, `RedisConfig`, v.v.

**Rủi ro:** Khó tìm, khó maintain, dễ conflict khi nhiều người làm cùng.

### 3.2 Thiếu `port` tường minh trong `application`
Hiện tại `application.usecase` được ngầm hiểu là inbound port, nhưng không có package `port` rõ ràng.
Ranh giới giữa **port** (interface) và **service** (implementation) dễ bị mờ theo thời gian.

### 3.3 `infrastructure.messaging` chỉ có Kafka
Cấu trúc `messaging/kafka/...` gán cứng vào Kafka. Nếu sau này thêm RabbitMQ, Redis Pub/Sub, hay AWS SQS, cấu trúc sẽ phải thay đổi đáng kể.

### 3.4 Thiếu package `scheduler` / `job`
Các scheduled task (cron job, batch job) thường bị nhét vào `application` hoặc `infrastructure` một cách tùy tiện, thiếu chỗ ở rõ ràng.

### 3.5 `common.util` dễ bị lạm dụng
`util` là package "catch-all" nổi tiếng trong mọi codebase. Không có giới hạn rõ ràng sẽ dẫn đến accumulation của các helper class không liên quan.

### 3.6 `interfaces` chỉ có `rest`
Nếu sau này cần thêm gRPC, GraphQL, hoặc WebSocket, cấu trúc cần mở rộng nhưng hiện tại không có chỗ chứa rõ ràng.

### 3.7 Thiếu tầng `event` trong `domain`
Domain events (sự kiện nội bộ domain như `OrderPlaced`, `PaymentConfirmed`) không có chỗ ở rõ ràng, thường bị nhầm lẫn với Kafka message ở infrastructure.

---

## 4. Cấu trúc package đề xuất (Final)

```
com.example.<service_name>
├── <ServiceName>Application.java
│
├── common                                  # Cross-cutting concerns toàn service
│   ├── exception                           # Custom exception classes
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── ErrorCode.java
│   ├── advice                              # @RestControllerAdvice xử lý exception toàn cục
│   │   └── GlobalExceptionHandler.java
│   ├── response                            # Wrapper response chuẩn (ApiResponse<T>)
│   │   ├── ApiResponse.java
│   │   └── PageResponse.java
│   ├── util                                # Utility thuần (không inject Spring bean)
│   │   ├── DateTimeUtils.java
│   │   └── StringUtils.java
│   └── constant                            # Hằng số dùng chung (thêm mới)
│       └── AppConstants.java
│
├── config                                  # Cấu hình Spring, được nhóm theo mục đích
│   ├── security                            # SecurityConfig, JwtConfig
│   ├── messaging                           # KafkaProducerConfig, KafkaConsumerConfig
│   ├── persistence                         # JpaConfig, TransactionConfig
│   ├── web                                 # WebMvcConfig, CorsConfig, SwaggerConfig
│   └── cache                               # RedisConfig, CacheConfig
│
├── domain                                  # Luật nghiệp vụ thuần — KHÔNG phụ thuộc framework
│   └── <bounded_context>
│       ├── model                           # Aggregate Root, Entity, Value Object
│       │   ├── <BoundedContext>.java       # Aggregate Root
│       │   └── vo                          # Value Objects (Money, Address, ...)
│       ├── event                           # Domain Events (thêm mới)
│       │   └── <BoundedContext>Created.java
│       ├── repository                      # Repository interface (Outbound Port)
│       │   └── <BoundedContext>Repository.java
│       ├── service                         # Domain Service (logic không thuộc về 1 entity)
│       │   └── <BoundedContext>DomainService.java
│       └── exception                       # Domain-specific exception (thêm mới)
│           └── <BoundedContext>Exception.java
│
├── application                             # Orchestration — điều phối giữa domain và adapter
│   └── <bounded_context>
│       ├── port                            # Inbound Ports — interface của use case (thêm mới)
│       │   ├── in                          # Command/Query interface (UseCase interface)
│       │   │   ├── Create<BC>UseCase.java
│       │   │   ├── Update<BC>UseCase.java
│       │   │   └── Query<BC>UseCase.java
│       │   └── out                         # Outbound Ports nếu application cần gọi ra ngoài
│       │       └── <BC>EventPublisher.java
│       ├── service                         # Implementation của UseCase interface
│       │   └── <BoundedContext>Service.java
│       ├── dto                             # Request/Response DTO (interface ↔ application)
│       │   ├── request
│       │   └── response
│       └── mapper                          # DTO ↔ Domain Model mapping
│           └── <BoundedContext>Mapper.java
│
├── infrastructure                          # Adapter ra ngoài — implement các port
│   ├── persistence                         # Adapter tới Database
│   │   ├── entity                          # JPA Entity (tách biệt khỏi Domain Model)
│   │   │   └── <BoundedContext>Entity.java
│   │   ├── repository                      # Spring Data JPA Repository interface
│   │   │   └── <BC>JpaRepository.java
│   │   ├── adapter                         # Implement domain Repository port (thêm mới)
│   │   │   └── <BC>RepositoryAdapter.java
│   │   └── mapper                          # Entity ↔ Domain Model mapping (thêm mới)
│   │       └── <BC>EntityMapper.java
│   │
│   ├── messaging                           # Adapter tới Message Broker (broker-agnostic)
│   │   ├── kafka
│   │   │   ├── producer                    # KafkaTemplate wrapper, implement EventPublisher
│   │   │   ├── consumer                    # @KafkaListener handlers
│   │   │   ├── event                       # Kafka message schema (Avro/JSON POJO)
│   │   │   └── mapper                      # Domain Event ↔ Kafka Event mapping (thêm mới)
│   │   └── (rabbitmq / sqs / ...)          # Dễ mở rộng thêm broker khác
│   │
│   ├── external                            # Adapter tới Third-party API
│   │   └── <provider>
│   │       ├── client                      # Feign / RestClient
│   │       ├── dto                         # External API DTO
│   │       └── adapter                     # Implement port, wrap external client
│   │
│   └── scheduler                           # Scheduled Task / Batch Job (thêm mới)
│       └── <BoundedContext>Scheduler.java
│
└── interfaces                              # Adapter vào — nhận request từ bên ngoài
    ├── rest                                # HTTP REST API
    │   └── <bounded_context>
    │       ├── <BC>Controller.java
    │       ├── dto                         # HTTP-specific request/response (thêm mới)
    │       │   ├── request
    │       │   └── response
    │       └── mapper                      # HTTP DTO ↔ Application DTO (thêm mới)
    │           └── <BC>RestMapper.java
    ├── grpc                                # gRPC service (dễ mở rộng)
    │   └── <bounded_context>
    └── websocket                           # WebSocket handler (dễ mở rộng)
        └── <bounded_context>
```

---

## 5. Mô tả chi tiết từng tầng

### 5.1 `common` — Dùng chung toàn service

| Package | Mô tả |
|---|---|
| `exception` | Custom exception hierarchy. `BusinessException` là base class, các subclass mang `ErrorCode`. |
| `advice` | `@RestControllerAdvice` bắt exception toàn cục, trả về `ApiResponse` chuẩn hóa. |
| `response` | `ApiResponse<T>` bọc mọi HTTP response. `PageResponse<T>` cho pagination. |
| `util` | Các static utility method thuần túy, **không** inject bean, **không** gọi I/O. |
| `constant` | Hằng số chia sẻ giữa các module (header name, cache key prefix, v.v.). |

> **Nguyên tắc:** `common` không được import bất kỳ thứ gì từ `domain`, `application`, hay `infrastructure`.

---

### 5.2 `config` — Cấu hình Spring

Được nhóm theo **mục đích kỹ thuật** thay vì để phẳng:

| Sub-package | Chứa |
|---|---|
| `security` | `SecurityFilterChain`, `JwtAuthFilter`, `PasswordEncoder` bean |
| `messaging` | `ProducerFactory`, `ConsumerFactory`, `KafkaTemplate` |
| `persistence` | `DataSource`, `JpaTransactionManager`, Audit config |
| `web` | `WebMvcConfigurer`, CORS, Swagger/OpenAPI bean |
| `cache` | `RedisCacheManager`, `CacheResolver` |

> **Nguyên tắc:** Config chỉ là "dây nối" giữa framework và business code. Không chứa logic nghiệp vụ.

---

### 5.3 `domain` — Trái tim của service

Tầng **không phụ thuộc** vào bất kỳ framework nào (không có `@Component`, `@Service`, không import Spring).

| Package | Mô tả |
|---|---|
| `model` | **Aggregate Root** — entry point duy nhất để thay đổi state. **Entity** — có identity. **Value Object** (`vo`) — bất biến, so sánh theo giá trị (Money, Email, Address). |
| `event` | **Domain Events** — sự kiện xảy ra bên trong domain (`OrderPlaced`, `UserRegistered`). Được publish sau khi aggregate thay đổi state thành công. |
| `repository` | **Interface** (Outbound Port) — domain định nghĩa *những gì* nó cần từ persistence, không quan tâm *cách* lưu. |
| `service` | **Domain Service** — logic nghiệp vụ không thuộc về một aggregate cụ thể (vd: tính phí vận chuyển cần cả `Order` và `Customer`). |
| `exception` | Exception mang ngữ nghĩa domain (`InsufficientInventoryException`, `DuplicateOrderException`). |

> **Nguyên tắc:** Nếu một class trong `domain` cần import từ Spring hay Hibernate — đó là dấu hiệu cần refactor.

---

### 5.4 `application` — Điều phối use case

Tầng **orchestration**: không chứa business rule, chỉ điều phối domain và các port.

| Package | Mô tả |
|---|---|
| `port.in` | **Inbound Port** — interface của use case (`CreateOrderUseCase`, `GetOrderUseCase`). Controller chỉ phụ thuộc vào interface này, không biết implementation. |
| `port.out` | **Outbound Port** tầng application — các nhu cầu gọi ra ngoài mà application service cần (vd: `OrderEventPublisher`). |
| `service` | **Application Service** — implement `port.in`, inject `domain.repository` và `domain.service`, gọi theo thứ tự: validate → domain logic → persist → publish event. |
| `dto` | DTO trung gian giữa `interfaces` và `application`. Tách biệt HTTP model khỏi domain model. |
| `mapper` | Map `dto` ↔ `domain.model`. Dùng MapStruct hoặc custom mapper. |

> **Nguyên tắc:** Application Service **không được** chứa `if/else` nghiệp vụ phức tạp. Logic đó thuộc về `domain`.

---

### 5.5 `infrastructure` — Adapter ra ngoài

Implement các interface (port) mà `domain` và `application` đã định nghĩa.

#### `persistence`
| Package | Mô tả |
|---|---|
| `entity` | JPA `@Entity` class — schema DB. **Tách biệt** hoàn toàn khỏi Domain Model để tránh coupling. |
| `repository` | Spring Data `JpaRepository` interface — chỉ biết về `entity`, không biết về domain. |
| `adapter` | Implement `domain.repository` interface. Gọi `JpaRepository`, rồi map Entity ↔ Domain Model. |
| `mapper` | `EntityMapper` — map `entity` ↔ `domain.model`. |

#### `messaging`
| Package | Mô tả |
|---|---|
| `kafka/producer` | Implement `application.port.out.EventPublisher`. Wrap `KafkaTemplate`. |
| `kafka/consumer` | `@KafkaListener` — nhận message, deserialize, gọi inbound use case. |
| `kafka/event` | POJO schema của Kafka message (Avro, JSON). |
| `kafka/mapper` | Map `domain.event` ↔ `kafka.event`. |

#### `external`
Mỗi third-party provider có sub-package riêng với pattern: `client` → `dto` → `adapter` (implement domain port).

#### `scheduler`
Chứa `@Scheduled` task. Mỗi scheduler chỉ gọi vào `application.port.in` (use case), không gọi thẳng vào domain hay repository.

---

### 5.6 `interfaces` — Adapter vào

Nhận request từ bên ngoài, chuyển đổi sang application command/query.

| Package | Mô tả |
|---|---|
| `rest/<bc>/<BC>Controller` | `@RestController`. Nhận HTTP request, gọi `port.in` use case, trả về HTTP response. **Không chứa business logic.** |
| `rest/<bc>/dto` | HTTP-specific DTO (có thể có annotation validation `@NotNull`, `@Size`, v.v.). |
| `rest/<bc>/mapper` | Map HTTP DTO ↔ Application DTO. Phân tách rõ "ngôn ngữ HTTP" và "ngôn ngữ application". |
| `grpc/<bc>` | gRPC service stub implementation — cùng pattern với REST. |
| `websocket/<bc>` | `@MessageMapping` handler — cùng pattern với REST. |

> **Nguyên tắc:** Controller chỉ được inject `port.in` interface, **không** inject domain service hay repository trực tiếp.

---

## 6. Luồng phụ thuộc (Dependency Rule)

```
interfaces ──► application ──► domain ◄── infrastructure
    │               │                          │
    └───────────────┴──────────────────────────┘
                    │
              (chỉ theo chiều mũi tên)
```

Mọi phụ thuộc đều **hướng vào trong** về phía `domain`:
- `infrastructure` implement interface của `domain` (Dependency Inversion)
- `interfaces` gọi interface của `application` (Port)
- `domain` không biết gì về các tầng bên ngoài

---

## 7. Ví dụ luồng xử lý: Tạo đơn hàng

```
HTTP POST /orders
    │
    ▼
OrderController (interfaces/rest)
    │  inject CreateOrderUseCase (port.in)
    │  map: CreateOrderHttpRequest → CreateOrderCommand (dto)
    ▼
OrderApplicationService (application/service)
    │  implement CreateOrderUseCase
    │  gọi: OrderRepository.findById() [domain port]
    │  gọi: Order.create() [domain logic]
    │  gọi: OrderRepository.save() [domain port]
    │  gọi: OrderEventPublisher.publish(OrderCreatedEvent) [application port.out]
    ▼
Domain: Order.create()
    │  validate business rule
    │  raise OrderCreated domain event
    ▼
OrderRepositoryAdapter (infrastructure/persistence)
    │  implement domain.repository.OrderRepository
    │  map: Order (domain) → OrderEntity (JPA)
    │  gọi: OrderJpaRepository.save()
    ▼
KafkaOrderEventPublisher (infrastructure/messaging)
    │  implement application.port.out.OrderEventPublisher
    │  map: OrderCreated (domain event) → OrderCreatedMessage (kafka event)
    │  gọi: KafkaTemplate.send()
```

---

## 8. Tổng kết thay đổi so với cấu trúc gốc

| # | Thay đổi | Lý do |
|---|---|---|
| 1 | Thêm `domain/event` | Tường minh hóa Domain Events, tách khỏi Kafka message |
| 2 | Thêm `domain/exception` | Domain exception mang ngữ nghĩa nghiệp vụ, không phải HTTP |
| 3 | Thêm `application/port/in` và `port/out` | Tường minh hóa Inbound/Outbound Port theo Hexagonal Architecture |
| 4 | Đổi `application/usecase` → `application/service` | Rõ ràng hơn: đây là implementation của use case |
| 5 | Thêm `infrastructure/persistence/adapter` và `mapper` | Tách biệt rõ việc implement port và map entity ↔ domain |
| 6 | Thêm `infrastructure/messaging/mapper` | Domain event không trực tiếp là Kafka message |
| 7 | Cấu trúc hóa `config` theo mục đích kỹ thuật | Tránh config package trở thành "junk drawer" |
| 8 | Thêm `infrastructure/scheduler` | Scheduled task có chỗ ở rõ ràng |
| 9 | Thêm `common/constant` | Tách hằng số ra khỏi `util` |
| 10 | Mở rộng `interfaces` cho `grpc`, `websocket` | Chuẩn bị cho multi-protocol adapter |
| 11 | Thêm DTO và mapper trong `interfaces/rest/<bc>` | Tách "HTTP model" khỏi "Application model" hoàn toàn |