# Online Book Store

A production-style microservices project built using:

- Java 21
- Spring Boot 3.5.x
- Spring Cloud
- MySQL
- Maven Multi-Module Architecture

This repository accompanies the Online Book Store Microservices course.

## Modules

- Product Service
- Order Service
- Auth Service
- Payment Service
- Notification Service
- API Gateway
- Eureka Discovery Server

Each lesson is tagged so students can check out the project at the end of any lesson.

Example:

git checkout lesson-2.1-multi-module-repository

## Running Kafka Locally

The Online Book Store project uses Apache Kafka for asynchronous communication between microservices.

Kafka runs locally using Docker Compose in KRaft mode. ZooKeeper is not required.

### Prerequisites

- Docker Desktop installed and running.
- Docker Compose available through Docker Desktop.

### Start Kafka

Navigate to the Kafka configuration directory:

```bash
cd bookstore-kafka
```

Start the Kafka broker:

```bash
docker compose up -d
```

This starts Apache Kafka 3.9.1 and exposes it on port `9092`.

### Verify Kafka

Check whether the Kafka container is running:

```bash
docker ps
```

Check Kafka logs:

```bash
docker logs bookstore-kafka
```

### Stop Kafka

To stop the Kafka container:

```bash
docker compose down
```

### Kafka Configuration

| Property | Value |
|---|---|
| Kafka image | apache/kafka:3.9.1 |
| Container name | bookstore-kafka |
| Mode | KRaft |
| Bootstrap server | localhost:9092 |
| ZooKeeper | Not required |