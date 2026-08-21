import psycopg2

try:
    conn = psycopg2.connect(dbname="postgres", user="postgres", password="", host="127.0.0.1", port="5432")
    conn.autocommit = True
    cur = conn.cursor()
    cur.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_course_type_check;")
    print("SUCCESS! Constraint courses_course_type_check dropped via 127.0.0.1!")
    conn.close()
except Exception as e:
    print(f"Error on 127.0.0.1: {e}")
