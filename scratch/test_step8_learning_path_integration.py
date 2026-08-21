import urllib.request
import urllib.error
import json
import uuid
import re

BACKEND_URL = "http://localhost:5173/api"
results = {}

def make_req(path, method="GET", data=None, headers=None):
    url = f"{BACKEND_URL}{path}"
    h = {"Content-Type": "application/json"}
    if headers:
        h.update(headers)
    req_body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=req_body, headers=h, method=method)
    
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
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

print("Starting Step 8 Live Personalized Learning Path Integration Tests...")

# Setup test user
test_email = f"step8_lp_user_{uuid.uuid4().hex[:8]}@example.com"
test_pwd = "SecurePassword123!"

s_status, s_body = make_req("/auth/signup", method="POST", data={
    "name": "Step8 Path Learner",
    "email": test_email,
    "password": test_pwd,
    "targetCareer": "Frontend Developer",
    "experienceLevel": "BEGINNER",
    "dailyLearningHours": 2,
    "learningStyle": "PRACTICAL",
    "preferredContentType": "VIDEO"
})

l_status, l_body = make_req("/auth/login", method="POST", data={
    "email": test_email,
    "password": test_pwd
})
token = l_body.get("accessToken")
user_id = l_body.get("user", {}).get("id")

c_status, c_body = make_req("/careers?page=0&size=10&sortBy=title&sortDir=ASC", headers={"Authorization": f"Bearer {token}"})
careers = c_body.get("content", []) if isinstance(c_body, dict) else []
frontend_career = next((c for c in careers if "Frontend" in c.get("name", "")), careers[0])
career_id = frontend_career.get("id")
career_name = frontend_career.get("name")
print(f"1. Setup complete: user_id={user_id}, career='{career_name}' (id={career_id})")

# TEST 1: Generate Learning Path (POST /api/learning-paths/generate)
gen_status, gen_body = make_req("/learning-paths/generate", method="POST", headers={"Authorization": f"Bearer {token}"}, data={
    "userId": user_id,
    "careerId": career_id
})
print(f"2. TEST 1 (Generate Learning Path): status={gen_status}, success={gen_body.get('success')}")
results["test_1_generate_path_200"] = (gen_status == 200 and gen_body.get("success") is True)

# TEST 2: Verify Response Structure & Phases
phases = gen_body.get("phases", [])
has_valid_phases = (
    len(phases) > 0 and 
    all(
        phase.get("phaseNumber") and 
        phase.get("phaseTitle") and 
        isinstance(phase.get("targetSkills"), list) and
        isinstance(phase.get("courses"), list)
        for phase in phases
    )
)
print(f"3. TEST 2 (Response Structure): total_phases={len(phases)}, targetCareer={gen_body.get('targetCareer')}, valid_phases={has_valid_phases}")
if phases:
    p1 = phases[0]
    print(f"   Phase 1: '{p1.get('phaseTitle')}' | Duration: {p1.get('estimatedDuration')} | Skills: {p1.get('targetSkills')}")
results["test_2_response_structure"] = (has_valid_phases and gen_body.get("targetCareer") is not None)

# TEST 3: Course Validity (Every course in path matches a valid UUID and title)
uuid_pattern = re.compile(r'^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$', re.IGNORECASE)
all_courses = [c for p in phases for c in p.get("courses", [])]
valid_courses = (
    len(all_courses) > 0 and
    all(uuid_pattern.match(str(c.get("courseId", ""))) and c.get("courseTitle") for c in all_courses)
)
print(f"4. TEST 3 (Course Validity): total_courses={len(all_courses)}, all_valid_uuids={valid_courses}")
if all_courses:
    print(f"   Sample Course: '{all_courses[0].get('courseTitle')}' ({all_courses[0].get('provider')})")
results["test_3_course_validity"] = valid_courses

# TEST 4: Skill Validity (Target skills in phases are non-empty strings)
all_skills = [s for p in phases for s in p.get("targetSkills", [])]
valid_skills = len(all_skills) > 0 and all(isinstance(s, str) and len(s) > 0 for s in all_skills)
print(f"5. TEST 4 (Skill Validity): total_target_skills={len(all_skills)}, valid={valid_skills}")
results["test_4_skill_validity"] = valid_skills

# TEST 5: Active Path Retrieval (GET /api/users/{userId}/learning-paths/active)
act_status, act_body = make_req(f"/users/{user_id}/learning-paths/active", headers={"Authorization": f"Bearer {token}"})
print(f"6. TEST 5 (Active Path Retrieval): status={act_status}, totalPhases={act_body.get('totalPhases')}, status={act_body.get('status')}")
results["test_5_active_path_retrieval"] = (act_status == 200 and act_body.get("totalPhases", 0) > 0)

# TEST 6: Adapt Learning Path (POST /api/learning-paths/users/{userId}/adapt)
adapt_status, adapt_body = make_req(f"/learning-paths/users/{user_id}/adapt", method="POST", headers={"Authorization": f"Bearer {token}"}, data={
    "careerId": career_id
})
print(f"7. TEST 6 (Adapt Learning Path): status={adapt_status}, adapted={adapt_body.get('adapted')}, reason={adapt_body.get('changeReason')}")
results["test_6_adapt_path_200"] = (adapt_status == 200 and "adapted" in adapt_body)

# TEST 7: Unauthorized Request (No JWT)
unauth_status, _ = make_req(f"/users/{user_id}/learning-paths/active")
print(f"8. TEST 7 (Unauthorized Request): status={unauth_status}")
results["test_7_unauthorized_401"] = (unauth_status == 401)

# TEST 8: Cross-user Access
random_uuid = str(uuid.uuid4())
cross_status, _ = make_req(f"/users/{random_uuid}/learning-paths/active", headers={"Authorization": f"Bearer {token}"})
print(f"9. TEST 8 (Cross-user Access): status={cross_status}")
results["test_8_cross_user_403"] = (cross_status == 403)

print("\n--- SUMMARY OF STEP 8 LEARNING PATH INTEGRATION TESTS ---")
all_passed = True
for k, v in results.items():
    print(f"  {k}: {'PASS' if v else 'FAIL'}")
    if not v:
        all_passed = False

print(f"\nOverall Result: {'ALL PASS' if all_passed else 'SOME FAILED'}")

with open(r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\scratch\step8_results.json", "w") as out:
    json.dump(results, out, indent=2)
