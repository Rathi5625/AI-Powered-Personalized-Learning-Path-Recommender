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

print("Starting Step 4 Live Skill Gap & Skill Intelligence Integration Tests...")

# Setup: Register and login test user
test_email = f"step4_user_{uuid.uuid4().hex[:8]}@example.com"
test_pwd = "SecurePassword123!"

s_status, s_body = make_req("/auth/signup", method="POST", data={
    "name": "Step4 Test Learner",
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

# Fetch career ID
c_status, c_body = make_req("/careers?page=0&size=10&sortBy=title&sortDir=ASC", headers={"Authorization": f"Bearer {token}"})
careers = c_body.get("content", []) if isinstance(c_body, dict) else []
frontend_career = next((c for c in careers if "Frontend" in c.get("name", "")), careers[0])
career_id = frontend_career.get("id")
career_name = frontend_career.get("name")
print(f"1. Setup complete: user_id={user_id}, career='{career_name}' (id={career_id})")

# TEST 1: GET /api/users/{userId}/skill-gaps?careerId={careerId}
g_status, g_body = make_req(f"/users/{user_id}/skill-gaps?careerId={career_id}", headers={"Authorization": f"Bearer {token}"})
print(f"2. TEST 1 (GET /api/users/{{userId}}/skill-gaps): status={g_status}")
results["test_1_skill_gap_status_200"] = (g_status == 200)

# TEST 2: Verify response contains summary fields
has_summary_fields = all(k in g_body for k in [
    "userId", "careerName", "totalRequiredSkills", "skillsWithNoGap", 
    "partialGaps", "fullGaps", "overallGapScore", "gaps"
]) if isinstance(g_body, dict) else False
print(f"3. TEST 2 (Summary fields verification): has_all_fields={has_summary_fields}")
print(f"   totalRequiredSkills={g_body.get('totalRequiredSkills')}, partialGaps={g_body.get('partialGaps')}, fullGaps={g_body.get('fullGaps')}, overallGapScore={g_body.get('overallGapScore')}")
results["test_2_summary_fields"] = (has_summary_fields and g_body.get("totalRequiredSkills", 0) > 0)

# TEST 3: Verify each gap contains valid properties
gaps = g_body.get("gaps", []) if isinstance(g_body, dict) else []
valid_gaps = len(gaps) > 0 and all(
    gap.get("skillName") and gap.get("gapType") and gap.get("severity") and gap.get("priority")
    for gap in gaps
)
print(f"4. TEST 3 (Gap items validation): total_gaps={len(gaps)}, valid_gaps={valid_gaps}")
if gaps:
    sample = gaps[0]
    print(f"   Sample Gap: '{sample.get('skillName')}' | Category: {sample.get('skillCategory')} | Req: {sample.get('requiredProficiency')} | GapType: {sample.get('gapType')} | Severity: {sample.get('severity')} | Priority: {sample.get('priority')}")
results["test_3_gap_items_valid"] = valid_gaps

# TEST 4: Prerequisites GET /api/skills/dependencies/{skillName}
test_skill = gaps[0].get("skillName") if gaps else "React"
prereq_status, prereq_body = make_req(f"/skills/dependencies/{urllib.parse.quote(test_skill)}", headers={"Authorization": f"Bearer {token}"})
print(f"5. TEST 4 (Prerequisites for '{test_skill}'): status={prereq_status}")
if isinstance(prereq_body, dict):
    print(f"   Direct Prerequisites: {prereq_body.get('directPrerequisites')}")
    print(f"   Recursive Prerequisites: {prereq_body.get('recursivePrerequisites')}")
results["test_4_prerequisites"] = (prereq_status == 200 and "directPrerequisites" in prereq_body)

# TEST 5: Learning Order POST /api/skills/dependencies/learning-order
skills_to_order = [gap.get("skillName") for gap in gaps[:5]] if len(gaps) >= 2 else ["JavaScript", "HTML", "CSS", "React"]
order_status, order_body = make_req("/skills/dependencies/learning-order", method="POST", headers={"Authorization": f"Bearer {token}"}, data={
    "skills": skills_to_order
})
print(f"6. TEST 5 (Learning Order for {skills_to_order}): status={order_status}")
if isinstance(order_body, dict):
    print(f"   Learning Order Sequence: {order_body.get('learningOrder')}")
results["test_5_learning_order"] = (order_status == 200 and isinstance(order_body.get("learningOrder"), list))

# TEST 6: Unauthorized request (No JWT)
unauth_status, unauth_body = make_req(f"/users/{user_id}/skill-gaps?careerId={career_id}")
print(f"7. TEST 6 (Unauthorized request without JWT): status={unauth_status}")
results["test_6_unauthorized_401"] = (unauth_status == 401)

# TEST 7: Cross-user request (Attempting another user's skill gaps)
random_uuid = str(uuid.uuid4())
cross_status, cross_body = make_req(f"/users/{random_uuid}/skill-gaps?careerId={career_id}", headers={"Authorization": f"Bearer {token}"})
print(f"8. TEST 7 (Cross-user access): status={cross_status}")
results["test_7_cross_user_403"] = (cross_status == 403)

print("\n--- SUMMARY OF STEP 4 INTEGRATION TESTS ---")
all_passed = True
for k, v in results.items():
    print(f"  {k}: {'PASS' if v else 'FAIL'}")
    if not v:
        all_passed = False

print(f"\nOverall Result: {'ALL PASS' if all_passed else 'SOME FAILED'}")

with open(r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\scratch\step4_results.json", "w") as out:
    json.dump(results, out, indent=2)
