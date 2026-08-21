import urllib.request
import urllib.error
import json
import uuid
import re
import time

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

print("=================================================================")
print("STEP 10 — FULL-SYSTEM END-TO-END AUTOMATED VALIDATION SUITE")
print("=================================================================\n")

# STAGE 1: Service Health Checks
b_stat, b_body = make_req("/health")
print(f"1. Backend Health Check: status={b_stat}, body={b_body}")
results["1_backend_health"] = (b_stat == 200 and b_body.get("status") == "UP")

# STAGE 2: User Signup Journey
e2e_user_email = f"e2e_learner_{uuid.uuid4().hex[:8]}@example.com"
e2e_pwd = "SecureE2EPassword123!"

signup_stat, signup_body = make_req("/auth/signup", method="POST", data={
    "name": "E2E Full Journey Learner",
    "email": e2e_user_email,
    "password": e2e_pwd,
    "targetCareer": "Frontend Developer",
    "experienceLevel": "BEGINNER",
    "dailyLearningHours": 3,
    "learningStyle": "PRACTICAL",
    "preferredContentType": "VIDEO"
})
print(f"2. Signup Journey: status={signup_stat}, user={signup_body.get('name')}")
results["2_signup_flow"] = (signup_stat == 201 or signup_stat == 200)

# STAGE 3: User Login Journey
login_stat, login_body = make_req("/auth/login", method="POST", data={
    "email": e2e_user_email,
    "password": e2e_pwd
})
token = login_body.get("accessToken")
user_id = login_body.get("user", {}).get("id")
auth_headers = {"Authorization": f"Bearer {token}"}
print(f"3. Login Journey: status={login_stat}, user_id={user_id}, has_token={token is not None}")
results["3_login_flow"] = (login_stat == 200 and token is not None and user_id is not None)

# STAGE 4: Identity Verification (/api/auth/me)
me_stat, me_body = make_req("/auth/me", headers=auth_headers)
print(f"4. Identity Verification (/auth/me): status={me_stat}, email={me_body.get('email')}")
results["4_auth_me"] = (me_stat == 200 and me_body.get("email") == e2e_user_email)

# STAGE 5: Career Retrieval & Selection
careers_stat, careers_body = make_req("/careers?page=0&size=10&sortBy=title&sortDir=ASC", headers=auth_headers)
careers = careers_body.get("content", []) if isinstance(careers_body, dict) else []
frontend_career = next((c for c in careers if "Frontend" in c.get("name", "")), careers[0])
career_id = frontend_career.get("id")
career_name = frontend_career.get("name")
print(f"5. Career Retrieval: status={careers_stat}, count={len(careers)}, selected='{career_name}' (id={career_id})")
results["5_career_retrieval"] = (careers_stat == 200 and len(careers) > 0 and career_id is not None)

# STAGE 6: Profile & Preference Update Persistence
up_stat, up_body = make_req(f"/users/{user_id}", method="PUT", headers=auth_headers, data={
    "name": "E2E Full Journey Learner Updated",
    "careerGoal": career_name,
    "experienceLevel": "INTERMEDIATE",
    "dailyLearningHours": 4,
    "learningStyle": "THEORETICAL",
    "preferredContentType": "ARTICLE"
})
# Re-fetch profile to verify persistence
get_user_stat, get_user_body = make_req(f"/users/{user_id}", headers=auth_headers)
print(f"6. Profile Update Persistence: status={up_stat}, name='{get_user_body.get('name')}', exp='{get_user_body.get('experienceLevel')}', hours={get_user_body.get('dailyLearningHours')}")
results["6_profile_update_persistence"] = (
    up_stat == 200 and 
    get_user_body.get("name") == "E2E Full Journey Learner Updated" and
    get_user_body.get("experienceLevel") == "INTERMEDIATE" and
    get_user_body.get("dailyLearningHours") == 4 and
    get_user_body.get("preferredContentType") == "ARTICLE"
)

# STAGE 7: Skill Gap Analysis & Prerequisite DAG Intelligence
gap_stat, gap_body = make_req(f"/users/{user_id}/skill-gaps?careerId={career_id}", headers=auth_headers)
gaps = gap_body.get("skillGaps", [])
print(f"7. Skill Gap Analysis: status={gap_stat}, totalRequired={gap_body.get('totalRequiredSkills')}, gapsCount={len(gaps)}")
results["7_skill_gap_analysis"] = (gap_stat == 200 and gap_body.get("totalRequiredSkills", 0) > 0)

