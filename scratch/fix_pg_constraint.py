import os

try:
    import psycopg2
    # Connect using default postgres credentials or environment
    user = os.environ.get("DB_USERNAME", os.environ.get("DATABASE_USERNAME", "postgres"))
    password = os.environ.get("DB_PASSWORD", os.environ.get("DATABASE_PASSWORD", "postgres"))
    host = "localhost"
    port = "5432"
    dbname = "postgres"

    conn = psycopg2.connect(dbname=dbname, user=user, password=password, host=host, port=port)
    conn.autocommit = True
    cursor = conn.cursor()
    cursor.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_course_type_check;")
    print("Dropped courses_course_type_check constraint successfully!")
    conn.close()
except Exception as e:
    print(f"Error: {e}")
