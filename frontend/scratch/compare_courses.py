import re
import openpyxl
import json

# 1. Load Excel courses
wb = openpyxl.load_workbook('datasets/techbot.xlsx')
courses_sheet = wb['Courses']
courses_rows = list(courses_sheet.iter_rows(values_only=True))
excel_courses = [dict(zip(courses_rows[0], r)) for r in courses_rows[1:]]

# 2. Parse CourseDataInitializer.java courses
with open('backend/learning-path-backend/src/main/java/com/learningpath/config/CourseDataInitializer.java', 'r', encoding='utf-8') as f:
    code = f.read()

# Find course definitions in CourseDataInitializer
course_pattern = re.compile(r'Course\.builder\(\)\s*\.title\("([^"]+)"\)\s*\.description\("([^"]+)"\)\s*\.provider\("([^"]+)"\)\s*\.url\("([^"]+)"\)\s*\.durationHours\(([^)]+)\)\s*\.courseType\(CourseType\.([A-Z_]+)\)\s*\.difficulty\(CourseDifficulty\.([A-Z_]+)\)\s*\.rating\(new BigDecimal\("([^"]+)"\)\)\s*\.price\(new BigDecimal\("([^"]+)"\)\)\s*\.isFree\((true|false)\)', re.MULTILINE)

matches = course_pattern.findall(code)
db_courses = []
for m in matches:
    db_courses.append({
        "title": m[0],
        "description": m[1],
        "provider": m[2],
        "url": m[3],
        "durationHours": float(m[4]),
        "courseType": m[5],
        "difficulty": m[6],
        "rating": float(m[7]),
        "price": float(m[8]),
        "isFree": m[9] == "true"
    })

print(f"Parsed {len(db_courses)} seeded database courses from CourseDataInitializer.java")
print(f"Total Excel courses: {len(excel_courses)}")

# Check overlap
excel_titles = set(c['title'] for c in excel_courses)
db_titles = set(c['title'] for c in db_courses)

overlap_titles = excel_titles.intersection(db_titles)
print(f"Exact title overlaps between Excel and Seeded DB: {len(overlap_titles)}")
for t in overlap_titles:
    print(f"  - {t}")

# Compare attributes
excel_fields = list(excel_courses[0].keys())
db_fields = ["id", "title", "description", "provider", "url", "durationMinutes", "durationHours", "courseType", "language", "difficulty", "rating", "price", "isFree"]

print("\nExcel Fields:", excel_fields)
print("DB Course Entity Fields:", db_fields)

fields_in_excel_not_db = [f for f in excel_fields if f not in db_fields and f not in ['link', 'platform', 'level', 'skill_tag']]
print("Fields in Excel not in DB Entity (conceptually):", fields_in_excel_not_db)

course_comparison = {
    "excel_course_count": len(excel_courses),
    "db_course_count": len(db_courses),
    "overlap_titles": list(overlap_titles),
    "excel_fields": excel_fields,
    "db_fields": db_fields,
    "sample_excel_courses": excel_courses[:5],
    "sample_db_courses": db_courses[:5]
}

with open('scratch/course_comparison_report.json', 'w', encoding='utf-8') as f:
    json.dump(course_comparison, f, indent=2)

print("Saved scratch/course_comparison_report.json")
