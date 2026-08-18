-- ==============================================================================
-- Migration 08: Skill Aliases Schema Preparation
-- Creates skill_aliases table for persistent mapping between dataset skills and canonical skills.
-- Idempotent for PostgreSQL / Supabase execution.
-- ==============================================================================

DO $$
BEGIN
    -- 1. Create skill_aliases table if not exists
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'skill_aliases'
    ) THEN
        CREATE TABLE public.skill_aliases (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            dataset_skill_name VARCHAR(100) NOT NULL UNIQUE,
            canonical_skill_id UUID NOT NULL REFERENCES public.skills(id) ON DELETE CASCADE,
            mapping_type VARCHAR(30) NOT NULL,
            confidence DOUBLE PRECISION NOT NULL,
            reason VARCHAR(500) NULL,
            created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
            updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
        );
    END IF;

    -- 2. Create index on dataset_skill_name if not exists
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE schemaname = 'public'
          AND tablename = 'skill_aliases'
          AND indexname = 'idx_skill_aliases_dataset_name'
    ) THEN
        CREATE INDEX idx_skill_aliases_dataset_name ON public.skill_aliases(dataset_skill_name);
    END IF;

    -- 3. Create index on canonical_skill_id if not exists
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE schemaname = 'public'
          AND tablename = 'skill_aliases'
          AND indexname = 'idx_skill_aliases_canonical_skill'
    ) THEN
        CREATE INDEX idx_skill_aliases_canonical_skill ON public.skill_aliases(canonical_skill_id);
    END IF;
END $$;
