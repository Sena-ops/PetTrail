# PatTrail PWA - GPS Recording Application

A Progressive Web App (PWA) for GPS walk recording with resilient data delivery to the backend.

## Features

### GPS Recording
- **High-accuracy GPS tracking** using `navigator.geolocation.watchPosition()`
- **WGS84 coordinates** compatible with OpenStreetMap tiles
- **Real-time map centering** on current location
- **Automatic coordinate validation** (clips out-of-range values)

### Batching & Queueing
- **In-memory buffer** for current walk (capacity: 10 points)
- **Persistent IndexedDB queue** for failed/unsent batches
- **Automatic flushing** when buffer reaches 10 points OR every 5 seconds
- **No data loss** during brief connection drops

### Resilient Delivery
- **Exponential backoff retry** (5s base, max 60s, with jitter)
- **Smart error handling**:
  - 409/404: Stop retrying (walk finished/not found)
  - 400: Drop batch (validation error)
  - 5xx/network: Retry with backoff
- **Background sync** support for queue draining
- **sendBeacon fallback** on page unload

### Offline Support
- **App shell caching** for offline access
- **Queue persistence** across browser sessions
- **Automatic upload** when connection restored
- **Service worker** for background processing

## Technical Implementation

### Data Flow
1. **GPS Fix** → Buffer (in-memory)
2. **Buffer Full/Timeout** → IndexedDB Queue
3. **Network Available** → Backend API
4. **Success** → Remove from queue
5. **Failure** → Retry with backoff

### API Integration
- `POST /api/walks/start?petId=...` → Start walk
- `POST /api/walks/{id}/points` → Upload batch
- `POST /api/walks/{id}/stop` → Stop walk + summary

### Data Format
```json
{
  "lat": 37.7749,
  "lon": -122.4194,
  "ts": "2025-01-15T10:30:00Z",
  "elev": 100.5
}
```

## Configuration

### Batching
- **Buffer size**: 10 points
- **Flush interval**: 5 seconds
- **Max retries**: 10 attempts

### Geolocation
- **High accuracy**: Enabled
- **Maximum age**: 2 seconds
- **Timeout**: 10 seconds

### Backoff Strategy
- **Base delay**: 5 seconds
- **Max delay**: 60 seconds
- **Jitter**: ±1 second random

## Privacy & Security

### Location Data
- **Local storage only** until uploaded
- **No third-party tracking**
- **User consent required** for GPS access
- **Data retention**: Server-side only (not cached in PWA)

### Network Security
- **HTTPS required** for production
- **Same-origin API calls** only
- **No sensitive data in logs**

## Browser Support

### Required Features
- **Service Workers** (for offline support)
- **IndexedDB** (for queue persistence)
- **Geolocation API** (for GPS tracking)
- **Fetch API** (for network requests)

### Optional Features
- **Background Sync** (for enhanced offline)
- **Push Notifications** (for future features)

## Usage Limits

### Performance
- **Max batch size**: 10 points (configurable)
- **Queue size**: Unlimited (IndexedDB)
- **GPS accuracy**: Platform dependent
- **Battery impact**: Moderate (high-accuracy GPS)

### Storage
- **App cache**: ~2MB (app shell)
- **Queue storage**: Unlimited (IndexedDB)
- **Local data**: Cleared on walk completion

## Error Handling

### GPS Errors
- **Permission denied**: User notification + guidance
- **Position unavailable**: Retry with timeout
- **Timeout**: Retry with increased timeout

### Network Errors
- **Connection lost**: Queue data, retry on restore
- **Server errors**: Exponential backoff
- **Validation errors**: Drop invalid data

### Storage Errors
- **IndexedDB full**: Fallback to memory (data loss risk)
- **Cache full**: LRU eviction
- **Quota exceeded**: User notification

## Development

### Local Development
1. Serve from HTTPS (required for service worker)
2. Enable location permissions
3. Test offline scenarios
4. Monitor IndexedDB usage

### Testing
- **Online/offline switching**
- **GPS permission changes**
- **Browser tab switching**
- **App installation/uninstallation**

### Debugging
- **Console logs** for all operations
- **IndexedDB inspection** in DevTools
- **Service Worker** status in DevTools
- **Network tab** for API calls

## Future Enhancements

### Planned Features
- **Pet selection UI** (currently hardcoded to pet ID 1)
- **Walk history** display
- **Route visualization** on map
- **Export functionality**
- **Push notifications** for walk completion

### Performance Optimizations
- **Web Workers** for heavy processing
- **Compression** for large datasets
- **Incremental sync** for long walks
- **Background processing** improvements

## Troubleshooting

### Common Issues
1. **GPS not working**: Check location permissions
2. **Data not uploading**: Check network connection
3. **App not loading**: Clear cache and reload
4. **Queue stuck**: Check browser console for errors

### Debug Commands
```javascript
// Check queue status
window.walkQueue.getQueueSize().then(console.log);

// Clear all data
window.walkQueue.clearAll();

// Force queue drain
window.networking.drainQueue();
```

## License

This PWA is part of the PatTrail project. See main project LICENSE for details.
