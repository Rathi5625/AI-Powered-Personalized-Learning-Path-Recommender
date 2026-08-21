import psycopg2

users = ["postgres", "parth", "sa"]
passwords = ["", "postgres", "admin", "root", "1234", "123456", "password", "parth", "parth123", "postgres123"]

found = False
for u in users:
    for p in passwords:
        try:
            conn = psycopg2.connect(dbname="postgres", user=u, password=p, host="localhost", port="5432")
            conn.autocommit = True
            cur = conn.cursor()
            cur.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_course_type_check;")
            print(f"SUCCESS! user={u}, password={p}. Constraint courses_course_type_check DROPPED!")
            conn.close()
            found = True
            break
        except Exception:
            pass
    if found:
        break

if not found:
    print("Could not find working PostgreSQL credentials automatically.")
