import json
import openpyxl

# 1. Load Excel skills
wb = openpyxl.load_workbook('datasets/techbot.xlsx')
courses_sheet = wb['Courses']
courses_rows = list(courses_sheet.iter_rows(values_only=True))
excel_skills = sorted(list(set(r[2] for r in courses_rows[1:])))

# 2. Load backend skill_prerequisites.json
with open('backend/learning-path-backend/src/main/resources/data/skill_prerequisites.json', 'r', encoding='utf-8') as f:
    prereqs_data = json.load(f)

backend_graph_skills = set()
for domain, skills_map in prereqs_data.items():
    for skill_name, prereqs in skills_map.items():
        if skill_name and not skill_name.startswith('---'):
            backend_graph_skills.add(skill_name)
        if prereqs:
            for p in prereqs:
                if p and not p.startswith('---'):
                    backend_graph_skills.add(p)

backend_graph_skills = sorted(list(backend_graph_skills))

# 3. Seed skills in CareerDataInitializer (25 skills)
seeded_db_skills = sorted([
    "Java", "OOP", "Data Structures & Algorithms", "SQL", "Spring Boot",
    "REST APIs", "JPA/Hibernate", "Spring Security", "Testing", "Docker",
    "HTML", "CSS", "JavaScript", "TypeScript", "React", "Git", "Python",
    "Pandas", "NumPy", "Statistics", "Data Visualization", "Machine Learning",
    "Deep Learning", "TensorFlow/PyTorch", "MLOps"
])

print("==================================================")
print("SKILL COMPARISONS")
print("==================================================")
print(f"Excel Skills Count: {len(excel_skills)}")
print(f"Backend Graph Skills Count: {len(backend_graph_skills)}")
print(f"Seeded DB Skills Count: {len(seeded_db_skills)}")

excel_set = set(excel_skills)
graph_set = set(backend_graph_skills)
db_set = set(seeded_db_skills)

# In Excel but not in seeded DB
excel_not_in_db = sorted(list(excel_set - db_set))
print(f"\nSkills in Excel but missing from seeded DB ({len(excel_not_in_db)}):")
for s in excel_not_in_db:
    print(f"  - {s}")

# In seeded DB but not in Excel
db_not_in_excel = sorted(list(db_set - excel_set))
print(f"\nSkills in seeded DB but missing from Excel ({len(db_not_in_excel)}):")
for s in db_not_in_excel:
    print(f"  - {s}")

# In Excel vs Backend Graph
excel_not_in_graph = sorted(list(excel_set - graph_set))
graph_not_in_excel = sorted(list(graph_set - excel_set))
print(f"\nSkills in Excel but missing from SkillDependencyGraph ({len(excel_not_in_graph)}):")
for s in excel_not_in_graph:
    print(f"  - {s}")

print(f"\nSkills in SkillDependencyGraph but missing from Excel ({len(graph_not_in_excel)}):")
for s in graph_not_in_excel:
    print(f"  - {s}")

# Potential aliases / near-matches
print("\n" + "="*50)
print("POTENTIAL ALIASES & SIMILAR SKILLS")
print("="*50)
potential_aliases = []
for es in excel_skills:
    for ds in seeded_db_skills:
        if es.lower() != ds.lower():
            if es.lower() in ds.lower() or ds.lower() in es.lower() or (es.replace('-', ' ') == ds.replace('-', ' ')):
                potential_aliases.append((es, ds, "Excel vs Seeded DB"))
    for gs in backend_graph_skills:
        if es.lower() != gs.lower():
            if (es.lower() in gs.lower() or gs.lower() in es.lower() or (es.replace('-', ' ') == gs.replace('-', ' '))) and (es, gs) not in [(a[0], a[1]) for a in potential_aliases]:
                potential_aliases.append((es, gs, "Excel vs Graph"))

for p in sorted(potential_aliases):
    print(f"  '{p[0]}' <---> '{p[1]}' ({p[2]})")

# Output JSON comparison report
diff_report = {
    "excel_skills_count": len(excel_skills),
    "seeded_db_skills_count": len(seeded_db_skills),
    "backend_graph_skills_count": len(backend_graph_skills),
    "excel_skills": excel_skills,
    "seeded_db_skills": seeded_db_skills,
    "backend_graph_skills": backend_graph_skills,
    "excel_not_in_seeded_db": excel_not_in_db,
    "seeded_db_not_in_excel": db_not_in_excel,
    "excel_not_in_graph": excel_not_in_graph,
    "graph_not_in_excel": graph_not_in_excel,
    "potential_aliases": potential_aliases
}

with open('scratch/skills_diff_report.json', 'w', encoding='utf-8') as f:
    json.dump(diff_report, f, indent=2)

print("\nWrote comparison report to scratch/skills_diff_report.json")
