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

print("Starting Step 3 Live User Profile & Career Integration Tests...")

# 1. TEST A: Signup / Login real user
test_email = f"step3_user_{uuid.uuid4().hex[:8]}@example.com"
test_pwd = "SecurePassword123!"

s_status, s_body = make_req("/auth/signup", method="POST", data={
    "name": "Step3 Test Learner",
    "email": test_email,
    "password": test_pwd,
    "targetCareer": "Frontend Developer",
    "experienceLevel": "BEGINNER",
    "dailyLearningHours": 2,
    "learningStyle": "VISUAL",
    "preferredContentType": "VIDEO"
})
print(f"1. Signup: status={s_status}, body={s_body}")

l_status, l_body = make_req("/auth/login", method="POST", data={
    "email": test_email,
    "password": test_pwd
})
token = l_body.get("accessToken")
user_id = l_body.get("user", {}).get("id")
print(f"2. Login: status={l_status}, user_id={user_id}")

# GET /api/auth/me
me_status, me_body = make_req("/auth/me", headers={"Authorization": f"Bearer {token}"})
print(f"3. TEST A (/api/auth/me): status={me_status}, name={me_body.get('name')}, career={me_body.get('targetCareer')}")
results["test_a_auth_me"] = (me_status == 200 and me_body.get("name") == "Step3 Test Learner")

# 2. TEST B: Career Loading (GET /api/careers)
c_status, c_body = make_req("/careers?page=0&size=20&sortBy=title&sortDir=ASC", headers={"Authorization": f"Bearer {token}"})
career_list = c_body.get("content", []) if isinstance(c_body, dict) else []
print(f"4. TEST B (Career Loading): status={c_status}, count={len(career_list)}")
for c in career_list[:3]:
    print(f"   - {c.get('name')} (id={c.get('id')}, category={c.get('category')})")
results["test_b_career_loading"] = (c_status == 200 and len(career_list) > 0)

# Pick a career from the list (e.g. Data Scientist or Full Stack Engineer)
target_career = career_list[1] if len(career_list) > 1 else career_list[0]
chosen_career_id = target_career.get("id")
chosen_career_name = target_career.get("name")
print(f"   Selected career to assign: '{chosen_career_name}' (id={chosen_career_id})")

# 3. TEST C: Career Selection & Persistence (PUT /api/users/{userId})
update_status, update_body = make_req(f"/users/{user_id}", method="PUT", headers={"Authorization": f"Bearer {token}"}, data={
    "name": "Step3 Test Learner",
    "careerGoal": chosen_career_name,
    "experienceLevel": "INTERMEDIATE",
    "dailyLearningHours": 3,
    "learningStyle": "PRACTICAL",
    "preferredContentType": "INTERACTIVE_EXERCISE"
})
print(f"5. TEST C (User Update / Career Save): status={update_status}, updated_goal={update_body.get('careerGoal')}")
results["test_c_career_selection_persisted"] = (update_status == 200 and update_body.get("careerGoal") == chosen_career_name)

# 4. TEST D: Browser Refresh Simulation (GET /api/auth/me)
refresh_status, refresh_body = make_req("/auth/me", headers={"Authorization": f"Bearer {token}"})
print(f"6. TEST D (Refresh Persistence /auth/me): status={refresh_status}, targetCareer={refresh_body.get('targetCareer')}")
results["test_d_refresh_persisted"] = (refresh_status == 200 and refresh_body.get("targetCareer") == chosen_career_name)

# 5. TEST E: Profile Loading (GET /api/users/{userId})
p_status, p_body = make_req(f"/users/{user_id}", headers={"Authorization": f"Bearer {token}"})
print(f"7. TEST E (Profile Loading /users/{user_id}): status={p_status}, expLevel={p_body.get('experienceLevel')}, hours={p_body.get('dailyLearningHours')}")
results["test_e_profile_loading"] = (p_status == 200 and p_body.get("experienceLevel") == "INTERMEDIATE")

# 6. TEST F: Profile Update (Change fields and save)
f_update_status, f_update_body = make_req(f"/users/{user_id}", method="PUT", headers={"Authorization": f"Bearer {token}"}, data={
    "name": "Updated Step3 Learner Name",
    "careerGoal": chosen_career_name,
    "experienceLevel": "ADVANCED",
    "dailyLearningHours": 4,
    "learningStyle": "THEORETICAL",
    "preferredContentType": "BOOK"
})
print(f"8. TEST F (Profile Update): status={f_update_status}, new_name={f_update_body.get('name')}, new_exp={f_update_body.get('experienceLevel')}")
results["test_f_profile_update"] = (f_update_status == 200 and f_update_body.get("name") == "Updated Step3 Learner Name" and f_update_body.get("experienceLevel") == "ADVANCED")

# Verify refresh after profile update
post_refresh_status, post_refresh_body = make_req("/auth/me", headers={"Authorization": f"Bearer {token}"})
print(f"9. TEST F (Verify Post-Update /auth/me): name={post_refresh_body.get('name')}, expLevel={post_refresh_body.get('experienceLevel')}")
results["test_f_post_update_refresh"] = (post_refresh_body.get("name") == "Updated Step3 Learner Name")

# 7. TEST G: Unauthorized access (No JWT)
unauth_status, unauth_body = make_req(f"/users/{user_id}")
print(f"10. TEST G (Unauthorized access): status={unauth_status}")
results["test_g_unauthorized_401"] = (unauth_status == 401)

# 8. TEST H: Cross-user access (Attempting another random user ID)
other_uuid = str(uuid.uuid4())
cross_status, cross_body = make_req(f"/users/{other_uuid}", method="PUT", headers={"Authorization": f"Bearer {token}"}, data={
    "name": "Hacker Attempt",
    "careerGoal": "Cybersecurity",
    "experienceLevel": "EXPERT",
    "dailyLearningHours": 5,
    "learningStyle": "PRACTICAL",
    "preferredContentType": "VIDEO"
})
print(f"11. TEST H (Cross-user access): status={cross_status}, body={cross_body}")
results["test_h_cross_user_403"] = (cross_status == 403)

print("\n--- SUMMARY OF STEP 3 INTEGRATION TESTS ---")
all_passed = True
for k, v in results.items():
    print(f"  {k}: {'PASS' if v else 'FAIL'}")
    if not v:
        all_passed = False

print(f"\nOverall Result: {'ALL PASS' if all_passed else 'SOME FAILED'}")
