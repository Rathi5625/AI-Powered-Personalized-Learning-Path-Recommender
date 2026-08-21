import os
import re

BACKEND_SRC = r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\backend\learning-path-backend\src\main\java"

all_dtos = []
for root, dirs, files in os.walk(BACKEND_SRC):
    for file in files:
        if file.endswith("Response.java") or file.endswith("Request.java") or file.endswith("DTO.java") or file.endswith("Dto.java") or "dto" in root.lower():
            if file.endswith(".java"):
                path = os.path.join(root, file)
                with open(path, "r", encoding="utf-8") as f:
                    content = f.read()
                
                # Extract fields
                fields = []
                for line in content.splitlines():
                    trimmed = line.strip()
                    if (trimmed.startswith("private ") or trimmed.startswith("public ")) and "(" not in trimmed and "class " not in trimmed and "enum " not in trimmed:
                        fields.append(trimmed.replace(";", ""))
                
                # Extract package
                pkg_match = re.search(r'package\s+([\w.]+);', content)
                pkg = pkg_match.group(1) if pkg_match else ""
                
                all_dtos.append({
                    "class_name": file.replace(".java", ""),
                    "package": pkg,
                    "fields": fields
                })

print(f"Total DTO / Request / Response classes found: {len(all_dtos)}")
for d in sorted(all_dtos, key=lambda x: x["class_name"]):
    print(f"\n{d['class_name']} ({d['package']}):")
    for f in d["fields"]:
        print(f"  - {f}")
