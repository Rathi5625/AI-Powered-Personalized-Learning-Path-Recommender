import os
import hashlib
import json

ROOT = r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender"
FRONTEND_DIR = os.path.join(ROOT, "frontend")

def get_file_hash(path):
    try:
        hasher = hashlib.md5()
        with open(path, "rb") as f:
            while chunk := f.read(8192):
                hasher.update(chunk)
        return hasher.hexdigest()
    except Exception as e:
        return f"ERROR: {e}"

def audit():
    frontend_files = []
    for root, dirs, files in os.walk(FRONTEND_DIR):
        for file in files:
            full_path = os.path.join(root, file)
            rel_path = os.path.relpath(full_path, ROOT)
            frontend_rel = os.path.relpath(full_path, FRONTEND_DIR)
            file_size = os.path.getsize(full_path)
            file_hash = get_file_hash(full_path)
            frontend_files.append({
                "full_path": full_path,
                "rel_path": rel_path.replace("\\", "/"),
                "frontend_rel": frontend_rel.replace("\\", "/"),
                "size": file_size,
                "hash": file_hash
            })

    # Collect all root files outside frontend
    root_files_map = {}
    for root_dir in ["backend", "database", "datasets", "docs", "ml-service", "scratch"]:
        dir_path = os.path.join(ROOT, root_dir)
        if os.path.exists(dir_path):
            for root, dirs, files in os.walk(dir_path):
                for file in files:
                    full_path = os.path.join(root, file)
                    rel_path = os.path.relpath(full_path, ROOT).replace("\\", "/")
                    root_files_map[rel_path] = {
                        "size": os.path.getsize(full_path),
                        "hash": get_file_hash(full_path)
                    }

    # Also top-level root files
    for item in os.listdir(ROOT):
        full_path = os.path.join(ROOT, item)
        if os.path.isfile(full_path):
            rel_path = item
            root_files_map[rel_path] = {
                "size": os.path.getsize(full_path),
                "hash": get_file_hash(full_path)
            }

    # Analyze comparison
    results = []
    for f in frontend_files:
        f_rel = f["frontend_rel"]
        # Check if f_rel exists directly in root
        match_type = "UNIQUE"
        matched_root_file = None
        
        # Exact relative path match in root (e.g. frontend/database/06_add_user_role.sql vs database/06_add_user_role.sql)
        if f_rel in root_files_map:
            matched_root_file = f_rel
            if root_files_map[f_rel]["hash"] == f["hash"]:
                match_type = "EXACT_DUPLICATE"
            else:
                match_type = "MODIFIED_DUPLICATE"
        else:
            # Check if same hash exists anywhere in root
            found_hash = False
            for r_path, r_info in root_files_map.items():
                if r_info["hash"] == f["hash"]:
                    match_type = "EXACT_DUPLICATE"
                    matched_root_file = r_path
                    found_hash = True
                    break
            if not found_hash:
                # Check if same filename exists in root
                fname = os.path.basename(f["full_path"])
                for r_path, r_info in root_files_map.items():
                    if os.path.basename(r_path) == fname:
                        match_type = "MODIFIED_DUPLICATE"
                        matched_root_file = r_path
                        break

        results.append({
            "frontend_file": f["rel_path"],
            "frontend_rel": f["frontend_rel"],
            "size": f["size"],
            "hash": f["hash"],
            "match_type": match_type,
            "matched_root_file": matched_root_file
        })

    with open(os.path.join(ROOT, "scratch", "frontend_audit_raw.json"), "w", encoding="utf-8") as out:
        json.dump(results, out, indent=2)

    print(f"Total frontend files scanned: {len(results)}")
    
    # Categorize
    categories = {
        "A_GENUINE_FRONTEND": [],
        "B_BACKEND": [],
        "C_DATABASE": [],
        "D_ML_SERVICE": [],
        "E_DOCUMENTATION": [],
        "F_CONFIG_ROOT": [],
        "G_DUPLICATE_ROOT": [],
        "H_UNKNOWN": []
    }
    
    for r in results:
        f_rel = r["frontend_rel"]
        if f_rel.startswith("backend/") or f_rel.endswith(".java") or "pom.xml" in f_rel or "mvnw" in f_rel:
            categories["B_BACKEND"].append(r)
        elif f_rel.startswith("database/") or f_rel.endswith(".sql"):
            categories["C_DATABASE"].append(r)
        elif f_rel.startswith("ml-service/") or "requirements.txt" in f_rel and "ml" in f_rel:
            categories["D_ML_SERVICE"].append(r)
        elif f_rel.startswith("docs/") or (f_rel.endswith(".md") and not f_rel == "README.md" and not f_rel.startswith("src/")):
            categories["E_DOCUMENTATION"].append(r)
        elif f_rel in [".env.example", ".gitignore", "README.md", ".vscode"]:
            categories["F_CONFIG_ROOT"].append(r)
        elif f_rel.startswith("src/") or f_rel.startswith("public/") or f_rel in ["package.json", "package-lock.json", "vite.config.ts", "tsconfig.json", "tsconfig.node.json", "index.html", "tailwind.config.js", "postcss.config.js", "eslint.config.js", ".eslintrc.json"]:
            categories["A_GENUINE_FRONTEND"].append(r)
        else:
            categories["H_UNKNOWN"].append(r)

    print("Summary by category:")
    for cat, items in categories.items():
        print(f"  {cat}: {len(items)} files")

if __name__ == "__main__":
    audit()
