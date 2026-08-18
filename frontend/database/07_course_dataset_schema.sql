-- ==============================================================================
-- Migration 07: Course Dataset Schema Preparation
-- Adds course_code and YouTube support fields to the courses table.
-- Idempotent for PostgreSQL / Supabase execution.
-- ==============================================================================

DO $$
BEGIN
    -- 1. Add course_code column if not exists
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'courses'
          AND column_name = 'course_code'
    ) THEN
        ALTER TABLE public.courses
        ADD COLUMN course_code VARCHAR(50) NULL;
    END IF;

    -- 2. Add unique constraint / index on course_code
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE schemaname = 'public'
          AND tablename = 'courses'
          AND indexname = 'idx_courses_course_code'
    ) THEN
        CREATE UNIQUE INDEX idx_courses_course_code ON public.courses(course_code) WHERE course_code IS NOT NULL;
    END IF;

    -- 3. Add youtube_title column if not exists
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'courses'
          AND column_name = 'youtube_title'
    ) THEN
        ALTER TABLE public.courses
        ADD COLUMN youtube_title VARCHAR(255) NULL;
    END IF;

    -- 4. Add youtube_url column if not exists
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'courses'
          AND column_name = 'youtube_url'
    ) THEN
        ALTER TABLE public.courses
        ADD COLUMN youtube_url VARCHAR(500) NULL;
    END IF;

    -- 5. Add youtube_notes column if not exists
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'courses'
          AND column_name = 'youtube_notes'
    ) THEN
        ALTER TABLE public.courses
        ADD COLUMN youtube_notes TEXT NULL;
    END IF;
END $$;
