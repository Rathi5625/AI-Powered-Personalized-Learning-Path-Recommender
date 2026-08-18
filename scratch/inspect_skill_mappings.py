import json
import openpyxl

wb = openpyxl.load_workbook('datasets/techbot.xlsx')
sheet = wb['Courses']
rows = list(sheet.iter_rows(values_only=True))
header = rows[0]

skills_in_excel = set()
for r in rows[1:]:
    row_dict = dict(zip(header, r))
    skill_tag = str(row_dict.get("skill_tag", "")).strip()
    if skill_tag:
        skills_in_excel.add(skill_tag)

with open('backend/learning-path-backend/src/main/resources/data/skill_prerequisites.json', 'r', encoding='utf-8') as f:
    prereq_data = json.load(f)

dag_skills = set()
for domain, s_map in prereq_data.items():
    for s_name, p_list in s_map.items():
        dag_skills.add(s_name)
        for p in p_list:
            dag_skills.add(p)

# Existing 25 DB skills from CareerDataInitializer
db_skills = {
    "Java", "OOP", "Data Structures & Algorithms", "SQL", "Spring Boot",
    "REST APIs", "JPA/Hibernate", "Spring Security", "Testing", "Docker",
    "HTML", "CSS", "JavaScript", "TypeScript", "React", "Git", "Python",
    "Pandas", "NumPy", "Statistics", "Data Visualization", "Machine Learning",
    "Deep Learning", "TensorFlow/PyTorch", "MLOps"
}

print(f"Total Unique Skill Tags in Excel: {len(skills_in_excel)}")
print(f"Total Unique Skills in DAG: {len(dag_skills)}")
print(f"Total Unique Skills in DB Seed: {len(db_skills)}")
print("=" * 60)

for s in sorted(skills_in_excel):
    in_db = s in db_skills
    in_dag = s in dag_skills
    print(f"Excel Skill: '{s}' | In DB: {in_db} | In DAG: {in_dag}")
