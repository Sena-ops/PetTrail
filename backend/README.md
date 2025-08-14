# PatTrail Backend

A Spring Boot application for pet walk tracking with GPS coordinates.

## Features

- **Pet Management**: Create and manage pets with species, age, and race information
- **Walk Management**: Start walks for pets with automatic active walk validation
- **GPS Tracking**: Upload walk points in batch with outlier detection and validation
- **OpenStreetMap Integration**: WGS84 coordinates compatible with OpenStreetMap tiles

## API Endpoints

### Pets
- `POST /api/pets` - Create a new pet
- `GET /api/pets` - List all pets
- `GET /api/pets/{id}` - Get pet by ID
- `PUT /api/pets/{id}` - Update pet information

### Walks
- `POST /api/walks/start?petId={id}` - Start a walk for a pet
- `POST /api/walks/{id}/points` - Upload GPS points for a walk

### Walk Points Endpoint

The walk points endpoint (`POST /api/walks/{id}/points`) allows uploading GPS coordinates in batch:

#### Request Format
```json
[
  {
    "lat": -23.5505,
    "lon": -46.6333,
    "ts": "2025-08-14T22:00:00Z",
    "elev": 760.2
  }
]
```

#### Features
- **WGS84 Coordinates**: Compatible with OpenStreetMap and Leaflet.js
- **Batch Processing**: Up to 5000 points per request
- **Outlier Detection**: Automatically discards points with speed > 50 m/s
- **Timestamp Validation**: Sorts points by timestamp and discards non-increasing timestamps
- **Haversine Distance**: Accurate distance calculation using spherical trigonometry

#### Response Format
```json
{
  "received": 100,
  "accepted": 95,
  "discarded": 5
}
```

## Database Schema

The application uses H2 database with the following tables:
- `pets` - Pet information
- `walks` - Walk sessions
- `walk_points` - GPS coordinates for walks

## Running the Application

1. **Prerequisites**: Java 21, Maven
2. **Build**: `./mvnw clean compile`
3. **Run**: `./mvnw spring-boot:run`
4. **Access**: http://localhost:8080

## API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs

## Testing

Run the test suite:
```bash
./mvnw test
```

## Documentation

- [Walk Points Endpoint Documentation](WALK_POINTS_ENDPOINT_README.md)
- [Walk Endpoint Documentation](WALK_ENDPOINT_README.md)
- [OpenAPI Documentation](OPENAPI_README.md)
