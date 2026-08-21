import os
import re

BACKEND_SRC = r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\backend\learning-path-backend\src\main\java"

controllers = []

for root, dirs, files in os.walk(BACKEND_SRC):
    for file in files:
        if file.endswith("Controller.java"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()
            
            # Extract base path
            base_mapping_match = re.search(r'@RequestMapping\(\s*["\']([^"\']+)["\']\s*\)', content)
            base_path = base_mapping_match.group(1) if base_mapping_match else ""
            
            # Find methods
            endpoints = []
            method_pattern = re.compile(r'@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\s*(\(\s*(?:value\s*=\s*)?["\']([^"\']*)["\']\s*\))?')
            
            lines = content.splitlines()
            for i, line in enumerate(lines):
                match = method_pattern.search(line)
                if match:
                    http_method = match.group(1).replace("Mapping", "").upper()
                    sub_path = match.group(3) if match.group(3) is not None else ""
                    full_endpoint = (base_path + sub_path).replace("//", "/")
                    
                    # Look ahead a few lines for method signature and return type
                    sig_lines = " ".join(lines[i:min(i+6, len(lines))])
                    ret_match = re.search(r'public\s+(ResponseEntity<[^>]+>|[\w<>, ]+)\s+(\w+)\s*\(([^)]*)\)', sig_lines)
                    ret_type = ret_match.group(1) if ret_match else "unknown"
                    meth_name = ret_match.group(2) if ret_match else "unknown"
                    params = ret_match.group(3) if ret_match else ""
                    
                    # Check security annotations
                    pre_auth = re.search(r'@PreAuthorize\(\s*["\']([^"\']+)["\']\s*\)', "\n".join(lines[max(0, i-5):i+1]))
                    auth_req = pre_auth.group(1) if pre_auth else "Default/Authenticated (check SecurityConfig)"
                    
                    endpoints.append({
                        "http_method": http_method,
                        "sub_path": sub_path,
                        "full_endpoint": full_endpoint,
                        "method_name": meth_name,
                        "return_type": ret_type,
                        "params": params.strip(),
                        "auth": auth_req
                    })
            
            controllers.append({
                "file": file,
                "path": path,
                "base_path": base_path,
                "endpoints": endpoints
            })

print(f"Total controllers found: {len(controllers)}")
for c in controllers:
    print(f"\n=== {c['file']} (Base: {c['base_path']}) ===")
    for ep in c["endpoints"]:
        print(f"  {ep['http_method']} {ep['full_endpoint']} -> {ep['method_name']}({ep['params']}) : {ep['return_type']}")
