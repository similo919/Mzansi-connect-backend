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
- `RankAssignmentNotFoundException`
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
- Type mismatch errors such as non-numeric path IDs
- Method-level validation errors such as non-positive IDs
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

### Database enums

The `enums` package contains Java enums that must match the MySQL `ENUM` values exactly:

- `AreaType`
- `RankType`
- `AssignmentType`

### Database entities

The `entity` package contains JPA mappings for the core transport data model:

- `Area` maps to `areas`
- `TaxiRank` maps to `taxi_ranks`
- `AreaRankAssignment` maps to `area_rank_assignments`

Important mapping rules:

- MySQL `ENUM` columns use Java enums with `EnumType.STRING`.
- MySQL `decimal(10,7)` latitude and longitude columns use `BigDecimal`, not `double`.
- MySQL `datetime` columns use `LocalDateTime`.
- MySQL `tinyint(1)` style flags use `Boolean`.
- Area and rank relationships use `@ManyToOne(fetch = FetchType.LAZY)`.
- Cascade delete/update is not used on assignment relationships.
- Reverse collections are intentionally not added yet to avoid recursive loading.

### Repositories

The `repository` package contains Spring Data JPA repositories for CRUD, pagination, filtering, duplicate checks, and future rank inheritance lookups:

- `AreaRepository`
- `TaxiRankRepository`
- `AreaRankAssignmentRepository`

Repository conventions:

- Active-record lookups use `findBy...AndActiveTrue`.
- Paginated list/search methods return `Page<T>` and accept `Pageable`.
- Duplicate checks use `existsBy...`.
- `AreaRepository` has separate duplicate checks for root areas where `parent_area_id` is `NULL` and child areas with a parent ID.
- Assignment lookups are ordered by `priority` for later rank resolution.
- `@EntityGraph` is used where related `Area` or `TaxiRank` data is expected with the result.

### Area DTOs

The `dto.area` package contains request and response DTOs for Area APIs:

- `AreaCreateRequest`
- `AreaUpdateRequest`
- `AreaSummaryResponse`
- `AreaResponse`

Create and update requests include validation annotations for required fields and length limits.

### Mappers

The `mapper` package contains DTO/entity mapping components:

- `AreaMapper`
- `TaxiRankMapper`
- `AreaRankAssignmentMapper`

`AreaMapper` normalizes request text, maps create/update Area requests into entities, and maps Area entities into API response DTOs with a parent-area summary.

`TaxiRankMapper` maps taxi-rank create/update requests, normalizes optional text, handles default boolean values, tracks local verification timestamps, and returns rank responses with located-area summaries.

`AreaRankAssignmentMapper` maps assignment create/update requests, normalizes notes and reasons, tracks local verification timestamps, and returns assignment responses with area and taxi-rank summaries.

### Area service

`AreaService` and `AreaServiceImpl` provide Area business logic:

- Create active areas.
- Retrieve active areas by ID.
- List active areas with search, type filter, parent filter, pagination, and safe sorting.
- Retrieve child areas by parent ID.
- Update area details and parent relationships.
- Soft-deactivate areas.

Business rules enforced:

- Duplicate area names are blocked under the same parent.
- Root area duplicate checks handle `parent_area_id = NULL` separately.
- `REGION` areas cannot have a parent.
- `ZONE` and `EXTENSION` areas must have a `SUBURB` parent.
- Area hierarchy cycles are blocked.
- Areas with active child areas, taxi ranks, or taxi-rank assignments cannot be deactivated.

### Area API

`AreaController` exposes Area endpoints under `/api/areas`:

- `POST /api/areas`
- `GET /api/areas`
- `GET /api/areas/{id}`
- `GET /api/areas/{id}/children`
- `PUT /api/areas/{id}`
- `DELETE /api/areas/{id}`

List requests support optional `search`, `areaType`, `parentAreaId`, `page`, `size`, `sortBy`, and `sortDirection` query parameters.

### Taxi Rank DTOs

The `dto.rank` package contains request and response DTOs for Taxi Rank APIs:

- `ResolvedTaxiRankResponse`
- `TaxiRankCreateRequest`
- `TaxiRankUpdateRequest`
- `TaxiRankSummaryResponse`
- `TaxiRankResponse`

Create and update requests include validation for required fields, rank type, located area ID, text length limits, and latitude/longitude decimal ranges.

### Taxi Rank Service

`TaxiRankService` and `TaxiRankServiceImpl` provide Taxi Rank business logic:

