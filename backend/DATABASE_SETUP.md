# Database Setup Guide

This guide explains how to set up the database connection with Supabase PostgreSQL.

## Prerequisites

1. Supabase project with PostgreSQL database
2. Database credentials (host, port, username, password)

## Environment Variables

Create a `.env` file in the project root with the following variables:

```env
# Supabase Configuration
SUPABASE_URL=https://imjnouggsraddeuekgqp.supabase.co
SUPABASE_ANON_KEY=your_anon_key_here

# Database connection details
SUPABASE_DB_HOST=db.imjnouggsraddeuekgqp.supabase.co
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=postgres
SUPABASE_DB_PASSWORD=your_actual_database_password_here

# Frontend environment variables
VITE_SUPABASE_URL=https://imjnouggsraddeuekgqp.supabase.co
VITE_SUPABASE_ANON_KEY=your_anon_key_here
```

## Database Schema

The application uses the following tables:

### 1. `pets` table
- `id` (SERIAL PRIMARY KEY)
- `name` (VARCHAR(60))
- `species` (VARCHAR(20))
- `age` (INTEGER)
- `race` (VARCHAR(50))
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### 2. `walks` table
- `id` (SERIAL PRIMARY KEY)
- `pet_id` (INTEGER, FOREIGN KEY)
- `started_at` (TIMESTAMP)
- `finished_at` (TIMESTAMP, nullable)
- `distancia_m` (DOUBLE, nullable)
- `duracao_s` (INTEGER, nullable)
- `vel_media_kmh` (DOUBLE, nullable)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### 3. `walk_points` table
- `id` (SERIAL PRIMARY KEY)
- `walk_id` (INTEGER, FOREIGN KEY)
- `latitude` (DECIMAL(10, 8))
- `longitude` (DECIMAL(11, 8))
- `timestamp` (TIMESTAMP)
- `elevation` (DECIMAL(8, 2), nullable)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

## Migration Files

The database schema is managed by Flyway migrations:

1. `V1__Create_pets_table.sql` - Creates the pets table
2. `V2__Create_walks_table.sql` - Creates the walks table with unique index
3. `V3__Create_walk_points_table.sql` - Creates the walk_points table with constraints
4. `V4__Add_walk_metrics.sql` - Adds metrics columns to walks table
5. `V5__Setup_database_for_production.sql` - Production setup with triggers and permissions

## Running the Application

1. **Set up environment variables**: Make sure your `.env` file contains the correct database password
2. **Start the application**: The Flyway migrations will run automatically on startup
3. **Verify connection**: Check the application logs for successful database connection

## Security Notes

- The `.env` file is ignored by git and should never be committed
- Database credentials are loaded from environment variables
- The application uses connection pooling for better performance
- All database operations are logged for monitoring

## Troubleshooting

### Connection Issues
- Verify your database credentials in the `.env` file
- Check that your Supabase project is active
- Ensure the database host and port are correct

### Migration Issues
- Check Flyway logs for migration errors
- Verify that the database user has proper permissions
- Ensure all migration files are in the correct format

### Performance Issues
- Monitor database connection pool usage
- Check for slow queries in the application logs
- Consider adding database indexes for frequently queried columns
