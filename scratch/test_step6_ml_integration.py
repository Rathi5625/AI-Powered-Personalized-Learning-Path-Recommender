import urllib.request
import urllib.error
import json
import uuid
import subprocess
import time
import os

ML_URL = "http://localhost:8000"
BACKEND_URL = "http://localhost:5173/api"
results = {}

def make_req(base_url, path, method="GET", data=None, headers=None):
    url = f"{base_url}{path}"
    h = {"Content-Type": "application/json"}
    if headers:
        h.update(headers)
    req_body = json.dumps(data).encode("utf-8") if data is not None else None
    req = urllib.request.Request(url, data=req_body, headers=h, method=method)
    
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
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

print("Starting Step 6 Live ML Personalized Ranking Integration Tests...")

# TEST 1: GET /health on ML service
h_status, h_body = make_req(ML_URL, "/health")
print(f"1. TEST 1 (ML /health): status={h_status}, body={h_body}")
results["test_1_ml_health_200"] = (h_status == 200 and h_body.get("status") == "UP" and h_body.get("model_loaded") is True)

# TEST 2: GET /model-info on ML service
m_status, m_body = make_req(ML_URL, "/model-info")
print(f"2. TEST 2 (ML /model-info): status={m_status}")
if isinstance(m_body, dict):
    print(f"   Model Type: {m_body.get('model_type')}, Version: {m_body.get('version')}")
    print(f"   Feature Count: {len(m_body.get('features', []))}")
    print(f"   Accuracy: {m_body.get('accuracy')}, ROC-AUC: {m_body.get('roc_auc')}, CV F1: {m_body.get('cv_f1_mean')}")
results["test_2_ml_model_info_200"] = (
    m_status == 200 and 
    m_body.get("version") == "1.0" and 
    len(m_body.get("features", [])) == 10
)

# TEST 3: POST /predict on ML service
sample_features = {
    "skill_gap_score": 0.85,
    "career_priority_score": 1.0,
    "skill_coverage": 0.75,
    "proficiency_gap": 0.70,
    "difficulty_match": 0.90,
    "course_rating": 0.95,
    "preference_match": 0.80,
    "mandatory_skill_match": 1.0,
    "course_duration_match": 0.90,
    "course_quality_score": 0.92
}
p_status, p_body = make_req(ML_URL, "/predict", method="POST", data=sample_features)
print(f"3. TEST 3 (ML /predict): status={p_status}, body={p_body}")
results["test_3_ml_predict_200"] = (
    p_status == 200 and 
    "recommendation_score" in p_body and 
    "recommendation_probability" in p_body
)

# Setup test user for backend recommendations
test_email = f"step6_ml_user_{uuid.uuid4().hex[:8]}@example.com"
test_pwd = "SecurePassword123!"

s_status, s_body = make_req(BACKEND_URL, "/auth/signup", method="POST", data={
    "name": "Step6 ML Learner",
    "email": test_email,
    "password": test_pwd,
    "targetCareer": "Frontend Developer",
    "experienceLevel": "BEGINNER",
    "dailyLearningHours": 2,
    "learningStyle": "PRACTICAL",
    "preferredContentType": "VIDEO"
})

l_status, l_body = make_req(BACKEND_URL, "/auth/login", method="POST", data={
    "email": test_email,
    "password": test_pwd
})
token = l_body.get("accessToken")
user_id = l_body.get("user", {}).get("id")

c_status, c_body = make_req(BACKEND_URL, "/careers?page=0&size=10&sortBy=title&sortDir=ASC", headers={"Authorization": f"Bearer {token}"})
careers = c_body.get("content", []) if isinstance(c_body, dict) else []
frontend_career = next((c for c in careers if "Frontend" in c.get("name", "")), careers[0])
career_id = frontend_career.get("id")

# TEST 4: Backend recommendation request with ML ONLINE (Hybrid Scoring)
r_status, r_body = make_req(BACKEND_URL, f"/users/{user_id}/recommendations?careerId={career_id}&limit=10", headers={"Authorization": f"Bearer {token}"})
recs_online = r_body.get("recommendations", []) if isinstance(r_body, dict) else []
print(f"4. TEST 4 (Backend Recommendations with ML ONLINE): status={r_status}, count={len(recs_online)}")
has_ml_score = False
if recs_online:
    sample = recs_online[0]
    print(f"   Sample #1: '{sample.get('courseTitle')}'")
    print(f"   Final Score: {sample.get('finalScore')} | Rule Score: {sample.get('ruleBasedScore')} | ML Score: {sample.get('mlScore')}")
    has_ml_score = sample.get("mlScore") is not None
results["test_4_hybrid_ml_online"] = (r_status == 200 and has_ml_score)

# TEST 5: Verify Formula: FinalScore = (0.70 * ruleScore) + (0.30 * mlScore)
if recs_online:
    sample = recs_online[0]
    rule_s = sample.get("ruleBasedScore", 0.0)
    ml_s = sample.get("mlScore", 0.0)
    expected_final = round((0.70 * rule_s) + (0.30 * ml_s), 1)
    actual_final = round(sample.get("finalScore", 0.0), 1)
    diff = abs(actual_final - expected_final)
    print(f"5. TEST 5 (Hybrid Formula Check): Expected={expected_final}, Actual={actual_final}, Diff={diff:.2f}")
    results["test_5_hybrid_formula_match"] = (diff <= 0.2)
else:
    results["test_5_hybrid_formula_match"] = False

# TEST 6: Ranking order (sorted descending by finalScore)
scores = [rec.get("finalScore", 0) for rec in recs_online]
is_ranked = scores == sorted(scores, reverse=True)
ranks = [rec.get("rank") for rec in recs_online]
is_consecutive = ranks == list(range(1, len(recs_online) + 1))
print(f"6. TEST 6 (Ranking Order): is_sorted={is_ranked}, ranks={ranks}")
results["test_6_ranking_order_valid"] = (is_ranked and is_consecutive)

# TEST 7: Unauthorized request
unauth_status, _ = make_req(BACKEND_URL, f"/users/{user_id}/recommendations?careerId={career_id}")
print(f"7. TEST 7 (Unauthorized Request): status={unauth_status}")
results["test_7_unauthorized_401"] = (unauth_status == 401)

# TEST 8: Cross-user access
random_uuid = str(uuid.uuid4())
cross_status, _ = make_req(BACKEND_URL, f"/users/{random_uuid}/recommendations?careerId={career_id}", headers={"Authorization": f"Bearer {token}"})
print(f"8. TEST 8 (Cross-user Access): status={cross_status}")
results["test_8_cross_user_403"] = (cross_status == 403)

print("\n--- SUMMARY OF STEP 6 ML INTEGRATION TESTS ---")
all_passed = True
for k, v in results.items():
    print(f"  {k}: {'PASS' if v else 'FAIL'}")
    if not v:
        all_passed = False

print(f"\nOverall Result: {'ALL PASS' if all_passed else 'SOME FAILED'}")

with open(r"C:\Users\parth\AI-Powered-Personalized-Learning-Path-Recommender\scratch\step6_results.json", "w") as out:
    json.dump(results, out, indent=2)
