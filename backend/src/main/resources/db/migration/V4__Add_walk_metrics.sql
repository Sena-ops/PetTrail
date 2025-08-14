-- Add metrics columns to walks table
ALTER TABLE walks ADD COLUMN distancia_m DOUBLE DEFAULT NULL;
ALTER TABLE walks ADD COLUMN duracao_s INTEGER DEFAULT NULL;
ALTER TABLE walks ADD COLUMN vel_media_kmh DOUBLE DEFAULT NULL;
