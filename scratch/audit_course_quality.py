import openpyxl

wb = openpyxl.load_workbook('datasets/techbot.xlsx')
sheet = wb['Courses']
rows = list(sheet.iter_rows(values_only=True))
header = rows[0]

courses = []
for r in rows[1:]:
    row_dict = dict(zip(header, r))
    courses.append(row_dict)

print(f"Total Rows: {len(courses)}")

# Quality checks
null_titles = [c for c in courses if not c.get('title')]
null_codes = [c for c in courses if not c.get('course_id')]
null_providers = [c for c in courses if not c.get('platform')]
null_links = [c for c in courses if not c.get('link')]
invalid_levels = [c for c in courses if c.get('level') not in ['Beginner', 'Easy', 'Medium', 'High']]

print(f"Null Titles: {len(null_titles)}")
print(f"Null Codes: {len(null_codes)}")
print(f"Null Providers: {len(null_providers)}")
print(f"Null Links: {len(null_links)}")
print(f"Invalid Levels: {len(invalid_levels)}")

unique_codes = set(c['course_id'] for c in courses)
print(f"Unique Course Codes: {len(unique_codes)} (Duplicates: {len(courses) - len(unique_codes)})")

# Skill breakdown
skill_counts = {}
for c in courses:
    s = c['skill_tag']
    skill_counts[s] = skill_counts.get(s, 0) + 1

print(f"Total Unique Skills: {len(skill_counts)}")
for s, cnt in sorted(skill_counts.items()):
    if cnt != 4:
        print(f"WARNING: Skill {s} has {cnt} courses (expected 4)")

print("All skills have exactly 4 courses:", all(cnt == 4 for cnt in skill_counts.values()))
