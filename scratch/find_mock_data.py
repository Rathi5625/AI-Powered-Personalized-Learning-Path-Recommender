import os
import re

SRC = r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\frontend\src"

mock_keywords = ["mock", "fake", "dummy", "hardcoded", "placeholder", "sample"]

findings = []
for root, dirs, files in os.walk(SRC):
    for file in files:
        if file.endswith(".ts") or file.endswith(".tsx"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                lines = f.readlines()
            for idx, line in enumerate(lines, 1):
                lower = line.lower()
                for kw in mock_keywords:
                    if kw in lower:
                        findings.append({
                            "file": os.path.relpath(path, SRC).replace("\\", "/"),
                            "line": idx,
                            "keyword": kw,
                            "snippet": line.strip()
                        })

print(f"Total keyword matches in frontend/src: {len(findings)}")
for f in findings:
    print(f"[{f['file']}:{f['line']}] ({f['keyword']}) -> {f['snippet']}")
