import sys

for mod in ['psycopg2', 'asyncpg', 'sqlite3', 'sqlalchemy', 'psycopg']:
    try:
        __import__(mod)
        print(f"Available: {mod}")
    except ImportError:
        pass
