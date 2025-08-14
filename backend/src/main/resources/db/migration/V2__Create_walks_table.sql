CREATE TABLE IF NOT EXISTS walks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pet_id INTEGER NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP NULL,
    FOREIGN KEY (pet_id) REFERENCES pets(id)
);

-- Create a unique index to ensure only one active walk per pet
-- This enforces the business rule that a pet can only have one active walk at a time
CREATE UNIQUE INDEX IF NOT EXISTS idx_walks_active_pet 
ON walks (pet_id) 
WHERE finished_at IS NULL;
