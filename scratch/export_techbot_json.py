import json
import openpyxl

wb = openpyxl.load_workbook('datasets/techbot.xlsx')
sheet = wb['Courses']
rows = list(sheet.iter_rows(values_only=True))
header = rows[0]

courses = []
for r in rows[1:]:
    row_dict = dict(zip(header, r))
    courses.append({
        "course_id": str(row_dict.get("course_id", "")).strip(),
        "title": str(row_dict.get("title", "")).strip(),
        "skill_tag": str(row_dict.get("skill_tag", "")).strip(),
        "level": str(row_dict.get("level", "")).strip(),
        "duration_hours": float(row_dict.get("duration_hours", 0)),
        "platform": str(row_dict.get("platform", "")).strip(),
        "link": str(row_dict.get("link", "")).strip()
    })

output_path = 'backend/learning-path-backend/src/main/resources/data/techbot_courses.json'
with open(output_path, 'w', encoding='utf-8') as f:
    json.dump(courses, f, indent=2)

print(f"Exported {len(courses)} courses to {output_path}")
