# Walk Points Batch Upload Endpoint

## Overview

The `POST /api/walks/{id}/points` endpoint allows uploading GPS coordinates for a walk in batch. The endpoint processes points through validation, sorting, and outlier detection before storing them in the database.

## Coordinates Format

All coordinates must be in **WGS84 lat/lon degrees**, compatible with:
- OpenStreetMap tile API: `https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png`
- Leaflet.js frontend mapping library
- Standard GPS coordinate systems

**Important**: Do not use Google Maps or other paid location APIs. This endpoint is designed for OpenStreetMap visualization.

## Endpoint Details

### URL
```
POST /api/walks/{id}/points
```

### Path Parameters
- `{id}` - Walk ID (numeric, required)

### Request Body
JSON array of point objects (1-5000 points):

```json
[
  {
    "lat": -23.5505,
    "lon": -46.6333,
    "ts": "2025-08-14T22:00:00Z",
    "elev": 760.2
  },
  {
    "lat": -23.5510,
    "lon": -46.6339,
    "ts": "2025-08-14T22:00:10Z"
  }
]
```

### Field Validation

| Field | Type | Required | Range/Format | Description |
|-------|------|----------|--------------|-------------|
| `lat` | number | Yes | [-90, 90] | Latitude in WGS84 degrees |
| `lon` | number | Yes | [-180, 180] | Longitude in WGS84 degrees |
| `ts` | string | Yes | ISO-8601 | Timestamp (UTC Z or with offset) |
| `elev` | number | No | ≥ 0 | Elevation in meters |

### Processing Rules

1. **Sorting**: Points are sorted by timestamp (`ts`) in ascending order
2. **Outlier Detection**: For consecutive points A→B:
   - Calculate Haversine distance between A and B (meters)
   - Calculate time difference Δt = ts(B) - ts(A) (seconds)
   - If speed = distance/Δt > 50 m/s, discard point B
   - If Δt ≤ 0, discard point B (non-increasing timestamp)

### Response

#### Success (202 Accepted)
```json
{
  "received": 100,
  "accepted": 95,
  "discarded": 5
}
```

#### Error Responses

**400 Bad Request** - Invalid payload
```json
{
  "code": "BAD_REQUEST",
  "message": "Payload must have 1..5000 points.",
  "details": []
}
```

**404 Not Found** - Walk doesn't exist
```json
{
  "code": "NOT_FOUND",
  "message": "walk not found",
  "details": []
}
```

**409 Conflict** - Walk already finished
```json
{
  "code": "CONFLICT",
  "message": "walk already finished",
  "details": []
}
```

## Examples

### Valid Request
```bash
curl -i -X POST "http://localhost:8080/api/walks/123/points" \
  -H "Content-Type: application/json" \
  -d '[
    {"lat": -23.5505, "lon": -46.6333, "ts": "2025-08-14T22:00:00Z"},
    {"lat": -23.5510, "lon": -46.6339, "ts": "2025-08-14T22:00:10Z", "elev": 760.2}
  ]'
```

**Response:**
```json
{
  "received": 2,
  "accepted": 2,
  "discarded": 0
}
```

### Outlier Detection Example
```bash
curl -i -X POST "http://localhost:8080/api/walks/123/points" \
  -H "Content-Type: application/json" \
  -d '[
    {"lat": -23.5505, "lon": -46.6333, "ts": "2025-08-14T22:00:00Z"},
    {"lat": -23.5600, "lon": -46.6400, "ts": "2025-08-14T22:00:01Z"}
  ]'
```

**Response:**
```json
{
  "received": 2,
  "accepted": 1,
  "discarded": 1
}
```
*Note: Second point discarded due to speed > 50 m/s*

## Implementation Notes

- **Haversine Distance**: Uses spherical trigonometry for accurate distance calculation
- **Speed Threshold**: 50 m/s (180 km/h) to filter GPS outliers
- **Bulk Insert**: Points are saved in batch for performance
- **Logging**: Discarded points and reasons are logged for monitoring
- **Transaction**: All operations are wrapped in a database transaction

## Database Schema

Points are stored in the `walk_points` table:

```sql
CREATE TABLE walk_points (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    walk_id INTEGER NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    elevation DECIMAL(8, 2) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (walk_id) REFERENCES walks(id) ON DELETE CASCADE
);
```

## Frontend Integration

For OpenStreetMap visualization with Leaflet.js:

```javascript
// Example: Display walk points on OpenStreetMap
const map = L.map('map').setView([-23.5505, -46.6333], 13);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

// Add walk points as markers or polyline
walkPoints.forEach(point => {
    L.marker([point.latitude, point.longitude]).addTo(map);
});
```
