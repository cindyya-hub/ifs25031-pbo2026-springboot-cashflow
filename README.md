# username-pbo2026-springboot-cashflow

Aplikasi **Cashflow (Catatan Keuangan)** — Spring Boot + Vaadin, Clean Architecture, Java Generics, Java Exceptions.

## Jalankan

```
.\mvnw.cmd spring-boot:run
```

Buka http://localhost:8080 (ubah port lewat `APP_PORT` di `.env`).

## Build JAR (wajib profil production)

```
.\mvnw.cmd clean package -Pproduction
java -jar target\username-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```

## Test

```
.\mvnw.cmd clean verify
```
