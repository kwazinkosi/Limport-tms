# Limport TMS (Transport Management Service)

A Transport Management Service built with Spring Boot following Domain-Driven Design (DDD) and Hexagonal Architecture principles.

## Prerequisites

- Docker & Docker Compose
- Java 17+
- [Limport Platform](https://github.com/kwazinkosi/limport-platform) running (shared infrastructure)

## Architecture

Follows Clean Architecture with DDD and event-driven patterns:

```
├── application/     # Use cases, commands, queries, DTOs, event services
├── domain/          # Business logic, entities, value objects, domain events
├── infrastructure/  # Adapters, configs, repositories, event publishing
└── presentation/    # REST & GraphQL controllers
```

### Event-Driven Architecture

TMS publishes domain events via the **Outbox Pattern** for reliable asynchronous communication:

- **Domain Events**: `TransportRequestCreatedEvent`, `TransportRequestUpdatedEvent`, `TransportRequestCancelledEvent`, `TransportRouteOptimizedEvent`
- **Event Storage**: Transactionally stored in `outbox_events` table alongside aggregate changes
- **Event Publishing**: Background processor polls outbox and publishes to Kafka topics
- **Reliability**: At-least-once delivery guarantee with retry logic

### External Service Integration

TMS integrates with other microservices via REST APIs and event streams:

- **Provider Matching Service (PMS)**: Capacity verification and provider matching
  - `GET /api/providers/{id}/capacity` - Verify provider capacity
  - `POST /api/providers/match` - Get provider suggestions
  - Stub mode enabled by default (`tms.pms.stub-mode=true`)
  
- **Route Optimization**: Handled internally by TMS with potential future extraction to dedicated service

## Port Configuration

### Shared Infrastructure (from Limport Platform)

| Port | Service | Description |
|------|---------|-------------|
| 6379 | Redis | Shared cache |
| 5540 | Redis Insight | Redis management UI |
| 8081 | Kafka UI | Kafka cluster management |
| 9092 | Kafka (internal) | Inter-service communication |
| 9094 | Kafka (external) | External/localhost access |
| 8025 | MailHog Web UI | Email testing interface |
| 1025 | MailHog SMTP | SMTP server for dev emails |

### TMS Local Services

| Port | Service | Description |
|------|---------|-------------|
| 5434 | TMS Postgres | Dedicated TMS database |
| 8082 | Adminer | Database management UI |
| 8080 | TMS Application | Spring Boot app (when running) |

### Dev Container Forwarded Ports

The following ports are automatically forwarded when using the dev container:

```
8081  - Kafka UI
8025  - MailHog Web UI  
5540  - Redis Insight
6379  - Redis
9094  - Kafka (external)
```

## Getting Started

### 1. Start Shared Infrastructure

Ensure the [Limport Platform](https://github.com/kwazinkosi/limport-platform) is running:

```bash
cd ../limport-platform
docker compose up -d
```

### 2. Start TMS Services

```bash
docker compose up -d
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

## Service URLs

| Service | URL |
|---------|-----|
| TMS API | http://localhost:8080 |
| TMS Adminer | http://localhost:8082 |
| Kafka UI | http://localhost:8081 |
| Redis Insight | http://localhost:5540 |
| MailHog | http://localhost:8025 |

## Database Connection

```
Host: localhost
Port: 5434
Database: tms_db
Username: tms_user
Password: tms_password
```

## Networks

- **limport-network** (external): Connects to shared platform services (Redis, Kafka, MailHog)
- **tms-local**: Isolated network for TMS-specific services (Postgres, Adminer)

## Event Topics

TMS publishes to the following Kafka topics:

| Topic | Event | Description |
|-------|-------|-------------|
| `tms.events.request.created` | TransportRequestCreatedEvent | New transport request created |
| `tms.events.request.updated` | TransportRequestUpdatedEvent | Request details or status updated |
| `tms.events.request.cancelled` | TransportRequestCancelledEvent | Request cancelled by user/system |
| `tms.events.request.assigned` | TransportRequestAssignedEvent | Request assigned to provider/vehicle |
| `tms.events.request.completed` | TransportRequestCompletedEvent | Transport successfully completed |
| `tms.events.request.rematching-triggered` | TransportRequestReMatchingTriggeredEvent | Request needs re-matching after provider rejection/timeout |
| `tms.events.route.optimized` | TransportRouteOptimizedEvent | Optimal route determined |

**Note**: Capacity verification events are published by PMS, not TMS.

Events are consumed by downstream services for provider matching, route optimization, and workflow coordination.