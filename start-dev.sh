#!/bin/bash
# Development startup script for banking-platform
# Loads environment variables from .env and starts the application via Maven

# Read .env file and set environment variables
if [ -f .env ]; then
    set -a
    source .env
    set +a
fi

# Set Spring Boot profile
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-default}"

# Print configuration for verification
echo "=========================================="
echo "Banking Platform Development Startup"
echo "=========================================="
echo
echo "Checking environment variables..."
echo "SPRING_DATASOURCE_URL: $SPRING_DATASOURCE_URL"
echo "SPRING_DATASOURCE_USERNAME: $SPRING_DATASOURCE_USERNAME"
echo "SPRING_DATASOURCE_PASSWORD: $SPRING_DATASOURCE_PASSWORD"
echo
echo "Using application profile: $SPRING_PROFILES_ACTIVE"
echo
echo "Starting application with Maven..."
echo

# Start the Spring Boot application
./mvnw spring-boot:run -Dr.spring.profiles.active="$SPRING_PROFILES_ACTIVE"

echo
echo "Application shutdown complete."