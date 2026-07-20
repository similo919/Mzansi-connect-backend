# Mzansi-connect-backend

## Phase 1 Backend Foundation

### Standard API response

All future controllers should return responses using:

```java
ResponseEntity<ApiResponse<T>>
```

The shared response wrapper is located at:

```text
src/main/java/com/mzansiconnect/backend/response/ApiResponse.java
```

Example:

```java
return ResponseEntity.ok(
        ApiResponse.success(
                "Areas retrieved successfully",
                areas
        )
);
```

### Custom exceptions

Custom runtime exceptions are located in:

```text
src/main/java/com/mzansiconnect/backend/exception
```

Available exceptions:

- `BusinessValidationException`
- `DatabaseConnectionException`
- `DuplicateResourceException`
- `ResourceNotFoundException`
- `UnauthorizedOperationException`

### Global exception handling

`GlobalExceptionHandler` standardizes error responses across the API using `ApiResponse.failure(...)`.

Handled cases include:

- `400 Bad Request`
- `401 Unauthorized`
- `403 Forbidden`
- `404 Not Found`
- `409 Conflict`
- `503 Service Unavailable`
- Malformed request bodies
- Database integrity conflicts
- Database health check failures
- Validation errors from `MethodArgumentNotValidException`
- Validation errors from `ConstraintViolationException`
- Unexpected server errors as `500 Internal Server Error`

### Temporary security configuration

`SecurityConfig` disables CSRF, form login, and HTTP Basic authentication for Phase 1.

All endpoints are temporarily allowed with:

```java
authorize.anyRequest().permitAll()
```

CORS allows the frontend origin configured by:

```properties
app.cors.allowed-origin=http://localhost:5173
```

JWT authentication and role-based authorization will be added in a later security phase.

### Health service

`HealthService` provides application and database health data.

Health endpoints:

- `GET /api/health`
- `GET /api/health/database`

Application health returns:

```text
application
status
```

Database health executes this read-only query:

```sql
SELECT DATABASE()
```

If the database check fails, `DatabaseConnectionException` is handled as `503 Service Unavailable` without exposing database credentials, SQL errors, or server internals.

### Verification

Compile check:

```bash
./mvnw compile
```

Run the `ApiResponse` unit tests:

```bash
./mvnw -Dtest=ApiResponseTest test
```

Run the health controller MVC tests:

```bash
./mvnw -Dtest=HealthControllerTest test
```

Run the health service unit tests:

```bash
./mvnw -Dtest=HealthServiceTest test
```

Run the full focused test suite:

```bash
./mvnw test
```
