import os
import re
import json
import psycopg2
from psycopg2.extras import RealDictCursor

# Parse .env to get DB connection details
env_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), '.env')
db_url = None
db_user = None
db_password = None

if os.path.exists(env_path):
    with open(env_path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.trim() if hasattr(line, 'trim') else line.strip()
            if line.startswith('DB_URL='):
                db_url = line.split('=', 1)[1].strip().strip('"').strip("'")
            elif line.startswith('DB_USERNAME='):
                db_user = line.split('=', 1)[1].strip().strip('"').strip("'")
            elif line.startswith('DB_PASSWORD='):
                db_password = line.split('=', 1)[1].strip().strip('"').strip("'")

# Clean JDBC URL prefix if present for psycopg2
# jdbc:postgresql://host:port/database?sslmode=require
if db_url and db_url.startswith('jdbc:'):
    db_url = db_url[5:]

print(f"Connecting to database: {db_url.split('@')[-1] if '@' in db_url else 'localhost'} as user: {db_user}...")

conn = psycopg2.connect(db_url, user=db_user, password=db_password)
conn.autocommit = False
cur = conn.cursor(cursor_factory=RealDictCursor)

# 1. Query all tables in public schema
cur.execute("""
    SELECT table_name 
    FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
    ORDER BY table_name;
""")
all_tables = [r['table_name'] for r in cur.fetchall()]
print(f"Total tables found in public schema: {len(all_tables)}")

# Table Classification
USER_DATA_TABLES = [
    'adaptive_assessment_responses',
    'adaptive_assessment_sessions',
    'ai_messages',
    'ai_conversations',
    'assessment_results',
    'learner_knowledge_states',
    'learning_activities',
    'learning_path_nodes',
    'learning_path_versions',
    'learning_paths',
    'weekly_day_schedules',
    'weekly_learning_plans',
    'user_learning_path_nodes',
    'user_learning_paths',
    'user_skills',
    'user_progress',
    'user_projects',
    'support_tickets',
    'notifications',
    'otp_verifications',
    'users'
]

REFERENCE_DATA_TABLES = [
    'assessment_questions',
    'assessments',
    'courses',
    'course_skills',
    'skills',
    'career_tracks',
    'career_skills',
    'career_nodes',
    'career_paths',
    'skill_dependencies',
    'skill_aliases',
    'flyway_schema_history'
]

# Identify existing user data tables present in DB
existing_user_tables = [t for t in USER_DATA_TABLES if t in all_tables]
existing_ref_tables = [t for t in all_tables if t not in existing_user_tables]

print("\n--- TABLE CLASSIFICATION ---")
print(f"USER DATA (WILL BE CLEARED): {existing_user_tables}")
print(f"REFERENCE DATA (WILL BE PRESERVED): {existing_ref_tables}")

# 2. Query Row Counts Before Reset
print("\n--- BEFORE RESET: ROW COUNTS ---")
before_counts = {}
for t in all_tables:
    cur.execute(f'SELECT COUNT(*) as cnt FROM "{t}";')
    cnt = cur.fetchone()['cnt']
    before_counts[t] = cnt
    is_user_tab = t in existing_user_tables
    print(f"  {'[USER]' if is_user_tab else '[REF] '} {t:35s}: {cnt} rows")

# 3. Create Backup of User Data to JSON
backup_data = {}
for t in existing_user_tables:
    cur.execute(f'SELECT * FROM "{t}";')
    rows = cur.fetchall()
    # Convert non-serializable objects (UUID, datetime, etc.) to string
    serialized_rows = []
    for r in rows:
        row_dict = {}
        for k, v in r.items():
            row_dict[k] = str(v) if v is not None else None
        serialized_rows.append(row_dict)
    backup_data[t] = serialized_rows

backup_file = os.path.join(os.path.dirname(__file__), 'backup_user_data_before_reset.json')
with open(backup_file, 'w', encoding='utf-8') as f:
    json.dump(backup_data, f, indent=2)
print(f"\n[BACKUP] Saved user data backup to {backup_file}")

# 4. Safely Clear User Data Tables in Dependency Order (Cascading TRUNCATE or Ordered DELETE)
print("\n--- CLEARING USER DATA TABLES ---")
# Use TRUNCATE on existing user tables with CASCADE
tables_to_truncate = ', '.join([f'"{t}"' for t in existing_user_tables])
truncate_sql = f"TRUNCATE TABLE {tables_to_truncate} CASCADE;"
print(f"Executing: {truncate_sql}")
cur.execute(truncate_sql)
conn.commit()
print("[SUCCESS] All user-data tables truncated successfully.")

# 5. Query Row Counts After Reset
print("\n--- AFTER RESET: ROW COUNTS ---")
after_counts = {}
for t in all_tables:
    cur.execute(f'SELECT COUNT(*) as cnt FROM "{t}";')
    cnt = cur.fetchone()['cnt']
    after_counts[t] = cnt
    is_user_tab = t in existing_user_tables
    print(f"  {'[USER]' if is_user_tab else '[REF] '} {t:35s}: {cnt} rows")

# Save summary to JSON for reporting
summary = {
    "existing_user_tables": existing_user_tables,
    "existing_ref_tables": existing_ref_tables,
    "before_counts": before_counts,
    "after_counts": after_counts
}
summary_file = os.path.join(os.path.dirname(__file__), 'reset_summary.json')
with open(summary_file, 'w', encoding='utf-8') as f:
    json.dump(summary, f, indent=2)

cur.close()
conn.close()
print("\nDatabase connection closed. Reset completed cleanly.")
