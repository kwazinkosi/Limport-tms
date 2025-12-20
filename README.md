# Limport TMS (Transport Management Service)

A Transport Management Service built with Spring Boot following Domain-Driven Design (DDD) and Hexagonal Architecture principles.

## Prerequisites

- Docker & Docker Compose
- Java 17+
- [Limport Platform](https://github.com/kwazinkosi/limport-platform) running (shared infrastructure)

## Architecture

```
├── application/     # Use cases, commands, queries, DTOs
├── domain/          # Business logic, entities, value objects, events
├── infrastructure/  # Adapters, configs, repositories
└── presentation/    # REST & GraphQL controllers
```

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