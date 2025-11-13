# E-Mysore Backend - Database Migrations

## Overview

This backend uses **Flyway** for automated database schema management. Flyway ensures version-controlled, repeatable database migrations across all environments.

## Migration Files

Located in `src/main/resources/db/migration/`:

- **V1__Initial_Schema.sql** — Initial schema setup (users, complaints, audit logs, notifications, departments tables and indexes)
- **V2__Add_Department_Hierarchy.sql** — Adds `department_hierarchy` TEXT column to `complaints` table to store ML-assigned escalation chains

## Configuration

In `src/main/resources/application.properties`:

```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baselineOnMigrate=true

# JPA Configuration (Hibernate validates but does NOT auto-create schema)
spring.jpa.hibernate.ddl-auto=validate
```

**Important**: `ddl-auto=validate` means Hibernate will verify the schema matches entities but will NOT create tables. Flyway handles all DDL.

## Running Migrations

Migrations run **automatically** on application startup:

```bash
java -jar target/ecom-mysore-backend-0.0.1-SNAPSHOT.jar
```

Flyway will:
1. Check the `flyway_schema_history` table.
2. Compare applied versions to versions found in the migration directory.
3. Execute any new/unapplied migrations in order.

## Manual Migration Check

To verify which migrations have been applied:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## Adding New Migrations

Follow this naming convention:
- **Versioned migrations**: `V<version>__<description>.sql`
  - Examples: `V3__Add_User_Status_Field.sql`, `V4__Create_Analytics_Table.sql`
- **Repeatable migrations** (optional): `R__<description>.sql` (runs every time if changed)

### Example: Adding a new column

1. Create `src/main/resources/db/migration/V3__Add_New_Column.sql`:
   ```sql
   ALTER TABLE complaints ADD COLUMN IF NOT EXISTS new_field VARCHAR(255);
   COMMENT ON COLUMN complaints.new_field IS 'Description of the new field';
   ```

2. Rebuild and restart:
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/ecom-mysore-backend-0.0.1-SNAPSHOT.jar
   ```

Flyway will automatically apply `V3` on the next startup.

## Database Setup (First Run)

**Prerequisites:**
- PostgreSQL running on localhost:5432
- Database `emysore` created and accessible with user `postgres` / password `postgres`

```bash
# Create database (if not exists)
createdb -U postgres emysore

# Start backend
cd ecom-mysore-backend
./mvnw spring-boot:run
```

The backend will:
1. Connect to PostgreSQL.
2. Create `flyway_schema_history` table (Flyway tracking).
3. Execute `V1__Initial_Schema.sql` to create all tables and indexes.
4. Execute `V2__Add_Department_Hierarchy.sql` to add the department hierarchy column.
5. Start the application normally.

## Department Hierarchy Persistence

When a complaint is created, the ML service enriches it with:
- `assigned_dept` — The assigned department name
- `department_hierarchy` — A list of authority names (e.g., ["AE", "JE", "EE", "Commissioner"])

These are stored in the database:
- `complaints.assigned_dept` — VARCHAR(255)
- `complaints.department_hierarchy` — TEXT (joined as "AE > JE > EE > Commissioner")

Example query:
```sql
SELECT id, title, assigned_dept, department_hierarchy
FROM complaints
WHERE assigned_dept IS NOT NULL
LIMIT 5;
```

## Production Considerations

1. **Rollback Scenarios**: Flyway does not auto-rollback failed migrations. On failure:
   - Review logs.
   - Fix the SQL in the migration file.
   - Manually clean up any partial changes in the database.
   - Increment version number and retry.

2. **Zero-Downtime Deployments**:
   - Design migrations to be backward-compatible (e.g., add columns with defaults, don't drop without verification).
   - Test migrations in staging first.

3. **Large Migrations**:
   - For large data operations, consider splitting into multiple migrations.
   - Add indexes separately after inserts for better performance.

## Troubleshooting

### Migration fails on startup

**Symptom**: Application crashes with `FlywayException: Unable to execute migration`.

**Solution**:
1. Check `flyway_schema_history` table for the failed migration.
2. Review the error in application logs (`/tmp/backend.log`).
3. Manually inspect/fix the database if needed.
4. Delete the failed row from `flyway_schema_history` (if safe) or increment the version and create a corrective migration.

### Table already exists

**Symptom**: `V1__Initial_Schema.sql` fails because tables exist from a previous Flyway setup.

**Solution**:
- Use `CREATE TABLE IF NOT EXISTS` (already done in V1).
- Or clean the database and restart: `DROP DATABASE emysore; CREATE DATABASE emysore;`

---

**For questions or issues**, check application logs and the Flyway documentation: https://flywaydb.org/documentation
