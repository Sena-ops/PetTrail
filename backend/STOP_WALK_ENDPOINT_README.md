# Stop Walk Endpoint Documentation

## Overview

The `POST /api/walks/{id}/stop` endpoint stops an active walk, computes consolidated metrics using WGS84 coordinates (OpenStreetMap compatible), and persists the results. The operation is idempotent - subsequent calls return 409 if the walk is already finished.

## Endpoint Details

- **URL**: `POST /api/walks/{id}/stop`
- **Path Parameter**: `id` (required) - The walk ID to stop
- **Response**: JSON with consolidated metrics

## Response Format

### Success (200 OK)

```json
{
  "walkId": 123,
  "distanciaM": 2450.7,
  "duracaoS": 1560,
  "velMediaKmh": 5.65,
  "startedAt": "2025-08-13T23:15:00Z",
  "finishedAt": "2025-08-13T23:41:00Z"
}
```

### Error Responses

#### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "walk not found",
  "details": [{"field": "id", "issue": "unknown"}]
}
```

#### 409 Conflict (Already Finished)
```json
{
  "code": "CONFLICT",
  "message": "walk already finished",
  "details": []
}
```

## Technical Implementation

### Geo Calculations

All distance calculations use **WGS84 lat/lon coordinates** in degrees, compatible with:
- OpenStreetMap tile service
- Leaflet mapping library
- No external map/geocoding APIs required

### Distance Calculation

- Uses **Haversine formula** for accurate spherical distance calculation
- Sums distances between consecutive accepted points (after outlier filtering)
- Returns distance in **meters**
- If fewer than 2 points exist: `distanciaM = 0.0`

### Duration Calculation

- `duracaoS = finishedAt - startedAt` in **seconds**
- Uses server clock for `finishedAt` timestamp

### Average Speed Calculation

- `velMediaKmh = (distanciaM / 1000) / (duracaoS / 3600)`
- Rounded to **2 decimal places** (half-up rounding)
- If `duracaoS = 0`: `velMediaKmh = 0.00`

### Outlier Filtering

Points are filtered using the same rules as US01-03:
- Instantaneous speed > 50 m/s → discarded
- Non-increasing timestamps → discarded

### Idempotency

- First successful stop: computes and persists metrics
- Subsequent calls: returns 409 Conflict (no data changes)
- Metrics remain unchanged after first consolidation

## Example Usage

### Stop a Walk

```bash
curl -i -X POST "http://localhost:8080/api/walks/123/stop"
```

**Response:**
```json
{
  "walkId": 123,
  "distanciaM": 2450.7,
  "duracaoS": 1560,
  "velMediaKmh": 5.65,
  "startedAt": "2025-08-13T23:15:00Z",
  "finishedAt": "2025-08-13T23:41:00Z"
}
```

### Try to Stop Already Finished Walk

```bash
curl -i -X POST "http://localhost:8080/api/walks/123/stop"
```

**Response:**
```json
{
  "code": "CONFLICT",
  "message": "walk already finished",
  "details": []
}
```

## Database Schema

The `walks` table includes new metrics columns:

```sql
ALTER TABLE walks ADD COLUMN distancia_m DOUBLE DEFAULT NULL;
ALTER TABLE walks ADD COLUMN duracao_s INTEGER DEFAULT NULL;
ALTER TABLE walks ADD COLUMN vel_media_kmh DOUBLE DEFAULT NULL;
```

## Business Rules

1. **Walk not found** → 404 Not Found
2. **Walk already finished** → 409 Conflict
3. **Idempotent operation** → No side effects on repeat calls
4. **WGS84 coordinates** → OpenStreetMap/Leaflet compatible
5. **No external APIs** → All calculations performed server-side

## Logging

The service logs diagnostic information:
- Walk ID
- Number of points used
- Total distance calculated
- Duration and average speed
- Example: `Walk 123 stopped: distance=2450.7m, duration=1560s, avg_speed=5.65km/h`