- Create active taxi ranks.
- Retrieve active taxi ranks by ID.
- List active taxi ranks with search, rank type, located area, formal-rank, local-verification, pagination, and safe sorting filters.
- Update taxi-rank details and located area.
- Soft-deactivate taxi ranks.

Business rules enforced:

- Duplicate taxi-rank names are blocked within the same located area.
- Taxi ranks may only be attached to `SUBURB`, `TOWN`, or `CITY` areas.
- Taxi ranks with active area-rank assignments cannot be deactivated.

### Taxi Rank Resolution Service

`TaxiRankResolutionService` and `TaxiRankResolutionServiceImpl` resolve the best active taxi rank for a selected area.

Resolution behavior:

- Start with the selected area.
- Load active assignments ordered by priority.
- Ignore assignments whose taxi rank is inactive.
- If no active assignment exists, move to the parent area.
- Continue up the parent hierarchy until a match is found.
- Stop and return `404 Not Found` through `RankAssignmentNotFoundException` when no assignment exists.
- Detect hierarchy loops and excessive hierarchy depth.

Assignment selection order:

1. Lowest `priority`
2. Assignment type order: `PRIMARY`, `SECONDARY`, `INHERITED`, `NEAREST_MALL_FALLBACK`, `NEAREST_MAJOR_FALLBACK`
3. Lowest assignment ID

The response includes both:

- `selectedArea`: the area selected by the user.
- `matchedArea`: the area where the assignment was found.

### Taxi Rank API

`TaxiRankController` exposes Taxi Rank endpoints under `/api/taxi-ranks`:

- `POST /api/taxi-ranks`
- `GET /api/taxi-ranks`
- `GET /api/taxi-ranks/resolve?areaId={areaId}`
- `GET /api/taxi-ranks/{id}`
- `PUT /api/taxi-ranks/{id}`
- `DELETE /api/taxi-ranks/{id}`

List requests support optional `search`, `rankType`, `locatedInAreaId`, `formalRank`, `locallyVerified`, `page`, `size`, `sortBy`, and `sortDirection` query parameters.

### Area Rank Assignment DTOs

The `dto.assignment` package contains request and response DTOs for Area Rank Assignment APIs:

- `AreaRankAssignmentCreateRequest`
- `AreaRankAssignmentUpdateRequest`
- `AreaRankAssignmentResponse`

Create and update requests include validation for required area ID, taxi rank ID, assignment type, priority range, and text length limits.

### Area Rank Assignment Service

`AreaRankAssignmentService` and `AreaRankAssignmentServiceImpl` provide assignment business logic:

- Create active area-rank assignments.
- Retrieve active assignments by ID.
- List active assignments with area, taxi rank, assignment type, local-verification, pagination, and safe sorting filters.
- Update assignment area, taxi rank, type, priority, notes, reason, and local verification state.
- Soft-deactivate assignments.

Business rules enforced:

- Duplicate active area-to-rank assignments are blocked.
- `NEAREST_MALL_FALLBACK` must reference a `MALL_RANK`.
- `NEAREST_MAJOR_FALLBACK` must reference a `MAJOR_RANK` or `HOSPITAL_RANK`.
- `INHERITED` assignments may only be created for `ZONE` or `EXTENSION` areas.
- Inherited assignments require the selected rank to be actively assigned to the parent township.
- Assignment reason is required for inherited and fallback assignments.

### Area Rank Assignment API

`AreaRankAssignmentController` exposes assignment endpoints under `/api/area-rank-assignments`:

- `POST /api/area-rank-assignments`
- `GET /api/area-rank-assignments`
- `GET /api/area-rank-assignments/{id}`
- `PUT /api/area-rank-assignments/{id}`
- `DELETE /api/area-rank-assignments/{id}`

List requests support optional `areaId`, `taxiRankId`, `assignmentType`, `locallyVerified`, `page`, `size`, `sortBy`, and `sortDirection` query parameters.

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

Run the area service unit tests:

```bash
./mvnw -Dtest=AreaServiceImplTest test
```

Run the taxi-rank service unit tests:

```bash
./mvnw -Dtest=TaxiRankServiceImplTest test
```

Run the area-rank assignment service unit tests:

```bash
./mvnw -Dtest=AreaRankAssignmentServiceImplTest test
```

Run the taxi-rank resolution service unit tests:

```bash
./mvnw -Dtest=TaxiRankResolutionServiceTest test
```

Run the area controller validation MVC test:

```bash
./mvnw -Dtest=AreaControllerValidationTest test
```

Run the full focused test suite:

```bash
./mvnw test
```

Current focused suite status:

```text
30 tests run, 0 failures, 0 errors
```
