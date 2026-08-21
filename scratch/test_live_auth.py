import urllib.request
import urllib.error
import json
import uuid

BASE_URL = "http://localhost:5173/api"

results = {}

def make_req(path, method="GET", data=None, headers=None):
    url = f"{BASE_URL}{path}"
    h = {"Content-Type": "application/json"}
    if headers:
        h.update(headers)
    req_body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=req_body, headers=h, method=method)
    
    try:
        with urllib.request.urlopen(req) as resp:
            status = resp.status
            body = resp.read().decode("utf-8")
            try:
                body_json = json.loads(body)
            except:
                body_json = body
            return status, body_json
    except urllib.error.HTTPError as e:
        err_body = e.read().decode("utf-8")
        try:
            err_json = json.loads(err_body)
        except:
            err_json = err_body
        return e.code, err_json
    except Exception as e:
        return 0, str(e)

print("Starting Live Auth Integration Tests...")

# Health Check
h_status, h_body = make_req("/health")
print(f"1. Health Check (via Vite Proxy): status={h_status}, body={h_body}")
results["proxy_health"] = (h_status == 200)

# TEST A: Invalid credentials
a_status, a_body = make_req("/auth/login", method="POST", data={
    "email": "admin@learnai.local",
    "password": "WrongPassword123!"
})
print(f"2. TEST A (Invalid credentials): status={a_status}, body={a_body}")
results["test_a_invalid_creds"] = (a_status in [400, 401, 403, 500] and "Invalid" in str(a_body))

# TEST B: Valid credentials (Admin login)
b_status, b_body = make_req("/auth/login", method="POST", data={
    "email": "admin@learnai.local",
    "password": "ChangeThisAdminPassword123!"
})
print(f"3. TEST B (Valid Admin Login): status={b_status}, token_type={b_body.get('tokenType') if isinstance(b_body, dict) else 'none'}")
admin_token = b_body.get("accessToken") if isinstance(b_body, dict) else None
admin_id = b_body.get("user", {}).get("id") if isinstance(b_body, dict) else None
results["test_b_admin_login"] = (b_status == 200 and admin_token is not None)

# TEST B2: Valid credentials (Signup + Login new user)
new_email = f"learner_{uuid.uuid4().hex[:8]}@example.com"
new_pwd = "StrongPassword123!"
s_status, s_body = make_req("/auth/signup", method="POST", data={
    "name": "Integration Test Learner",
    "email": new_email,
    "password": new_pwd,
    "targetCareer": "Frontend Developer",
    "experienceLevel": "BEGINNER",
    "dailyLearningHours": 3,
    "learningStyle": "PRACTICAL",
    "preferredContentType": "VIDEO"
})
print(f"4. TEST B2 (Signup New User): status={s_status}, body={s_body}")
results["test_b2_signup"] = (s_status == 201)

b2_status, b2_body = make_req("/auth/login", method="POST", data={
    "email": new_email,
    "password": new_pwd
})
learner_token = b2_body.get("accessToken") if isinstance(b2_body, dict) else None
learner_id = b2_body.get("user", {}).get("id") if isinstance(b2_body, dict) else None
print(f"5. TEST B2 (Login New User): status={b2_status}, user_id={learner_id}")
results["test_b2_login"] = (b2_status == 200 and learner_token is not None)

# TEST C: Protected API with Bearer token
c_status, c_body = make_req("/auth/me", headers={"Authorization": f"Bearer {learner_token}"})
print(f"6. TEST C (Protected /auth/me with Bearer token): status={c_status}, email={c_body.get('email') if isinstance(c_body, dict) else 'none'}")
results["test_c_auth_me"] = (c_status == 200 and isinstance(c_body, dict) and c_body.get("email") == new_email)

# Also test user-scoped protected endpoint
c2_status, c2_body = make_req(f"/users/{learner_id}", headers={"Authorization": f"Bearer {learner_token}"})
print(f"7. TEST C2 (User-scoped /users/{learner_id}): status={c2_status}")
results["test_c2_user_scoped"] = (c2_status == 200)

# TEST E: Expired / Invalid token
e_status, e_body = make_req("/auth/me", headers={"Authorization": "Bearer InvalidOrExpiredToken12345"})
print(f"8. TEST E (Invalid token): status={e_status}, body={e_body}")
results["test_e_invalid_token"] = (e_status == 401)

# TEST F: Cross-user authorization (Learner attempts to access another random user or Admin attempts without matching ID)
random_uuid = str(uuid.uuid4())
f_status, f_body = make_req(f"/users/{random_uuid}", headers={"Authorization": f"Bearer {learner_token}"})
print(f"9. TEST F (Cross-user access /users/{random_uuid}): status={f_status}, body={f_body}")
results["test_f_cross_user_forbidden"] = (f_status in [401, 403])

print("\n--- SUMMARY OF ALL TESTS ---")
for k, v in results.items():
    print(f"  {k}: {'PASS' if v else 'FAIL'}")

with open(r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\scratch\live_auth_results.json", "w") as out:
    json.dump(results, out, indent=2)
