import os

SRC = r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\frontend\src"

forbidden = ["GEMINI_API_KEY", "AIza", "JWT_SECRET", "DB_PASSWORD", "DATABASE_PASSWORD", "404E635266"]
findings = []

for root, dirs, files in os.walk(SRC):
    for f in files:
        if f.endswith(('.ts', '.tsx', '.js', '.html', '.css')):
            path = os.path.join(root, f)
            with open(path, 'r', encoding='utf-8') as file:
                content = file.read()
            for kw in forbidden:
                if kw in content:
                    findings.append((f, kw))

if not findings:
    print("ALL SECURITY CHECKS PASSED: 0 secrets found in frontend source code!")
else:
    print(f"SECURITY ALERT: Found secrets: {findings}")
