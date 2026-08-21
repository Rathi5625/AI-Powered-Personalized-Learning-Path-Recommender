import urllib.request
import json
import uuid

BACKEND_URL = "http://localhost:5173/api"

# Login
test_email = f"step6_fallback_{uuid.uuid4().hex[:8]}@example.com"
test_pwd = "SecurePassword123!"

def make_req(path, method="GET", data=None, headers=None):
    url = f"{BACKEND_URL}{path}"
    h = {"Content-Type": "application/json"}
    if headers:
        h.update(headers)
    req_body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=req_body, headers=h, method=method)
    with urllib.request.urlopen(req, timeout=5) as resp:
        return resp.status, json.loads(resp.read().decode("utf-8"))

_, s_body = make_req("/auth/signup", method="POST", data={
    "name": "Fallback Learner",
    "email": test_email,
    "password": test_pwd,
    "targetCareer": "Frontend Developer",
    "experienceLevel": "BEGINNER"
})
_, l_body = make_req("/auth/login", method="POST", data={"email": test_email, "password": test_pwd})
token = l_body.get("accessToken")
user_id = l_body.get("user", {}).get("id")

_, c_body = make_req("/careers?page=0&size=10&sortBy=title&sortDir=ASC", headers={"Authorization": f"Bearer {token}"})
career_id = c_body.get("content", [])[0].get("id")

# Verify normal recommendation works with ML Online
status, body = make_req(f"/users/{user_id}/recommendations?careerId={career_id}&limit=5", headers={"Authorization": f"Bearer {token}"})
recs = body.get("recommendations", [])
print(f"Fallback verification test completed: status={status}, totalRecs={len(recs)}")
for r in recs:
    print(f"  #{r.get('rank')} {r.get('courseTitle')}: Final={r.get('finalScore')}, Rule={r.get('ruleBasedScore')}, ML={r.get('mlScore')}")
