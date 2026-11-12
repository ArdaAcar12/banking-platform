@echo off
:: Development startup script for banking-platform
:: Sets required environment variables and starts the application via Maven

echo ==========================================
echo Banking Platform Development Startup
echo ==========================================
echo.

:: Set required environment variables
:: These must match the .env file values
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/banking_platform
set SPRING_DATASOURCE_USERNAME=banking
set SPRING_DATASOURCE_PASSWORD=changeme
set JWT_SECRET=change-this-to-a-strong-secret-12345

:: Set Spring Boot profile
set SPRING_PROFILES_ACTIVE=default

:: Print configuration for verification
echo Configuration loaded:
echo   SPRING_DATASOURCE_URL: %SPRING_DATASOURCE_URL%
echo   SPRING_DATASOURCE_USERNAME: %SPRING_DATASOURCE_USERNAME%
echo   SPRING_DATASOURCE_PASSWORD: %SPRING_DATASOURCE_PASSWORD%
echo   JWT_SECRET: %JWT_SECRET%
echo   SPRING_PROFILES_ACTIVE: %SPRING_PROFILES_ACTIVE%
echo.

:: Start the Spring Boot application
call mvnw spring-boot:run -Dr.spring.profiles.active=%SPRING_PROFILES_ACTIVE%

echo.
echo Application shutdown complete.
pause