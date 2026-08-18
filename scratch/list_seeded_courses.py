import re

with open('backend/learning-path-backend/src/main/java/com/learningpath/config/CourseDataInitializer.java', 'r', encoding='utf-8') as f:
    text = f.read()

courses = re.findall(r'createCourseWithSkills\(\s*"([^"]+)",\s*"([^"]+)",\s*"([^"]+)",\s*"([^"]+)",\s*CourseDifficulty\.([A-Z_]+),\s*([0-9.]+)', text)
print(f"Found {len(courses)} courses in CourseDataInitializer.java:")
for i, c in enumerate(courses, 1):
    print(f"{i}. {c[0]} | Provider: {c[2]} | Diff: {c[4]} | Hours: {c[5]}")
