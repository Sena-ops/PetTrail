CREATE TABLE IF NOT EXISTS walk_points (
    id BIGSERIAL PRIMARY KEY,
    walk_id BIGINT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    elevation DECIMAL(8, 2) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (walk_id) REFERENCES walks(id) ON DELETE CASCADE
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_walk_points_walk_id ON walk_points (walk_id);
CREATE INDEX IF NOT EXISTS idx_walk_points_timestamp ON walk_points (timestamp);

-- Add constraints for coordinate validation
-- Latitude must be between -90 and 90 degrees
-- Longitude must be between -180 and 180 degrees
-- Elevation must be positive if provided
ALTER TABLE walk_points ADD CONSTRAINT chk_latitude CHECK (latitude >= -90 AND latitude <= 90);
ALTER TABLE walk_points ADD CONSTRAINT chk_longitude CHECK (longitude >= -180 AND longitude <= 180);
ALTER TABLE walk_points ADD CONSTRAINT chk_elevation CHECK (elevation IS NULL OR elevation >= 0);
