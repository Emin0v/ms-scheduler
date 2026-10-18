# ms-scheduler

A Spring Boot microservice for scheduling and managing jobs using Quartz.

## Features

- Schedule jobs with cron expressions
- Get all scheduled jobs
- Get all running jobs
- Pause, resume, and delete jobs

## Technologies Used

- **Java:** 21
- **Spring Boot:** 3.x
- **Spring Cloud**
- **Gradle:** 8.x
- **Database:** PostgreSQL
- **Scheduling:** Quartz
- **Messaging:** RabbitMQ
- **Database Migration:** Liquibase
- **Mapping:** MapStruct
- **Code Generation:** Lombok
- **Containerization:** Docker, Docker Compose
- **Testing:** Spock controller and service tests

## Prerequisites

- JDK 21
- Docker
- Docker Compose

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Emin0v/ms-scheduler.git
cd ms-scheduler
```

### 2. Running the application

Start PostgreSQL and RabbitMQ with Docker Compose:

```bash
docker compose -f infra/docker-compose.yml up -d db_scheduler rabbitmq
```

Compose starts the infrastructure. Run the application separately with the local profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

The local profile uses the Compose services and serves the API at `http://localhost:8085`.
The default profile expects database and RabbitMQ environment variables and uses port 80.

The messaging configuration uses the `ns.scheduler.dx` direct exchange with automatic exchange declaration disabled.
Create that exchange and bind a consumer queue with the intended routing key before testing message delivery.
`responseQueueInfo` is carried in the scheduled event; it does not provision broker infrastructure.

## API Endpoints

The base URL for the API is `/api/schedule-jobs`.

| Method   | Endpoint          | Description                                | Request Body        |
| -------- | ----------------- | ------------------------------------------ | ------------------- |
| `GET`    | `/`               | Get all scheduled jobs                     | -                   |
| `GET`    | `/running`        | Get all currently running jobs             | -                   |
| `POST`   | `/`               | Create or update a job                     | `ScheduleRequest`   |
| `POST`   | `/action`         | Pause, resume a job                        | `JobActionRequest`  |
| `DELETE` | `?jobName&jobGroup` | Delete a job by its name and group         | -                   |

### Request Bodies

#### `ScheduleRequest`

```json
{
  "jobName": "my-job",
  "jobGroup": "my-group",
  "cronExpression": "0/10 * * * * ?",
  "responseQueueInfo": {
    "exchangeName": "example.responses",
    "routingKey": "example.response"
  },
  "payload": {
    "message": "Hello from the scheduler!"
  }
}
```

#### `JobActionRequest`

```json
{
  "jobName": "my-job",
  "jobGroup": "my-group",
  "actionType": "PAUSE"
}
```

**Possible values for `actionType`:** `RUN_ONCE`, `PAUSE`, `RESUME`

## Testing

To run the tests, execute the following command:

```bash
./gradlew test
```

This runs the committed Spock controller and service tests and generates a report in
`build/reports/tests/test/index.html`. The service tests mock Quartz; they do not establish
real database persistence, clustered scheduling, or RabbitMQ delivery guarantees.
