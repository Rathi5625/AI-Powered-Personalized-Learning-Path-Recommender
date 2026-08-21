import urllib.request
import urllib.error
import json
import uuid

BASE_URL = "http://localhost:8080/api"

# 1. Signup brand new user without learning path
email = f"test_nopath_{uuid.uuid4().hex[:8]}@example.com"
pwd = "Password123!"

s_req = urllib.request.Request(
    f"{BASE_URL}/auth/signup",
    data=json.dumps({
        "name": "No Path User",
        "email": email,
        "password": pwd,
        "targetCareer": "Frontend Developer",
        "experienceLevel": "BEGINNER",
        "dailyLearningHours": 2,
        "learningStyle": "PRACTICAL",
        "preferredContentType": "VIDEO"
    }).encode("utf-8"),
    headers={"Content-Type": "application/json"},
    method="POST"
)

with urllib.request.urlopen(s_req) as resp:
    s_body = json.loads(resp.read().decode("utf-8"))

l_req = urllib.request.Request(
    f"{BASE_URL}/auth/login",
    data=json.dumps({
        "email": email,
        "password": pwd
    }).encode("utf-8"),
    headers={"Content-Type": "application/json"},
    method="POST"
)

with urllib.request.urlopen(l_req) as resp:
    body = json.loads(resp.read().decode("utf-8"))
    token = body.get("accessToken")
    user_id = body.get("user", {}).get("id")

print(f"Created & logged in user {email} (id={user_id})")

# 2. Call Dashboard directly
dash_req = urllib.request.Request(
    f"{BASE_URL}/users/{user_id}/dashboard",
    headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
    method="GET"
)

try:
    with urllib.request.urlopen(dash_req) as resp:
        print(f"Status: {resp.status}")
        print(f"Body: {resp.read().decode('utf-8')[:200]}")
except urllib.error.HTTPError as e:
    print(f"HTTP Error {e.code}:")
    print(e.read().decode("utf-8"))
