# CertiMaster — Package Structure & Architecture Guide

> Kiến trúc **thực dụng** cho dự án cá nhân: đủ clean, đủ solid, không over-engineer.
> Stack: Java 21 · Spring Boot 3.4 · Spring Cloud 2024 · Kafka · OpenTelemetry

---

## 1. Tổng quan Multi-module

```
certi-master/                          ← Parent POM
├── common-library/                    ← Thư viện dùng chung
├── api-gateway/                       ← Spring Cloud Gateway
├── identity-service/                  ← Auth, User
├── learning-service/                  ← Course, Certificate
├── engagement-service/                ← Progress, Achievement
└── notification-service/              ← Email, Push
```

---

## 2. Đánh giá `common-library`

### 2.1 Ưu điểm

**Tránh duplicate code có giá trị cao**
Các thành phần như `ApiResponse<T>`, `ErrorCode`, `BusinessException`, JWT util, và Kafka event schema
là những thứ **thực sự cần dùng chung** giữa nhiều service. Không có `common-library`, mỗi service sẽ
tự copy-paste và dần dần phát sinh inconsistency.

**Đồng bộ contract giữa các service**
Khi `identity-service` publish event `UserRegistered`, `notification-service` cần deserialize đúng schema đó.
`common-library` là nơi lý tưởng để giữ shared event schema, tránh mismatch.

**Chuẩn hóa response format**
Toàn bộ hệ thống trả về cùng một `ApiResponse` structure — quan trọng khi có API Gateway ở trước.

---

### 2.2 Nhược điểm & Rủi ro

#### ❌ Rủi ro 1: `common-library` trở thành "God Module"
Đây là **rủi ro lớn nhất và phổ biến nhất**. Theo thời gian, mọi thứ khó phân loại đều bị ném vào đây.
Kết quả: một service nhỏ như `notification-service` phải kéo theo dependency của cả JPA, Kafka, Security
chỉ vì chúng nằm cùng `common-library`.

**Dấu hiệu nhận biết:** `common-library/pom.xml` có nhiều hơn 5 dependency framework.

#### ❌ Rủi ro 2: Coupling ngầm giữa các service
Khi sửa một class trong `common-library`, **tất cả** service phải rebuild và redeploy. Với dự án cá nhân
điều này chấp nhận được, nhưng cần ý thức rõ để không tạo coupling không cần thiết.

#### ❌ Rủi ro 3: Phụ thuộc tuần hoàn tiềm ẩn
`common-library` phải là **lá** trong dependency graph (không import bất kỳ service nào khác).
Vi phạm nguyên tắc này sẽ tạo circular dependency, Maven sẽ báo lỗi build.

#### ❌ Rủi ro 4: Versioning không rõ ràng
Hiện tại dùng `1.0.0-SNAPSHOT` chung cho toàn bộ parent. Nếu `common-library` thay đổi breaking,
tất cả service bị ảnh hưởng ngay mà không có cơ chế kiểm soát.

---

### 2.3 Nguyên tắc cho `common-library` — "Chỉ chứa những gì STATELESS và FRAMEWORK-AGNOSTIC"

```
✅ NÊN đưa vào common-library          ❌ KHÔNG đưa vào common-library
────────────────────────────────────   ────────────────────────────────────
ApiResponse<T>, PageResponse<T>        Entity / JPA class của bất kỳ service nào
ErrorCode enum, BusinessException      Repository / Service của bất kỳ domain nào
JWT utility (parse, validate)          Kafka Consumer (logic consume riêng mỗi service)
Shared Kafka event schema (POJO)       Security filter chain (config riêng từng service)
DateTimeUtils, StringUtils thuần       Feign client (coupling với service cụ thể)
Constant (header name, MDC key)        @Configuration bean (lifecycle Spring)
BaseAuditEntity (nếu dùng JPA chung)  Business logic bất kỳ
```

---

### 2.4 Cấu trúc đề xuất cho `common-library`

```
common-library/
└── src/main/java/com/certimaster/common/
    ├── response/
    │   ├── ApiResponse.java            # { success, message, data, errorCode }
    │   └── PageResponse.java           # { content, page, size, totalElements }
    ├── exception/
    │   ├── BusinessException.java      # RuntimeException + ErrorCode
    │   └── ErrorCode.java              # Enum: USER_NOT_FOUND, INVALID_TOKEN, ...
    ├── advice/
    │   └── GlobalExceptionHandler.java # @RestControllerAdvice dùng chung
    ├── event/                          # Shared Kafka Event Schema
    │   ├── UserRegisteredEvent.java
    │   ├── CertificateIssuedEvent.java
    │   └── BaseEvent.java              # eventId, occurredAt, version
    ├── security/
    │   └── JwtUtils.java               # Parse/validate JWT (stateless util)
    └── constant/
        └── AppConstants.java           # Header names, MDC keys, cache prefixes
```