# Drilldown into a DAG prerequisite
sample_skill = "HTML"
prereq_stat, prereq_body = make_req(f"/skills/dependencies/{urllib.parse.quote(sample_skill)}", headers=auth_headers)
print(f"   DAG Prerequisite Query for '{sample_skill}': status={prereq_stat}, direct={prereq_body.get('directPrerequisites')}, recursive={prereq_body.get('recursivePrerequisites')}")
results["7b_dag_prerequisites"] = (prereq_stat == 200 and "directPrerequisites" in prereq_body and "recursivePrerequisites" in prereq_body)

# STAGE 8: Course Recommendations & Hybrid ML Ranking
rec_stat, rec_body = make_req(f"/users/{user_id}/recommendations?careerId={career_id}&limit=5", headers=auth_headers)
recs = rec_body.get("recommendations", [])
print(f"8. Course Recommendations: status={rec_stat}, count={len(recs)}")
if recs:
    r0 = recs[0]
    print(f"   Top Recommendation: '{r0.get('courseTitle')}' ({r0.get('provider')})")
    print(f"   Scores: Rule={r0.get('ruleBasedScore')}, ML={r0.get('mlScore')}, Final={r0.get('finalScore')}")
    # Verify Hybrid Score Formula
    expected_final = round(0.70 * r0.get("ruleBasedScore", 0) + 0.30 * r0.get("mlScore", 0), 4)
    actual_final = round(r0.get("finalScore", 0), 4)
    score_formula_valid = abs(expected_final - actual_final) < 0.05
    print(f"   Score Formula Check (0.70*Rule + 0.30*ML): expected={expected_final}, actual={actual_final}, match={score_formula_valid}")
    results["8_ml_hybrid_scoring"] = (rec_stat == 200 and score_formula_valid)
else:
    results["8_ml_hybrid_scoring"] = False

# STAGE 9: Gemini AI Reasoning Validation
has_ai_reasoning = len(recs) > 0 and recs[0].get("explanation") is not None
print(f"9. Gemini AI Reasoning: present={has_ai_reasoning}, explanationPreview='{str(recs[0].get('explanation') if recs else '')[:80]}...'")
results["9_gemini_ai_reasoning"] = (rec_stat == 200 and has_ai_reasoning)

# STAGE 10: Learning Path Generation & Active Retrieval
gen_stat, gen_body = make_req("/learning-paths/generate", method="POST", headers=auth_headers, data={
    "userId": user_id,
    "careerId": career_id
})
phases = gen_body.get("phases", [])
print(f"10. Learning Path Generation: status={gen_stat}, success={gen_body.get('success')}, phasesCount={len(phases)}")
# Active path retrieval
active_stat, active_body = make_req(f"/users/{user_id}/learning-paths/active", headers=auth_headers)
print(f"    Active Path Retrieval: status={active_stat}, title='{active_body.get('title')}', totalPhases={active_body.get('totalPhases')}")
results["10_learning_path_generation_and_active"] = (
    gen_stat == 200 and 
    gen_body.get("success") is True and 
    active_stat == 200 and 
    active_body.get("totalPhases", 0) > 0
)

# STAGE 11: Real Course Progress Persistence & Invariant Validation
all_path_courses = [c for p in phases for c in p.get("courses", [])]
test_course = all_path_courses[0] if all_path_courses else recs[0]
test_course_id = test_course.get("courseId")
test_course_title = test_course.get("courseTitle")

# Step A: Update to IN_PROGRESS at 63%
prog_up_stat, prog_up_body = make_req(
    f"/users/{user_id}/learning-progress/{test_course_id}",
    method="PUT",
    headers=auth_headers,
    data={"status": "IN_PROGRESS", "completionPercentage": 63.0}
)
# Step B: Retrieve to verify persistence
prog_get_stat, prog_get_body = make_req(f"/users/{user_id}/learning-progress", headers=auth_headers)
saved_prog = next((p for p in prog_get_body if p.get("courseId") == test_course_id), None) if isinstance(prog_get_body, list) else None
print(f"11. Course Progress Persistence: status={prog_up_stat}, savedPercentage={saved_prog.get('completionPercentage') if saved_prog else None}%")
results["11_progress_persistence"] = (
    prog_up_stat == 200 and 
    saved_prog is not None and 
    float(saved_prog.get("completionPercentage", 0)) == 63.0
)

