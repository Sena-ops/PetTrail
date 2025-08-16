# PatTrail PWA - Offline Support Documentation

## Overview

PatTrail is a Progressive Web App (PWA) that provides GPS tracking for pet walks with robust offline support. The app continues capturing GPS points even without internet connectivity and reliably uploads them once reconnected.

## Offline Functionality

### GPS Point Capture
- **Continuous Recording**: The app continues capturing GPS points via `watchPosition` even when offline
- **Local Storage**: Points are stored in IndexedDB, grouped by `walkId`
- **Data Format**: Points follow the API format: `{ lat: number, lon: number, ts: ISO-8601, elev?: number }`
- **Validation**: Invalid coordinates are filtered out before storage

### Batching System
- **Buffer Size**: Points are collected in a RAM buffer (default: 10 points)
- **Flush Interval**: Buffer is flushed every 5 seconds or when full
- **Persistent Queue**: Each batch is written to IndexedDB before any network attempt
- **Reliable Upload**: Batches are only removed from IndexedDB after successful 2xx response

### Reconnection Behavior
- **Automatic Sync**: Pending batches are automatically sent when internet is restored
- **Retry Logic**: Failed uploads use exponential backoff (2^retry * 5s, max 60s)
- **Error Handling**: 
  - 2xx responses: Remove batch from queue
  - 5xx/Network errors: Retry with backoff
  - 404/409 (walk not found/finished): Stop retrying, show warning
  - 400 (malformed data): Drop batch, log error

## Visual Indicators

### Status Badges
The app displays real-time status indicators in the top-right corner:

- **Recording** (●): GPS tracking is active
- **Offline** (⚠): No internet connection detected
- **Syncing** (↻): Uploading queued batches to server

### UI Elements
- **Last Synced**: Shows timestamp of last successful upload
- **Queued Count**: Displays number of pending batches
- **Points Count**: Shows current buffer size
- **Recording Time**: Elapsed time for current walk

## App Restart Safety

### State Persistence
- **Walk State**: Current walk ID, start time, and recording status are persisted in IndexedDB
- **Automatic Resume**: App automatically resumes recording if it was active when closed
- **Queue Preservation**: All pending batches remain intact across app restarts

### Recovery Process
1. On startup, app checks for persisted walk state
2. If active walk found, restores recording state
3. Resumes GPS tracking and timers
4. Attempts to drain any pending batches
5. Updates all UI indicators

## Map Behavior

### OpenStreetMap Integration
- **Tile Source**: Uses OpenStreetMap tiles via Leaflet
- **No Pre-caching**: Service Worker does NOT cache OSM tiles
- **Offline Maps**: Map tiles may be unavailable when offline
- **Recording Continues**: GPS point capture works regardless of map tile availability

### Tile Policy
- **Network Only**: OSM tiles are fetched directly from network
- **No Storage**: Tiles are never stored in cache
- **Graceful Degradation**: App functions normally even with missing map tiles

## Technical Implementation

### IndexedDB Schema
```javascript
// Batches store
{
  id: auto-increment,
  walkId: number,
  points: array,
  retryCount: number,
  nextAttemptAt: timestamp,
  createdAt: timestamp
}

// Metadata store
{
  key: string,
  value: any,
  updatedAt: timestamp
}
```

### Key Methods
- `saveCurrentWalk(walkId, startedAt)`: Persist walk state
- `getCurrentWalk()`: Restore walk state on startup
- `addBatch(batch)`: Queue points for upload
- `drainQueue()`: Process pending batches
- `updateLastSynced()`: Track sync timestamps

### Event Handling
- `online/offline`: Trigger queue drain and update indicators
- `visibilitychange`: Flush buffer when app goes to background
- `beforeunload`: Attempt final upload via sendBeacon

## Error Handling

### GPS Errors
- **Permission Denied**: Show clear error message
- **Position Unavailable**: Continue attempting to drain queue
- **Timeout**: Retry with current settings

### Network Errors
- **Connection Lost**: Queue data, show offline indicator
- **Server Errors**: Retry with exponential backoff
- **Client Errors**: Stop retrying, show appropriate warning

### Data Validation
- **Coordinate Validation**: Filter invalid lat/lon values
- **Timestamp Validation**: Ensure ISO-8601 format
- **Duplicate Prevention**: Backend handles deduplication by (walkId, ts)

## Performance Considerations

### Memory Management
- **Buffer Limits**: RAM buffer limited to 10 points
- **Batch Size**: Maximum 5000 points per batch (backend limit)
- **Queue Monitoring**: Regular cleanup of old/failed batches

### Battery Optimization
- **Efficient GPS**: Uses `enableHighAccuracy: true` with reasonable timeouts
- **Background Sync**: Leverages Service Worker for background uploads
- **Smart Flushing**: Balances data freshness with battery life

## Usage Scenarios

### Normal Operation
1. User starts walk recording
2. GPS points captured and buffered
3. Batches sent to server every 5 seconds
4. Visual indicators show current status

### Offline Recording
1. Internet connection lost during walk
2. Recording continues, points stored locally
3. Offline indicator appears
4. Data queued for later upload

### Reconnection
1. Internet restored
2. Syncing indicator appears
3. Queued batches uploaded automatically
4. Last synced timestamp updated

### App Restart
1. User closes app mid-walk
2. State persisted to IndexedDB
3. On reopen, recording resumes automatically
4. Pending data uploaded if online

## Troubleshooting

### Common Issues
- **GPS Not Working**: Check location permissions in browser
- **Points Not Uploading**: Verify internet connection and server status
- **App Not Resuming**: Clear browser data and restart
- **Map Not Loading**: Normal when offline, tiles load when online

### Debug Information
- Open browser console for detailed logs
- Check IndexedDB storage in DevTools
- Monitor network requests in Network tab
- Verify Service Worker registration

## Browser Compatibility

### Required Features
- **IndexedDB**: For persistent storage
- **Geolocation API**: For GPS tracking
- **Service Workers**: For offline functionality
- **Fetch API**: For network requests

### Supported Browsers
- Chrome 40+
- Firefox 44+
- Safari 11.1+
- Edge 17+

## Security Considerations

### Data Privacy
- **Local Storage**: GPS data stored locally until uploaded
- **No External Tracking**: No analytics or tracking services
- **Secure Uploads**: HTTPS required for all API calls

### Permissions
- **Location Access**: Required for GPS tracking
- **Storage Access**: Required for offline functionality
- **Network Access**: Required for data upload

---

For technical support or feature requests, please refer to the main project documentation.
