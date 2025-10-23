package com.example.pettrail.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Database configuration for Supabase PostgreSQL
 * This class handles environment variable loading for database connection
 */
@Configuration
@Profile("!test")
public class DatabaseConfig {

    @Value("${SUPABASE_DB_PASSWORD:your_database_password_here}")
    private String databasePassword;

    @Value("${SUPABASE_DB_HOST:db.imjnouggsraddeuekgqp.supabase.co}")
    private String databaseHost;

    @Value("${SUPABASE_DB_PORT:5432}")
    private String databasePort;

    @Value("${SUPABASE_DB_NAME:postgres}")
    private String databaseName;

    @Value("${SUPABASE_DB_USER:postgres}")
    private String databaseUser;

    // Getters for potential use in other configurations
    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseHost() {
        return databaseHost;
    }

    public String getDatabasePort() {
        return databasePort;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }
}
