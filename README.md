<p align="center">
  <h1 align="center">ShopFlow Microservices</h1>
  <p align="center">
    A production-ready e-commerce backend built with <strong>Spring Boot 3</strong> and <strong>Spring Cloud</strong>, demonstrating microservices architecture patterns including service discovery, centralized configuration, API gateway, distributed tracing, centralized logging, circuit breakers, and OAuth2 security.
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring%20Cloud-2025.0.0-blue?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Cloud"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
</p>

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Services](#services)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Observability](#observability)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## Architecture Overview

```
                         ┌─────────────────┐
                         │   Keycloak IAM   │
                         │   (Port 8180)    │
                         └────────┬─────────┘
                                  │ JWT Validation
                                  ▼
┌──────────┐            ┌─────────────────┐            ┌──────────────────┐
│  Client  │───────────▶│   API Gateway   │───────────▶│ Discovery Service│
│          │            │   (Port 8080)   │◀───────────│  Eureka (8761)   │
└──────────┘            └────────┬────────┘            └──────────────────┘
                                 │                              ▲
                    ┌────────────┼────────────┐                 │
                    ▼                         ▼                 │
           ┌───────────────┐         ┌───────────────┐          │
           │Product Service│         │ Order Service  │──────────┘
           │  (Port 8081)  │◀────────│  (Port 8082)  │
           │    [H2 DB]    │ OpenFeign│    [H2 DB]    │
           └───────┬───────┘         └───────┬───────┘
                   │                         │
                   └──────────┬──────────────┘
                              ▼
              ┌──────────────────────────────┐
              │      Config Server (8888)     │
              │    (Git-backed properties)    │
              └──────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        ┌──────────┐   ┌───────────┐   ┌──────────┐
        │  Zipkin   │   │ Logstash  │   │  Kibana  │
        │  (9411)   │   │  (5000)   │   │  (5601)  │
        └──────────┘   └─────┬─────┘   └────┬─────┘
                             │              │
                             ▼              │
                       ┌──────────────┐     │
                       │Elasticsearch │◀────┘
                       │   (9200)     │
                       └──────────────┘
```

---

## Services

| Service | Port | Description |
|---|---|---|
| **API Gateway** | `8080` | Single entry point for all client requests. Routes traffic to downstream services via Eureka service discovery. Secures all endpoints with OAuth2/JWT (Keycloak). |
| **Discovery Service** | `8761` | Netflix Eureka Server for service registration and discovery. All microservices register here automatically. |
| **Config Server** | `8888` | Spring Cloud Config Server providing centralized, Git-backed configuration for all services. |
| **Product Service** | `8081` | Manages product catalog — create and retrieve products. Uses H2 in-memory DB and exposes Swagger UI. |
| **Order Service** | `8082` | Handles order placement. Communicates with Product Service via OpenFeign with Resilience4j circuit breaker fallback. Uses H2 in-memory DB and exposes Swagger UI. |

---

## Tech Stack

### Core
- **Java 17** — Language
- **Spring Boot 3.5.8** — Application framework
- **Spring Cloud 2025.0.0** — Microservices toolkit
- **Maven** — Multi-module build tool

### Microservices Patterns
| Pattern | Technology |
|---|---|
| API Gateway | Spring Cloud Gateway |
| Service Discovery | Netflix Eureka |
| Centralized Config | Spring Cloud Config Server (Git backend) |
| Inter-Service Communication | OpenFeign (declarative REST client) |
| Circuit Breaker | Resilience4j |
| Security | OAuth2 Resource Server + Keycloak (JWT) |

### Observability
| Concern | Technology |
|---|---|
| Distributed Tracing | Micrometer Tracing + OpenTelemetry → Zipkin |
| Centralized Logging | Logstash Logback Encoder → ELK Stack |
| Health & Metrics | Spring Boot Actuator |

### Data & Documentation
| Concern | Technology |
|---|---|
| Database | H2 (in-memory, development) |
| ORM | Spring Data JPA + Hibernate |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Boilerplate Reduction | Lombok |

### Infrastructure
- **Docker Compose** — Orchestrates Zipkin, Elasticsearch, Logstash, and Kibana containers

---

## Prerequisites

Make sure the following are installed on your machine:

- **Java 17+** — [Download](https://adoptium.net/)
- **Maven 3.8+** — [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** — [Download](https://www.docker.com/products/docker-desktop/)
- **Keycloak** (for authentication) — [Download](https://www.keycloak.org/downloads)
  - Running on port `8180`
  - A realm named `ShopFlow` must be configured

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Aakashgautam41/shopflow-parent.git
cd shopflow-parent
```

### 2. Set up the external Config Repository

The Config Server reads properties from a local Git repository. Create one:

```bash
mkdir -p ~/Documents/GitHub/microservices-config-repo
cd ~/Documents/GitHub/microservices-config-repo
git init
```

Add service-specific config files (e.g., `product-service.properties`, `order-service.properties`) to this repo and commit them.

> **Note:** Update the path in `config-server/src/main/resources/application.properties` if your config repo is in a different location.

### 3. Start infrastructure services (Docker)

```bash
docker-compose up -d
```

This starts:
- **Zipkin** — `http://localhost:9411`
- **Elasticsearch** — `http://localhost:9200`
- **Logstash** — port `5000` (TCP)
- **Kibana** — `http://localhost:5601`

### 4. Set up Keycloak

1. Start Keycloak on port `8180`
2. Create a realm named **ShopFlow**
3. Create a client for the API Gateway
4. Create test users and assign roles

### 5. Start all microservices

Use the provided startup script (macOS):

```bash
chmod +x run-all.sh
./run-all.sh
```

This starts services in the correct order:
1. **Config Server** → waits 15s
2. **Discovery Service** → waits 10s
3. **Product Service**, **Order Service**, **API Gateway** → started in parallel

**Or start manually** (in separate terminals):

```bash
# Terminal 1 — Config Server (start first!)
cd config-server && mvn spring-boot:run

# Terminal 2 — Discovery Service (start after Config Server is ready)
cd discovery-service && mvn spring-boot:run

# Terminals 3, 4, 5 — Business Services (start after Discovery Service is ready)
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### 6. Verify everything is running

| Service | URL |
|---|---|
| Eureka Dashboard | [http://localhost:8761](http://localhost:8761) |
| API Gateway | [http://localhost:8080](http://localhost:8080) |
| Product Service Swagger | [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) |
| Order Service Swagger | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) |
| Zipkin Dashboard | [http://localhost:9411](http://localhost:9411) |
| Kibana Dashboard | [http://localhost:5601](http://localhost:5601) |

---

## API Endpoints

> All requests through the API Gateway (`localhost:8080`) require a valid **JWT Bearer token** from Keycloak.

### Product Service

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/products/{id}` | Get a product by ID |
| `POST` | `/products` | Create a new product |
| `GET` | `/products/message` | Get the welcome message (from Config Server) |

**Create a product:**
```bash
curl -X POST http://localhost:8081/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Wireless Mouse", "price": 29.99}'
```

**Get a product:**
```bash
curl http://localhost:8081/products/1
```

### Order Service

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/orders` | Place a new order (validates product via Product Service) |

**Place an order:**
```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 3}'
```

> **Circuit Breaker:** If the Product Service is unavailable, the order endpoint gracefully returns a fallback response with `productName: "Unavailable - Please Retry Later"` and `totalPrice: 0.0` instead of failing.

---

## Observability

### Distributed Tracing (Zipkin)

All services propagate trace IDs automatically via Micrometer + OpenTelemetry. View end-to-end traces at:

**[http://localhost:9411](http://localhost:9411)**

### Centralized Logging (ELK Stack)

Logs are shipped from services → Logstash (port 5000) → Elasticsearch → Kibana.

1. Open Kibana at **[http://localhost:5601](http://localhost:5601)**
2. Create an index pattern: `microservices-logs-*`
3. Explore logs in the **Discover** tab

### Health Checks (Actuator)

All services expose health endpoints:

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

---

## Project Structure

```
shopflow-microservices/
├── api-gateway/                 # Spring Cloud Gateway + OAuth2 Security
│   └── src/main/java/.../
│       ├── ApiGatewayApplication.java
│       └── config/SecurityConfig.java
│
├── config-server/               # Centralized config (Git-backed)
│   └── src/main/java/.../
│       └── ConfigServerApplication.java
│
├── discovery-service/           # Eureka Server
│   └── src/main/java/.../
│       └── DiscoveryServiceApplication.java
│
├── product-service/             # Product CRUD microservice
│   └── src/main/java/.../
│       ├── controller/ProductController.java
│       ├── entity/Product.java
│       ├── repository/ProductRepository.java
│       ├── exception/GlobalExceptionHandler.java
│       └── config/OpenApiConfig.java
│
├── order-service/               # Order placement microservice
│   └── src/main/java/.../
│       ├── controller/OrderController.java
│       ├── entity/Order.java
│       ├── dto/OrderRequest.java & ProductDTO.java
│       ├── client/ProductClient.java          # OpenFeign client
│       ├── repository/OrderRepository.java
│       ├── exception/GlobalExceptionHandler.java
│       └── config/OpenApiConfig.java
│
├── pipeline/
│   └── logstash.conf            # Logstash pipeline config
│
├── docker-compose.yml           # Zipkin + ELK stack
├── run-all.sh                   # macOS startup script
└── pom.xml                      # Parent POM (multi-module)
```



