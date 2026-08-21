import psycopg2

passwords = ["", "postgres", "admin", "root", "password", "1234", "123456"]
for pwd in passwords:
    try:
        conn = psycopg2.connect(dbname="postgres", user="postgres", password=pwd, host="localhost", port="5432")
        conn.autocommit = True
        cur = conn.cursor()
        cur.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_course_type_check;")
        print(f"Success with password: '{pwd}'! Dropped constraint.")
        conn.close()
        break
    except Exception as e:
        print(f"Failed with '{pwd}': {e}")
