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
- **Testing:** Spock, ArchUnit, Jacoco

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

There are two ways to run the application:

#### Using Docker Compose (recommended)

This is the easiest way to get the application and its dependencies (PostgreSQL, RabbitMQ) up and running.

```bash
docker-compose -f infra/docker-compose.yml up -d
```

The application will be available at `http://localhost:8085`.

#### Using Gradle

If you prefer to run the application without Docker, you can use the Gradle wrapper. You will need to have PostgreSQL and RabbitMQ running separately.

```bash
./gradlew bootRun
```

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
  "data": {
    "message": "Hello from the scheduler!"
  }
}
```

#### `JobActionRequest`

```json
{
  "jobName": "my-job",
  "jobGroup": "my-group",
  "action": "PAUSE"
}
```

**Possible values for `action`:** `RUN_ONCE`, `PAUSE`, `RESUME`

## Testing

To run the tests, execute the following command:

```bash
./gradlew test
```

This will run all the unit and integration tests and generate a test report in `build/reports/tests/test/index.html`.