import urllib.request
import urllib.error
import json

BASE_URL = "http://localhost:8080/api"

# 1. Login as admin
login_req = urllib.request.Request(
    f"{BASE_URL}/auth/login",
    data=json.dumps({
        "email": "admin@learnai.local",
        "password": "ChangeThisAdminPassword123!"
    }).encode("utf-8"),
    headers={"Content-Type": "application/json"},
    method="POST"
)

try:
    with urllib.request.urlopen(login_req) as resp:
        body = json.loads(resp.read().decode("utf-8"))
        token = body.get("accessToken")
        user = body.get("user", {})
        user_id = user.get("id")
        print(f"Logged in user: {user.get('email')}, id={user_id}, targetCareer={user.get('targetCareer')}")
except Exception as e:
    print(f"Login failed: {e}")
    exit(1)

# 2. Call Dashboard endpoint
dash_req = urllib.request.Request(
    f"{BASE_URL}/users/{user_id}/dashboard",
    headers={
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    },
    method="GET"
)

try:
    with urllib.request.urlopen(dash_req) as resp:
        print(f"Dashboard response status: {resp.status}")
        print(f"Dashboard body: {resp.read().decode('utf-8')[:300]}")
except urllib.error.HTTPError as e:
    print(f"Dashboard request failed with HTTP {e.code}:")
    print(e.read().decode("utf-8"))
except Exception as e:
    print(f"Dashboard request exception: {e}")
