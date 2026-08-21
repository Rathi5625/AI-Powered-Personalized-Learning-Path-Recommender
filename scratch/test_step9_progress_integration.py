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

print("Starting Step 9 Live Learning Progress & Learner State Integration Tests...")

# Setup test user
test_email = f"step9_prog_user_{uuid.uuid4().hex[:8]}@example.com"
test_pwd = "SecurePassword123!"

s_status, s_body = make_req("/auth/signup", method="POST", data={
    "name": "Step9 Progress Learner",
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

# Generate learning path to obtain real course references
gen_status, gen_body = make_req("/learning-paths/generate", method="POST", headers={"Authorization": f"Bearer {token}"}, data={
    "userId": user_id,
    "careerId": career_id
})
phases = gen_body.get("phases", [])
courses = [c for p in phases for c in p.get("courses", [])]
if not courses:
    # Fallback to recommendations
    rec_status, rec_body = make_req(f"/users/{user_id}/recommendations?careerId={career_id}&limit=5", headers={"Authorization": f"Bearer {token}"})
    courses = [{"courseId": r.get("courseId"), "courseTitle": r.get("courseTitle")} for r in rec_body.get("recommendations", [])]

target_course = courses[0]
target_course_id = target_course.get("courseId")
target_course_title = target_course.get("courseTitle")
print(f"2. Target Course: '{target_course_title}' (id={target_course_id})")

# TEST 1: Get Initial Progress (GET /api/users/{userId}/learning-progress)
p1_status, p1_body = make_req(f"/users/{user_id}/learning-progress", headers={"Authorization": f"Bearer {token}"})
print(f"3. TEST 1 (Get Progress): status={p1_status}, records={len(p1_body) if isinstance(p1_body, list) else p1_body}")
results["test_1_get_progress_200"] = (p1_status == 200 and isinstance(p1_body, list))

# TEST 2: Update Progress (PUT /api/users/{userId}/learning-progress/{courseId})
up1_status, up1_body = make_req(
    f"/users/{user_id}/learning-progress/{target_course_id}",
    method="PUT",
    headers={"Authorization": f"Bearer {token}"},
    data={
        "status": "IN_PROGRESS",
        "completionPercentage": 50.0
    }
)
print(f"4. TEST 2 & 3 (Update Progress): status={up1_status}, saved_status={up1_body.get('status')}, percentage={up1_body.get('completionPercentage')}")
results["test_3_update_progress_200"] = (
    up1_status == 200 and 
    up1_body.get("status") == "IN_PROGRESS" and 
    float(up1_body.get("completionPercentage", 0)) == 50.0
)

# TEST 4: Progress Record Validity and Persistence (Fetch and Verify)
p2_status, p2_body = make_req(f"/users/{user_id}/learning-progress", headers={"Authorization": f"Bearer {token}"})
persisted = next((p for p in p2_body if p.get("courseId") == target_course_id), None) if isinstance(p2_body, list) else None
print(f"5. TEST 4 (Persistence): status={p2_status}, persisted_found={persisted is not None}")
if persisted:
    print(f"   Record details: id={persisted.get('progressId')}, course='{persisted.get('courseTitle')}', status={persisted.get('status')}, %={persisted.get('completionPercentage')}")
results["test_2_record_validity"] = (persisted is not None and persisted.get("courseTitle") is not None)
results["test_4_persistence"] = (
    persisted is not None and 
    persisted.get("status") == "IN_PROGRESS" and 
    float(persisted.get("completionPercentage", 0)) == 50.0
)

# TEST 5: Complete Course (Verify automatic 100% invariant)
up2_status, up2_body = make_req(
    f"/users/{user_id}/learning-progress/{target_course_id}",
    method="PUT",
    headers={"Authorization": f"Bearer {token}"},
    data={
        "status": "COMPLETED",
        "completionPercentage": 100.0
    }
)
print(f"6. TEST 5 (Complete Course): status={up2_status}, status={up2_body.get('status')}, %={up2_body.get('completionPercentage')}")
results["test_5_completion_state"] = (
    up2_status == 200 and 
    up2_body.get("status") == "COMPLETED" and 
    float(up2_body.get("completionPercentage", 0)) == 100.0
)

# TEST 6: Invalid Completion Percentages (Should be rejected with 400 Bad Request)
inv1_status, _ = make_req(
    f"/users/{user_id}/learning-progress/{target_course_id}",
    method="PUT",
    headers={"Authorization": f"Bearer {token}"},
    data={"status": "IN_PROGRESS", "completionPercentage": -5.0}
)
inv2_status, _ = make_req(
    f"/users/{user_id}/learning-progress/{target_course_id}",
    method="PUT",
    headers={"Authorization": f"Bearer {token}"},
    data={"status": "IN_PROGRESS", "completionPercentage": 105.0}
)
print(f"7. TEST 6 (Invalid Percentage Validation): -5% status={inv1_status}, 105% status={inv2_status}")
results["test_6_invalid_percentage_rejected_400"] = (inv1_status == 400 and inv2_status == 400)

# TEST 7: Unauthorized Request (No JWT)
unauth_status, _ = make_req(f"/users/{user_id}/learning-progress")
print(f"8. TEST 7 (Unauthorized Request): status={unauth_status}")
results["test_7_unauthorized_401"] = (unauth_status == 401)

# TEST 8: Cross-User Access
random_user_uuid = str(uuid.uuid4())
cross_status, _ = make_req(
    f"/users/{random_user_uuid}/learning-progress/{target_course_id}",
    method="PUT",
    headers={"Authorization": f"Bearer {token}"},
    data={"status": "IN_PROGRESS", "completionPercentage": 25.0}
)
print(f"9. TEST 8 (Cross-User Access): status={cross_status}")
results["test_8_cross_user_403"] = (cross_status == 403)

# TEST 9: Adaptive Compatibility (Call adapt after progress update)
adapt_status, adapt_body = make_req(
    f"/learning-paths/users/{user_id}/adapt",
    method="POST",
    headers={"Authorization": f"Bearer {token}"},
    data={"careerId": career_id}
)
print(f"10. TEST 9 (Adaptive Compatibility): status={adapt_status}, adapted={adapt_body.get('adapted')}, reason={adapt_body.get('changeReason')}")
results["test_9_adaptive_compatibility"] = (adapt_status == 200 and "adapted" in adapt_body)

# TEST 10: Dashboard Aggregation Verification
dash_status, dash_body = make_req(f"/users/{user_id}/dashboard", headers={"Authorization": f"Bearer {token}"})
prog_summary = dash_body.get("progressSummary", {})
print(f"11. TEST 10 (Dashboard Aggregation): status={dash_status}, completedCourses={prog_summary.get('completedCourses')}, rate={prog_summary.get('overallCompletionRate')}%")
results["test_10_dashboard_aggregation"] = (dash_status == 200 and prog_summary.get("completedCourses") == 1)

print("\n--- SUMMARY OF STEP 9 PROGRESS INTEGRATION TESTS ---")
all_passed = True
for k, v in results.items():
    print(f"  {k}: {'PASS' if v else 'FAIL'}")
    if not v:
        all_passed = False

print(f"\nOverall Result: {'ALL PASS' if all_passed else 'SOME FAILED'}")

with open(r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\scratch\step9_results.json", "w") as out:
    json.dump(results, out, indent=2)
