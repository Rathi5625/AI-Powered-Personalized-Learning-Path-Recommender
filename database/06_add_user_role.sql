-- ==============================================================================
-- Idempotent Migration: Add 'role' column to users table
-- ==============================================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'users'
          AND column_name = 'role'
    ) THEN
        ALTER TABLE public.users
        ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
    END IF;
END $$;

-- Ensure all existing rows have a valid default role
UPDATE public.users SET role = 'USER' WHERE role IS NULL;
