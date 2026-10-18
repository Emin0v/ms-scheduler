# Contributing

Use Java 21 and the included Gradle wrapper. Read the README for infrastructure setup and the current verification boundaries.

Before opening a pull request, run:

```bash
./gradlew check
```

Tests cover controller/service behavior with Quartz mocked, plus messaging handoff results. Changes to persistence, misfire behavior, or delivery must describe how they were checked against PostgreSQL, Quartz, or RabbitMQ; a successful handoff is not broker confirmation.

Keep a pull request focused on one problem. Explain the behavior before and after, add a regression test for a behavior change, and state the checks actually run and their limits. Discuss substantial API or architecture changes in an issue first.

For a bug report, include the commit, Java and infrastructure versions, minimal reproduction, expected behavior, actual behavior, and relevant sanitized logs. Report suspected vulnerabilities through [SECURITY.md](SECURITY.md).

