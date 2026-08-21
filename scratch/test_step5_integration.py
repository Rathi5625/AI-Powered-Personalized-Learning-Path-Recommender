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

print("Starting Step 5 Live Course Recommendation Integration Tests...")

# Setup: Register and login test user
test_email = f"step5_user_{uuid.uuid4().hex[:8]}@example.com"
test_pwd = "SecurePassword123!"

s_status, s_body = make_req("/auth/signup", method="POST", data={
    "name": "Step5 Test Learner",
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

# TEST 1: GET /api/users/{userId}/recommendations?careerId={careerId}
r_status, r_body = make_req(f"/users/{user_id}/recommendations?careerId={career_id}&limit=10", headers={"Authorization": f"Bearer {token}"})
print(f"2. TEST 1 (GET /api/users/{{userId}}/recommendations): status={r_status}")
results["test_1_recommendations_status_200"] = (r_status == 200)

# TEST 2: Recommendation count
recs = r_body.get("recommendations", []) if isinstance(r_body, dict) else []
print(f"3. TEST 2 (Recommendation count): count={len(recs)}, totalCandidates={r_body.get('totalCandidateCourses')}, hasGaps={r_body.get('hasGaps')}")
results["test_2_recommendation_count"] = (len(recs) > 0 and r_body.get("totalCandidateCourses", 0) > 0)

# TEST 3: Real course identity
all_have_id_and_title = len(recs) > 0 and all(rec.get("courseId") and rec.get("courseTitle") for rec in recs)
print(f"4. TEST 3 (Real course identity): all_valid={all_have_id_and_title}")
results["test_3_real_course_identity"] = all_have_id_and_title

# TEST 4: Course fields
valid_fields = len(recs) > 0 and all(
    rec.get("provider") and rec.get("difficulty") and rec.get("finalScore") is not None
    for rec in recs
)
if recs:
    sample = recs[0]
    print(f"5. TEST 4 (Course fields sample):")
    print(f"   Rank: #{sample.get('rank')} | Title: '{sample.get('courseTitle')}'")
    print(f"   Provider: {sample.get('provider')} | Difficulty: {sample.get('difficulty')} | Type: {sample.get('courseType')}")
    print(f"   Price: ${sample.get('price')} (isFree={sample.get('isFree')}) | Rating: {sample.get('rating')}")
    print(f"   Final Score: {sample.get('finalScore')} (ruleBased={sample.get('ruleBasedScore')}, ml={sample.get('mlScore')})")
    print(f"   Gap Skills Addressed: {sample.get('gapSkillsAddressed')}")
    print(f"   URL: {sample.get('url')}")
results["test_4_course_fields"] = valid_fields

# TEST 5: Ranking order
scores = [rec.get("finalScore", 0) for rec in recs]
is_ranked = scores == sorted(scores, reverse=True)
ranks = [rec.get("rank") for rec in recs]
is_consecutive_ranks = ranks == list(range(1, len(recs) + 1))
print(f"6. TEST 5 (Ranking verification): is_sorted_scores={is_ranked}, ranks={ranks}")
results["test_5_ranking_preserved"] = (is_ranked and is_consecutive_ranks)

# TEST 6: Career context (gap skills addressed belong to career required skills)
g_status, g_body = make_req(f"/users/{user_id}/skill-gaps?careerId={career_id}", headers={"Authorization": f"Bearer {token}"})
gap_skill_names = {gap.get("skillName") for gap in g_body.get("gaps", [])} if isinstance(g_body, dict) else set()
addressed_skills = {s for rec in recs for s in rec.get("gapSkillsAddressed", [])}
overlap = addressed_skills.intersection(gap_skill_names)
print(f"7. TEST 6 (Career context & gap connection): career_gaps={len(gap_skill_names)}, addressed_skills={len(addressed_skills)}, overlap={len(overlap)}")
results["test_6_career_context_gap_connection"] = (len(overlap) > 0)

# TEST 7: Unauthorized request (no JWT)
unauth_status, unauth_body = make_req(f"/users/{user_id}/recommendations?careerId={career_id}")
print(f"8. TEST 7 (Unauthorized request without JWT): status={unauth_status}")
results["test_7_unauthorized_401"] = (unauth_status == 401)

# TEST 8: Cross-user request
random_uuid = str(uuid.uuid4())
cross_status, cross_body = make_req(f"/users/{random_uuid}/recommendations?careerId={career_id}", headers={"Authorization": f"Bearer {token}"})
print(f"9. TEST 8 (Cross-user request): status={cross_status}")
results["test_8_cross_user_403"] = (cross_status == 403)

print("\n--- SUMMARY OF STEP 5 INTEGRATION TESTS ---")
all_passed = True
for k, v in results.items():
    print(f"  {k}: {'PASS' if v else 'FAIL'}")
    if not v:
        all_passed = False

print(f"\nOverall Result: {'ALL PASS' if all_passed else 'SOME FAILED'}")

with open(r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\scratch\step5_results.json", "w") as out:
    json.dump(results, out, indent=2)
