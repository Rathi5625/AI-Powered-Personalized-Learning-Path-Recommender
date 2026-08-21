import json
import os

with open(r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\scratch\frontend_audit_raw.json", "r", encoding="utf-8") as f:
    data = json.load(f)

print(f"Total files in frontend/: {len(data)}")

# Group by directory prefix in frontend/
groups = {}
for item in data:
    f_rel = item["frontend_rel"]
    parts = f_rel.split("/")
    prefix = parts[0] if len(parts) > 1 else "(root files)"
    groups.setdefault(prefix, []).append(item)

for prefix, items in sorted(groups.items()):
    print(f"\n--- {prefix} ({len(items)} files) ---")
    for it in items:
        print(f"  {it['frontend_rel']} | Size: {it['size']} B | Match: {it['match_type']} -> {it['matched_root_file']}")
