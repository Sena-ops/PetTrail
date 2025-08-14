# Walk Start Endpoint - US01-02

This document describes the implementation of the walk start endpoint for the pet walking application.

## Endpoint

**POST** `/api/walks/start?petId={id}`

## Description

Starts a new walk for a specified pet. Only one active walk per pet is allowed.

## Business Rules

- Pet must exist (404 if not found)
- Only one active walk per pet allowed (409 if active walk exists)
- Server time is used for walk start time (ignores client timestamps)
- Walk is created with `finishedAt = null`

## Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| petId | Long | Yes | ID of the pet to start a walk for |

## Response

### Success (200 OK)
```json
{
  "walkId": 101,
  "startedAt": "2025-08-14T22:15:30Z"
}
```

### Error Responses

#### 400 Bad Request - Invalid petId
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid query parameter.",
  "details": [
    {
      "field": "petId",
      "issue": "required numeric id"
    }
  ]
}
```

#### 404 Not Found - Pet not found
```json
{
  "code": "NOT_FOUND",
  "message": "pet not found",
  "details": [
    {
      "field": "petId",
      "issue": "unknown"
    }
  ]
}
```

#### 409 Conflict - Active walk exists
```json
{
  "code": "CONFLICT",
  "message": "caminhada ativa já existe",
  "details": []
}
```

## How to Run Locally

1. **Start the application:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Access Swagger UI:**
   - Open browser and go to: `http://localhost:8080/api-docs.html`
   - Navigate to the "Walks" section
   - Test the `/api/walks/start` endpoint

## Testing Examples

### Using curl

1. **Start a walk (success):**
   ```bash
   curl -i -X POST "http://localhost:8080/api/walks/start?petId=1"
   ```

2. **Pet not found:**
   ```bash
   curl -i -X POST "http://localhost:8080/api/walks/start?petId=99999"
   ```

3. **Conflict (already active walk):**
   ```bash
   curl -i -X POST "http://localhost:8080/api/walks/start?petId=1"
   ```

4. **Invalid petId:**
   ```bash
   curl -i -X POST "http://localhost:8080/api/walks/start?petId=abc"
   ```

### Using Swagger UI

1. Open `http://localhost:8080/api-docs.html`
2. Find the "Walks" section
3. Click on `POST /api/walks/start`
4. Click "Try it out"
5. Enter a petId value
6. Click "Execute"

## Database Schema

The walks table has the following structure:

```sql
CREATE TABLE walks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pet_id INTEGER NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP NULL,
    FOREIGN KEY (pet_id) REFERENCES pets(id)
);

-- Unique index to ensure only one active walk per pet
CREATE UNIQUE INDEX idx_walks_active_pet 
ON walks (pet_id) 
WHERE finished_at IS NULL;
```

## Implementation Details

- **Model**: `Walk.java` - JPA entity with validation
- **Repository**: `WalkRepository.java` - Custom queries for active walks
- **Service**: `WalkService.java` - Business logic with transaction support
- **Controller**: `WalkController.java` - REST endpoint with OpenAPI documentation
- **DTOs**: `StartWalkResponse.java` - Response structure
- **Exceptions**: Custom exceptions for 404 and 409 scenarios
- **Error Handling**: Updated `GlobalExceptionHandler.java` for all error cases

## Notes

- The application uses server time (UTC) for all timestamps
- Database constraint ensures only one active walk per pet
- All responses are in JSON format
- Error responses follow the project's standard format
- OpenAPI documentation is automatically generated
