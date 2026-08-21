import os
import json

DTO_DIR = r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\backend\learning-path-backend\src\main\java\com\learningpath\dto"

dtos = {}
for file in os.listdir(DTO_DIR):
    if file.endswith(".java"):
        with open(os.path.join(DTO_DIR, file), "r", encoding="utf-8") as f:
            dtos[file] = f.read()

print(f"Total DTOs: {len(dtos)}")
for name, content in dtos.items():
    fields = [line.strip() for line in content.splitlines() if "private " in line]
    print(f"\n--- {name} ---")
    for f in fields:
        print(f"  {f}")
