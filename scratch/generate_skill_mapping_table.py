import json
import openpyxl

wb = openpyxl.load_workbook('datasets/techbot.xlsx')
sheet = wb['Courses']
rows = list(sheet.iter_rows(values_only=True))
header = rows[0]

skills_in_excel = []
for r in rows[1:]:
    row_dict = dict(zip(header, r))
    skill_tag = str(row_dict.get("skill_tag", "")).strip()
    if skill_tag and skill_tag not in skills_in_excel:
        skills_in_excel.append(skill_tag)

# All canonical skills defined across the system
canonical_skills = {
    # Existing DB 25
    "Java": "Programming",
    "OOP": "Programming",
    "Data Structures & Algorithms": "Computer Science",
    "SQL": "Database",
    "Spring Boot": "Backend Framework",
    "REST APIs": "Web Services",
    "JPA/Hibernate": "Database",
    "Spring Security": "Security",
    "Testing": "DevOps & QA",
    "Docker": "DevOps",
    "HTML": "Frontend",
    "CSS": "Frontend",
    "JavaScript": "Frontend",
    "TypeScript": "Frontend",
    "React": "Frontend Framework",
    "Git": "Tools",
    "Python": "Programming",
    "Pandas": "Data Science",
    "NumPy": "Data Science",
    "Statistics": "Mathematics",
    "Data Visualization": "Data Science",
    "Machine Learning": "AI/ML",
    "Deep Learning": "AI/ML",
    "TensorFlow/PyTorch": "AI/ML Framework",
    "MLOps": "DevOps & ML",
    # Frontend Track Canonical Skills from DAG
    "Internet Basics": "Frontend",
    "CLI & Terminal Basics": "DevOps & Tools",
    "Version Control(Git & GitHub)": "Tools",
    "VCS Hosting": "Tools",
    "Package Managers": "Frontend",
    "CSS Frameworks": "Frontend",
    "JavaScript Frameworks": "Frontend",
    "AI-Assisted Coding": "AI/Tooling",
    "Generative AI for Frontend": "AI/Frontend",
    "Implementing AI in Frontend": "AI/Frontend",
    "Linters & formatters": "Tools",
    "Module Bundlers": "Frontend",
    "Auth Strategies": "Security",
    "Testing & Debugging": "QA",
    "Browser Web APIs": "Frontend",
    "Web Security": "Security",
    "Server-Side Rendering": "Frontend",
    "Static Site Generators": "Frontend",
    "Type Checkers": "Frontend",
    "Deployment": "DevOps",
    "Design Systems": "Frontend",
    "Performance": "Frontend",
    "Web Components": "Frontend",
    "GraphQL": "Web Services",
    "Accessibility": "Frontend",
    "Progressive Web Apps": "Frontend",
    "Mobile Apps": "Mobile",
    "Desktop Applications in JavaScript": "Desktop",
    # Backend Track Canonical Skills from DAG
    "Introduction to Backend Development": "Backend",
    "Frontend Basics": "Frontend",
    "Node.js Basics": "Backend",
    "Express.js (Web Framework)": "Backend",
    "REST APIs in Node": "Backend",
    "Testing (Node.js)": "QA",
    "Django or Flask (Web Framework)": "Backend",
    "REST APIs in Python": "Backend",
    "Testing (Python)": "QA",
    "Backend Track Completed (Milestone)": "Milestone",
    "Databases (SQL)": "Database",
    "NoSQL Databases": "Database",
    "Learn about Web Servers": "DevOps & Infra",
    "CI/CD Basics": "DevOps",
    "AI Assisted Coding": "AI/Tooling",
    "Learn the Basics (AI in Backend)": "AI/Backend",
    "AI Applications in Software Development": "AI/Backend",
    "Integration Patterns (For AI)": "AI/Backend",
    "Caching": "Backend",
    "Search Engines": "Backend",
    "Real Time Data": "Backend",
    "Message Brokers": "Backend",
    "Scaling Databases": "Database",
    "Architectural Patterns": "Architecture",
    "Building for Scale": "Architecture",
    "SQL Databases": "Database",
    "ORM / Hibernate": "Database",
    "Backend REST APIs": "Backend",
    "Data Analysis": "Data Science"
}

# Mapping table generator
mappings = []
for s in skills_in_excel:
    if s in canonical_skills:
        # Exact match
        mappings.append((s, s, "EXACT", 1.00, f"Exact match with canonical skill '{s}' in curriculum DAG."))
    elif s == "CSS Fundamentals":
        mappings.append((s, "CSS", "ALIAS", 1.00, "Foundational curriculum variant of canonical skill 'CSS'."))
    elif s == "HTML Fundamentals":
        mappings.append((s, "HTML", "ALIAS", 1.00, "Foundational curriculum variant of canonical skill 'HTML'."))
    elif s == "Internet Fundamentals":
        mappings.append((s, "Internet Basics", "ALIAS", 1.00, "Curriculum variant for canonical 'Internet Basics'."))
    elif s == "JavaScript Foundations":
        mappings.append((s, "JavaScript", "ALIAS", 1.00, "Foundational curriculum variant of canonical skill 'JavaScript'."))
    elif s == "Python Basics":
        mappings.append((s, "Python", "ALIAS", 1.00, "Foundational curriculum variant of canonical skill 'Python'."))
    else:
        mappings.append((s, s, "UNRESOLVED", 0.0, f"Unresolved skill: '{s}'"))

print(f"Total Mapped: {len(mappings)}")
exact_count = sum(1 for m in mappings if m[2] == 'EXACT')
alias_count = sum(1 for m in mappings if m[2] == 'ALIAS')
norm_count = sum(1 for m in mappings if m[2] == 'NORMALIZED_ALIAS')
amb_count = sum(1 for m in mappings if m[2] == 'AMBIGUOUS')
unres_count = sum(1 for m in mappings if m[2] == 'UNRESOLVED')

print(f"EXACT: {exact_count}")
print(f"ALIAS: {alias_count}")
print(f"NORMALIZED_ALIAS: {norm_count}")
print(f"AMBIGUOUS: {amb_count}")
print(f"UNRESOLVED: {unres_count}")
print("=" * 60)
for m in mappings:
    print(f"| {m[0]} | {m[1]} | {m[2]} | {m[3]:.2f} | {m[4]} |")