**`common-library/pom.xml` chỉ nên có:**
```xml
<dependencies>
    <!-- Chỉ những gì thực sự cần -->
    <dependency>lombok</dependency>
    <dependency>jackson-databind</dependency>          <!-- cho event POJO -->
    <dependency>spring-web</dependency>                <!-- cho @RestControllerAdvice -->
    <dependency>jjwt (hoặc nimbus-jose-jwt)</dependency>
</dependencies>
```
> **Không** có `spring-data-jpa`, `spring-kafka`, `spring-security` trong common-library.

---

## 3. Package Structure — Phiên bản Thực dụng (Personal Project)

### Nguyên tắc thiết kế
- **Mỗi file chỉ làm 1 việc** nhưng **không tạo interface nếu chỉ có 1 implementation**
- **Bỏ `port/in`, `port/out`** — overkill cho dự án cá nhân
- **Giữ `domain` thuần** — không import Spring/JPA
- **Mapper, DTO, Service** — đủ để clean, không hơn

---

### 3.1 Cấu trúc chuẩn mỗi service

```
com.certimaster.<service_name>
├── <ServiceName>Application.java
│
├── config/                            # Spring configuration beans
│   ├── SecurityConfig.java
│   ├── KafkaConfig.java
│   └── OpenApiConfig.java
│
├── domain/                            # Business logic — KHÔNG import Spring/JPA
│   └── <bounded_context>/
│       ├── model/                     # POJO thuần: Aggregate, Entity, Value Object
│       ├── repository/                # Interface — "cần gì từ DB"
│       └── service/                   # Domain Service (logic đa aggregate)
│                                      # Bỏ nếu không có logic phức tạp
│
├── application/                       # Orchestration, Use case
│   └── <bounded_context>/
│       ├── <BC>Service.java           # Use case chính: validate → domain → persist → event
│       ├── dto/                       # Request/Response DTO
│       │   ├── <BC>Request.java
│       │   └── <BC>Response.java
│       └── mapper/                    # DTO ↔ Domain (dùng MapStruct)
│           └── <BC>Mapper.java
│
├── infrastructure/                    # Adapter ra ngoài
│   ├── persistence/
│   │   ├── entity/                    # JPA @Entity
│   │   ├── repository/                # Implement domain.repository (Spring Data JPA)
│   │   └── mapper/                    # Entity ↔ Domain Model
│   ├── messaging/
│   │   ├── producer/                  # Publish event ra Kafka
│   │   └── consumer/                  # Consume event từ Kafka
│   └── external/                      # Feign client tới service khác (nếu có)
│
└── interfaces/
    └── rest/
        └── <bounded_context>/
            └── <BC>Controller.java    # @RestController — chỉ inject Application Service
```

---

### 3.2 Áp dụng cụ thể cho từng service

#### `identity-service` — Auth, User, Role
```
com.certimaster.identity
├── IdentityApplication.java
├── config/
│   ├── SecurityConfig.java            # JWT filter, password encoder
│   └── OpenApiConfig.java
├── domain/
│   └── user/
│       ├── model/
│       │   ├── User.java              # Aggregate Root
│       │   └── Role.java              # Entity
│       └── repository/
│           └── UserRepository.java    # Interface
├── application/
│   └── user/
│       ├── UserService.java           # register, login, refreshToken, getProfile
│       ├── dto/
│       │   ├── RegisterRequest.java
│       │   ├── LoginRequest.java
│       │   └── AuthResponse.java      # { accessToken, refreshToken }
│       └── mapper/
│           └── UserMapper.java
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   └── UserEntity.java
│   │   ├── repository/
│   │   │   └── UserJpaRepository.java # implement UserRepository
│   │   └── mapper/
│   │       └── UserEntityMapper.java
│   └── messaging/
│       └── producer/
│           └── UserEventProducer.java # publish UserRegisteredEvent
└── interfaces/
    └── rest/
        └── user/
            ├── AuthController.java    # POST /auth/register, /auth/login
            └── UserController.java    # GET /users/me, PUT /users/me
```

