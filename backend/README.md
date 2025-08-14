# Pets API (CRUD)

A complete REST API for managing pets with full CRUD operations, validation, and comprehensive documentation.

## Features

- **POST /api/pets** - Create a new pet with name, species, age, and race validation
- **GET /api/pets** - List all pets
- **GET /api/pets/{id}** - Get a specific pet by ID
- **PUT /api/pets/{id}** - Update a pet's information
- **DELETE /api/pets/{id}** - Delete a pet
- Standard JSON error format for validation errors
- OpenAPI/Swagger documentation
- Comprehensive HTML documentation
- H2 database with automatic schema updates

## Requirements

- Java 21
- Maven 3.6+

## Running Locally

1. **Clone the repository and navigate to the backend directory:**
   ```bash
   cd backend
   ```

2. **Build the project:**
   ```bash
   mvn clean install
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the API:**
   - API Base URL: `http://localhost:8080`
   - **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
   - **API Documentation**: `http://localhost:8080/api-docs.html`
   - **H2 Console**: `http://localhost:8080/h2-console`
   - OpenAPI JSON: `http://localhost:8080/v3/api-docs`
   - API Endpoints: `http://localhost:8080/api/pets`

## API Endpoints

### Create Pet
```bash
curl -i -X POST http://localhost:8080/api/pets \
  -H "Content-Type: application/json" \
  -d '{ "name": "Rex", "species": "CACHORRO", "age": 5, "race": "Golden Retriever" }'
```

### List All Pets
```bash
curl -i -X GET http://localhost:8080/api/pets
```

### Get Pet by ID
```bash
curl -i -X GET http://localhost:8080/api/pets/1
```

### Update Pet
```bash
curl -i -X PUT http://localhost:8080/api/pets/1 \
  -H "Content-Type: application/json" \
  -d '{ "name": "Rex Updated", "age": 6 }'
```

### Delete Pet
```bash
curl -i -X DELETE http://localhost:8080/api/pets/1
```

## Validation Rules

- **name**: Required, 1-60 characters
- **species**: Required, must be either "CACHORRO" or "GATO"
- **age**: Required, must be between 0 and 30 years
- **race**: Required, 1-50 characters

## Error Response Format

All validation errors return a standard JSON format:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "One or more validation errors occurred.",
  "details": [
    { "field": "name", "issue": "must not be blank" }
  ]
}
```

## Database

- H2 database file: `./data/app.mv.db`
- Schema is automatically created/updated on startup
- H2 console available at `http://localhost:8080/h2-console`
- No manual database setup required

## Notes

- Duplicate pet names are permitted in this MVP
- All responses use `application/json` content type
- The API includes comprehensive OpenAPI documentation with examples
- Update operations allow partial updates (only provided fields are updated)

## Testing

The API has been tested and verified to work with the following scenarios:

✅ **Valid pet creation**: `{"name": "Rex", "species": "CACHORRO", "age": 5, "race": "Golden Retriever"}` returns 201  
✅ **List pets**: GET `/api/pets` returns 200 with array of pets  
✅ **Get pet by ID**: GET `/api/pets/{id}` returns 200 with pet details  
✅ **Update pet**: PUT `/api/pets/{id}` returns 200 with updated pet  
✅ **Delete pet**: DELETE `/api/pets/{id}` returns 204  
✅ **Validation - blank name**: Returns 400 with error details  
✅ **Validation - invalid species**: Returns 400 with error details  
✅ **Validation - invalid age**: Returns 400 with error details  
✅ **Validation - invalid race**: Returns 400 with error details  
✅ **Duplicate names allowed**: Multiple pets with same name can be created  
✅ **Swagger UI accessible**: Available at `http://localhost:8080/swagger-ui/index.html`
✅ **HTML Documentation accessible**: Available at `http://localhost:8080/api-docs.html`
✅ **H2 Console accessible**: Available at `http://localhost:8080/h2-console`

## Documentation

### Swagger UI
For interactive API documentation with testing capabilities, visit:
**http://localhost:8080/swagger-ui/index.html**

### HTML Documentation
For complete API documentation with examples, validation rules, and error responses, visit:
**http://localhost:8080/api-docs.html**

### H2 Console
For database management and inspection, visit:
**http://localhost:8080/h2-console**

All documentation options include:
- Detailed endpoint descriptions
- Request/response examples
- Validation rules
- Error response formats
- Testing scenarios
