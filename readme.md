# GlobalCards

## Overview

GlobalCards is an event driven banking microservice designed to process massive card files using Spring Boot, Spring Batch and Apache Kafka. The system follows Hexagonal Architecture and enterprise banking patterns to ensure scalability, consistency and reliability.

This project simulates a real banking batch system similar to those used in large financial institutions.

---

## Architecture

Hexagonal Architecture (Ports and Adapters)

Layers:

- Domain
  - Business models
  - Domain logic

- Application
  - Use cases
  - Input and output ports

- Infrastructure
  - Spring Batch jobs
  - Kafka producer
  - S3 integration
  - PostgreSQL persistence
  - Configuration

---

## Main Features

- Processes millions of cards using Spring Batch
- Reads large CSV files from AWS S3
- Splits processing using partitioning and multithreading
- Chunk oriented processing
- Publishes events to Kafka
  - cardsok topic
  - cardsko topic
- Stores batch metadata in PostgreSQL
- Fully event driven
- Fault tolerant
- Restartable jobs

---

## Technologies

- Java 21
- Spring Boot
- Spring Batch
- Apache Kafka
- PostgreSQL
- AWS S3
- Maven

---

## Kafka Flow

Batch Processing

Reader

Process card

If OK

Publish event to cardsok topic

If KO

Publish event to cardsko topic

Kafka Broker stores events

Consumers can process events independently

---

## Batch Flow

1. File uploaded to S3

2. Batch job starts

3. File is read using multipart

4. File is partitioned

5. Cards processed in chunks

6. Events published to Kafka

7. Metadata stored in PostgreSQL

---

## Project Structure

com.globalcards

- domain
- application
- infrastructure

---

## Scalability

Designed to support:

- Millions of cards per file
- Horizontal scaling
- Multiple Kafka brokers
- Parallel processing

---

## Real World Concepts Implemented

- Event Driven Architecture
- Hexagonal Architecture
- Batch Processing
- Idempotency
- Fault Tolerance
- Separation of concerns

---

## Author

David Muñoz

Backend Engineer

---

## Status

In progress

Enterprise grade architecture implementation