#### `learning-service` — Course, Certificate
```
com.certimaster.learning
├── domain/
│   ├── course/
│   │   ├── model/ Course.java, Lesson.java, Enrollment.java
│   │   └── repository/ CourseRepository.java, EnrollmentRepository.java
│   └── certificate/
│       ├── model/ Certificate.java
│       └── repository/ CertificateRepository.java
├── application/
│   ├── course/
│   │   ├── CourseService.java
│   │   └── dto/ ...
│   └── certificate/
│       ├── CertificateService.java
│       └── dto/ ...
├── infrastructure/
│   ├── persistence/ ...
│   └── messaging/
│       ├── producer/ CertificateEventProducer.java
│       └── consumer/ EnrollmentEventConsumer.java
└── interfaces/rest/
    ├── course/ CourseController.java
    └── certificate/ CertificateController.java
```

#### `notification-service` — Email, Push (Event-driven, không có REST write)
```
com.certimaster.notification
├── domain/
│   └── notification/
│       ├── model/ Notification.java
│       └── repository/ NotificationRepository.java
├── application/
│   └── notification/
│       ├── NotificationService.java   # sendEmail, sendPush, saveHistory
│       └── dto/ ...
├── infrastructure/
│   ├── persistence/ ...
│   ├── messaging/
│   │   └── consumer/
│   │       ├── UserEventConsumer.java         # consume UserRegisteredEvent
│   │       └── CertificateEventConsumer.java  # consume CertificateIssuedEvent
│   └── external/
│       ├── email/ SendGridClient.java
│       └── push/  FirebaseClient.java
└── interfaces/rest/
    └── notification/ NotificationController.java  # GET /notifications (lịch sử)
```

#### `api-gateway` — Routing, Auth filter
```
com.certimaster.gateway
├── GatewayApplication.java
├── config/
│   ├── RouteConfig.java               # @Bean RouteLocator — định nghĩa route
│   └── CorsConfig.java
└── filter/
    ├── JwtAuthFilter.java             # GlobalFilter — validate JWT trước khi forward
    └── RequestLoggingFilter.java      # Log request/response
```
> `api-gateway` cực kỳ gọn. Không có domain, không có persistence.

---

## 4. Quy tắc phụ thuộc

```
┌─────────────────────────────────────────┐
│              common-library             │  ← Không import service nào
└─────────────────────────────────────────┘
           ▲         ▲         ▲
    identity    learning   notification  ← Chỉ import common-library
           ▲         ▲         ▲
           └────── api-gateway ──────┘  ← Route, không import service logic
```

**Trong mỗi service:**
```
interfaces → application → domain ← infrastructure
```
- `infrastructure` implement interface của `domain` (Dependency Inversion)
- `interfaces` chỉ gọi `application service`, không gọi thẳng `domain` hay `infrastructure`
- `domain` không biết gì về Spring, JPA, Kafka

---

## 5. Những gì CỐ TÌNH bỏ (so với full Hexagonal)

| Bỏ | Lý do |
|---|---|
| `port/in`, `port/out` interface | Dự án cá nhân: 1 implementation → interface dư thừa |
| `domain/event` package riêng | Dùng `common-library/event` cho shared event là đủ |
| `interfaces/rest/<bc>/mapper` riêng | DTO của application đủ nhỏ, không cần thêm tầng HTTP-specific DTO |
| `infrastructure/scheduler` | Đặt thẳng vào `application/<bc>/<BC>Scheduler.java` nếu có |
| `grpc`, `websocket` | Thêm sau nếu thực sự cần |

---

## 6. Checklist trước khi code

**Thêm class mới, hỏi:**
- [ ] Class này thuộc tầng nào? (`domain` / `application` / `infrastructure` / `interfaces`)
- [ ] Class này có import Spring annotation không? → Nếu có, không được để trong `domain`
- [ ] Class này có logic nghiệp vụ không? → Nếu có, không để trong `infrastructure` hay `interfaces`
- [ ] Có cần thêm vào `common-library` không? → Chỉ thêm nếu **ít nhất 2 service** cần dùng

**Thêm vào `common-library`, hỏi:**
- [ ] Class này có import JPA / Kafka / Security không? → Không được thêm
- [ ] Class này stateless không? → Phải stateless
- [ ] Thay đổi class này có ảnh hưởng breaking API không? → Nếu có, cần notify tất cả service