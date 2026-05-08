---
name: db-migration
description: Create a new Flyway SQL migration file for this project with the correct next version number. Use when adding tables, columns, indexes, or constraints. Also enables Flyway if it is currently disabled.
argument-hint: "<description of schema change>  e.g. add email_verifications table"
---

Create a Flyway migration for this project. Change: `$ARGUMENTS`

## Existing migrations
!`ls -1 microservice/src/main/resources/db/migration/ 2>/dev/null || echo "none yet"`

## Current schema (for reference)
!`cat microservice/src/main/resources/db/migration/V1__init.sql 2>/dev/null || echo "not found"`

## Flyway config
!`grep -E "flyway|ddl-auto" microservice/src/main/resources/application.properties`

## Steps

### 1. Determine next version number
- Look at existing files — find the highest `V{N}` prefix
- Next file is `V{N+1}__<snake_case_description>.sql`

### 2. Write the migration file
Path: `microservice/src/main/resources/db/migration/V{N+1}__<description>.sql`

Rules:
- Use `IF NOT EXISTS` / `IF EXISTS` guards where appropriate
- Prefer `ALTER TABLE ADD COLUMN IF NOT EXISTS` for additive changes
- Always add indexes for FK columns and any column used in WHERE clauses
- Use `UUID` for primary keys, `TEXT` for strings (no arbitrary VARCHAR lengths unless constrained), `BOOLEAN DEFAULT false`, `TIMESTAMPTZ` for timestamps
- Never drop columns or tables without explicit instruction — irreversible
- One logical change per migration file

### 3. Enable Flyway (if currently disabled)
If `spring.flyway.enabled=false` is in `application.properties`, note it but **do not change it** — just remind the user to enable it before running.

### 4. Update docs/decisions.md
Append a brief entry explaining why the schema change was made.

## Rules reminder
- File path before every code block
- No placeholders — complete working SQL
