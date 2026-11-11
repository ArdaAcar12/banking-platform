# Banking Platform

Banking platform backend built with Spring Boot.

## Stack

- Java 21
- Spring Boot 4.1.2
- Apache Maven (Maven Wrapper)
- Spring Web MVC, Spring Data JPA, Hibernate, Flyway
- Spring Security, Validation
- Spring AMQP (RabbitMQ)
- Spring Boot Actuator, Mail, DevTools
- PostgreSQL (runtime), H2 (tests)
- Lombok

## Build and run

This project uses the [Maven Wrapper](https://maven.apache.org/wrapper/). No pre-installed Maven or IDE is required.

### Compile
```cmd
mvnw.cmd compile
```
or
```sh
./mvnw compile
```

### Run tests
```cmd
mvnw.cmd test
```
or
```sh
./mvnw test
```

### Run the application
```cmd
mvnw.cmd spring-boot:run
```
or
```sh
./mvnw spring-boot:run
```

## Configuration

Configuration lives in `src/main/resources/application.properties`. Environment-specific
secrets (database credentials, mail credentials, etc.) must be provided through a `.env`
file or environment variables and must never be committed. See `.gitignore`.

## IDE

The project is a standard Maven project and can be opened in **IntelliJ IDEA Free** (Community
Edition) as well as any other Java IDE. No Ultimate-only features or paid plugins are required.

## Version control

- `main` — stable, verified releases
- `develop` — integration branch
- `feature/*` — individual features
- `fix/*` — bug fixes
- `docs/*` — documentation

Commits follow [Conventional Commits](https://www.conventionalcommits.org/).
