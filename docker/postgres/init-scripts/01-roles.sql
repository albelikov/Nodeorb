-- Initialize Logi database roles and permissions

-- Create application users
DO $$
BEGIN
    -- Create login roles for each service
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'logi_admin') THEN
        CREATE ROLE logi_admin WITH LOGIN PASSWORD 'logi_admin_password';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'logi_iam') THEN
        CREATE ROLE logi_iam WITH LOGIN PASSWORD 'logi_iam_password';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'logi_audit') THEN
        CREATE ROLE logi_audit WITH LOGIN PASSWORD 'logi_audit_password';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'logi_dp') THEN
        CREATE ROLE logi_dp WITH LOGIN PASSWORD 'logi_dp_password';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'logi_tms') THEN
        CREATE ROLE logi_tms WITH LOGIN PASSWORD 'logi_tms_password';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'logi_fms') THEN
        CREATE ROLE logi_fms WITH LOGIN PASSWORD 'logi_fms_password';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'logi_gis') THEN
        CREATE ROLE logi_gis WITH LOGIN PASSWORD 'logi_gis_password';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'logi_wms') THEN
        CREATE ROLE logi_wms WITH LOGIN PASSWORD 'logi_wms_password';
    END IF;
    
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'logi_oms') THEN
        CREATE ROLE logi_oms WITH LOGIN PASSWORD 'logi_oms_password';
    END IF;
END $$;

-- Grant CONNECT privilege on database
GRANT CONNECT ON DATABASE logi TO logi_admin, logi_iam, logi_audit, logi_dp, logi_tms, logi_fms, logi_gis, logi_wms, logi_oms;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- Enable pg_cron for scheduled tasks (optional)
CREATE EXTENSION IF NOT EXISTS "pg_cron";