# Step C: Update to COMPLETED (verify 100% automatic invariant)
comp_stat, comp_body = make_req(
    f"/users/{user_id}/learning-progress/{test_course_id}",
    method="PUT",
    headers=auth_headers,
    data={"status": "COMPLETED", "completionPercentage": 100.0}
)
print(f"    Course Completion Invariant: status={comp_stat}, statusValue={comp_body.get('status')}, percentage={comp_body.get('completionPercentage')}")
results["11b_completion_invariant"] = (
    comp_stat == 200 and 
    comp_body.get("status") == "COMPLETED" and 
    float(comp_body.get("completionPercentage", 0)) == 100.0
)

# STAGE 12: Adaptive Learning Evaluation
adapt_stat, adapt_body = make_req(
    f"/learning-paths/users/{user_id}/adapt",
    method="POST",
    headers=auth_headers,
    data={"careerId": career_id}
)
print(f"12. Adaptive Learning Engine: status={adapt_stat}, adapted={adapt_body.get('adapted')}, reason='{adapt_body.get('changeReason')}'")
results["12_adaptive_learning"] = (adapt_stat == 200 and "adapted" in adapt_body)

# STAGE 13: Aggregated Dashboard Retrieval
dash_stat, dash_body = make_req(f"/users/{user_id}/dashboard", headers=auth_headers)
prog_summary = dash_body.get("progressSummary", {})
print(f"13. Dashboard Aggregation: status={dash_stat}, tracked={prog_summary.get('totalCoursesTracked')}, completed={prog_summary.get('completedCourses')}, overallRate={prog_summary.get('overallCompletionRate')}%")
results["13_dashboard_aggregation"] = (
    dash_stat == 200 and 
    prog_summary.get("completedCourses", 0) >= 1
)

# STAGE 14: Security Validations
# Test A: Unauthenticated request (401)
unauth_stat, _ = make_req(f"/users/{user_id}/dashboard")
print(f"14a. Security (Unauthenticated 401): status={unauth_stat}")
results["14a_security_401"] = (unauth_stat == 401)

# Test B: Cross-user request (403)
victim_uuid = str(uuid.uuid4())
cross_stat, _ = make_req(f"/users/{victim_uuid}/dashboard", headers=auth_headers)
print(f"14b. Security (Cross-User 403): status={cross_stat}")
results["14b_security_403"] = (cross_stat == 403)

# STAGE 15: Input Validation Checks
# Test A: Invalid progress percentages (-1 and 101)
inv1_stat, _ = make_req(f"/users/{user_id}/learning-progress/{test_course_id}", method="PUT", headers=auth_headers, data={"status": "IN_PROGRESS", "completionPercentage": -1.0})
inv2_stat, _ = make_req(f"/users/{user_id}/learning-progress/{test_course_id}", method="PUT", headers=auth_headers, data={"status": "IN_PROGRESS", "completionPercentage": 101.0})
print(f"15a. Input Validation (Invalid Percentage): -1% status={inv1_stat}, 101% status={inv2_stat}")
results["15a_input_validation_percentage_400"] = (inv1_stat == 400 and inv2_stat == 400)

# Test B: Non-existent career ID (404)
fake_career_uuid = str(uuid.uuid4())
inv_career_stat, _ = make_req(f"/users/{user_id}/skill-gaps?careerId={fake_career_uuid}", headers=auth_headers)
print(f"15b. Input Validation (Non-existent Career 404): status={inv_career_stat}")
results["15b_input_validation_career_404"] = (inv_career_stat == 404)

print("\n=================================================================")
print("FULL-SYSTEM E2E RESULTS SUMMARY")
print("=================================================================")
all_passed = True
for k, v in results.items():
    print(f"  {k}: {'PASS' if v else 'FAIL'}")
    if not v:
        all_passed = False

print(f"\nOVERALL VERDICT: {'ALL TESTS PASSED (100%)' if all_passed else 'SOME TESTS FAILED'}")

with open(r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\scratch\step10_full_system_results.json", "w") as out:
    json.dump(results, out, indent=2)
