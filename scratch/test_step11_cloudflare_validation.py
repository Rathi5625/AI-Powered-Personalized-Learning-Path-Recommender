"""
Step 11 — Public Cloudflare Full-Stack Validation Suite
Tests all 11+ required endpoints and full learner lifecycle directly over the public Cloudflare Tunnel URL.
"""

import json
import urllib.request
import urllib.error
import uuid
import sys

CLOUDFLARE_BASE = "https://drew-order-staffing-topics.trycloudflare.com"

def make_request(path, method="GET", body=None, token=None, expected_status=200):
    url = f"{CLOUDFLARE_BASE}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
        
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    
    try:
        with urllib.request.urlopen(req, timeout=25) as resp:
            status = resp.status
            content = resp.read().decode("utf-8")
            parsed = json.loads(content) if content else {}
            assert status == expected_status, f"Expected {expected_status}, got {status}"
            return status, parsed
    except urllib.error.HTTPError as e:
        content = e.read().decode("utf-8")
        parsed = {}
        try:
            parsed = json.loads(content)
        except Exception:
            parsed = {"raw": content}
        if e.code == expected_status:
            return e.code, parsed
        print(f"[-] HTTP Error on {method} {path}: {e.code} - {content}")
        raise e
    except Exception as e:
        print(f"[-] Network/Client Error on {method} {path}: {e}")
        raise e

def main():
    print("=" * 70)
    print("STEP 11 — PUBLIC CLOUDFLARE FULL-STACK VALIDATION SUITE")
    print(f"Target Public Base: {CLOUDFLARE_BASE}")
    print("=" * 70)
    
    results = {}
    
    # 1. Frontend & Backend Health check over Cloudflare Tunnel
    print("\n1. Validating Public Frontend & Backend Health...")
    status, health = make_request("/api/health")
    assert health.get("status") == "UP", "Backend health not UP"
    print(f"   [+] Backend /api/health: {status} -> {health}")
    results["1_public_health"] = "PASS"
    
    # 2. Signup through public tunnel
    unique_id = uuid.uuid4().hex[:8]
    email = f"cloudflare_learner_{unique_id}@example.com"
    password = "TestPassword123!"
    print(f"\n2. Testing POST /api/auth/signup for {email}...")
    status, signup_resp = make_request("/api/auth/signup", method="POST", body={
        "name": "Cloudflare Public Learner",
        "email": email,
        "password": password,
        "targetCareer": "Frontend Developer",
        "experienceLevel": "BEGINNER",
        "dailyLearningHours": 2,
        "learningStyle": "PRACTICAL",
        "preferredContentType": "VIDEO"
    }, expected_status=201)
    assert "userId" in signup_resp, "No userId returned on signup"
    user_id = signup_resp["userId"]
    print(f"   [+] Signup Success: status={status}, userId={user_id}")
    results["2_public_signup"] = "PASS"
    
    # 3. Login through public tunnel (POST /api/auth/login)
    print(f"\n3. Testing POST /api/auth/login for {email}...")
    status, login_resp = make_request("/api/auth/login", method="POST", body={
        "email": email,
        "password": password
    })
    assert "accessToken" in login_resp, "No accessToken in login response"
    token = login_resp["accessToken"]
    print(f"   [+] Login Success: status={status}, token_len={len(token)}")
    results["3_public_login"] = "PASS"
    
    # 4. Identity Verification (GET /api/auth/me)
    print("\n4. Testing GET /api/auth/me...")
    status, me_resp = make_request("/api/auth/me", token=token)
    assert me_resp["email"] == email, "Authenticated email mismatch"
    print(f"   [+] Identity Verified: status={status}, name={me_resp['name']}, role={me_resp['role']}")
    results["4_public_auth_me"] = "PASS"
    
    # 5. Career Catalog Retrieval (GET /api/careers)
    print("\n5. Testing GET /api/careers...")
    status, careers_page = make_request("/api/careers")
    careers = careers_page.get("content", [])
    assert len(careers) > 0, "No careers found in catalog"
    career = next((c for c in careers if c.get("name") == "Frontend Developer" or c.get("title") == "Frontend Developer"), careers[0])
    career_id = career["id"]
    career_name = career.get("name", career.get("title"))
    print(f"   [+] Career Retrieved: status={status}, total={len(careers)}, selected='{career_name}' ({career_id})")
    results["5_public_careers"] = "PASS"
    
    # 6. User Profile Update (PUT /api/users/{userId})
    print(f"\n6. Testing PUT /api/users/{user_id}...")
    status, update_resp = make_request(f"/api/users/{user_id}", method="PUT", token=token, body={
        "name": "Cloudflare Public Learner Updated",
        "targetCareer": "Frontend Developer",
        "experienceLevel": "INTERMEDIATE",
        "dailyLearningHours": 4,
        "learningStyle": "THEORETICAL",
        "preferredContentType": "INTERACTIVE_EXERCISE"
    })
    assert update_resp["name"] == "Cloudflare Public Learner Updated"
    print(f"   [+] Profile Updated: status={status}, newExperience={update_resp['experienceLevel']}")
    results["6_public_profile_update"] = "PASS"
    
    # 7. Dashboard Query before path generation (GET /api/users/{userId}/dashboard)
    print(f"\n7. Testing GET /api/users/{user_id}/dashboard (Initial State)...")
    status, dash_init = make_request(f"/api/users/{user_id}/dashboard", token=token)
    assert dash_init["activeLearningPath"] is None, "Expected null active learning path before generation"
    print(f"   [+] Dashboard Initial: status={status}, activePath={dash_init['activeLearningPath']}, rollBackError=NONE")
    results["7_public_dashboard_initial"] = "PASS"
    
    # 8. Skill Gap Analysis (GET /api/users/{userId}/skill-gaps)
    print(f"\n8. Testing GET /api/users/{user_id}/skill-gaps...")
    status, skill_gaps = make_request(f"/api/users/{user_id}/skill-gaps?careerId={career_id}", token=token)
    assert "totalRequiredSkills" in skill_gaps, "Invalid skill gap payload"
    print(f"   [+] Skill Gap Analysis: status={status}, totalReq={skill_gaps['totalRequiredSkills']}, gapsCount={len(skill_gaps['gaps'])}")
    results["8_public_skill_gaps"] = "PASS"
    
    # 9. Course Recommendations with ML Ranking & Gemini Reasoning (GET /api/users/{userId}/recommendations)
    print(f"\n9. Testing GET /api/users/{user_id}/recommendations...")
    status, recs = make_request(f"/api/users/{user_id}/recommendations?careerId={career_id}&limit=5", token=token)
    items = recs.get("recommendations", [])
    assert len(items) > 0, "No recommendations returned"
    r0 = items[0]
    print(f"   [+] Recommendations: count={len(items)}, top='{r0['courseTitle']}' ({r0['provider']})")
    print(f"       Scores: Rule={r0['ruleBasedScore']}, ML={r0['mlScore']}, Final={r0['finalScore']}")
    print(f"       AI Reasoning: '{r0['explanation'][:80]}...'")
    assert r0["ruleBasedScore"] is not None and r0["mlScore"] is not None, "Missing score components"
    results["9_public_recommendations"] = "PASS"
    
    # 10. Personalized Learning Path Generation (POST /api/learning-paths/generate)
    print("\n10. Testing POST /api/learning-paths/generate...")
    status, lp_gen = make_request("/api/learning-paths/generate", method="POST", token=token, body={
        "userId": user_id,
        "careerId": career_id
    })
    assert len(lp_gen.get("phases", [])) > 0, "No phases generated"
    print(f"    [+] Learning Path Generated: status={status}, career='{lp_gen['targetCareer']}', phases={len(lp_gen['phases'])}")
    results["10_public_lp_generate"] = "PASS"
    
    # 11. Active Learning Path Retrieval (GET /api/users/{userId}/learning-paths/active)
    print(f"\n11. Testing GET /api/users/{user_id}/learning-paths/active...")
    status, active_lp = make_request(f"/api/users/{user_id}/learning-paths/active", token=token)
    assert active_lp["status"] == "ACTIVE", "Path is not ACTIVE"
    first_course_id = active_lp["phases"][0]["courses"][0]["courseId"]
    first_course_title = active_lp["phases"][0]["courses"][0]["courseTitle"]
    print(f"    [+] Active Path: status={status}, totalCourses={active_lp['totalCourses']}, firstCourse='{first_course_title}' ({first_course_id})")
    results["11_public_active_lp"] = "PASS"
    
    # 12. Learning Progress Upsert (PUT /api/users/{userId}/learning-progress/{courseId})
    print(f"\n12. Testing PUT /api/users/{user_id}/learning-progress/{first_course_id}...")
    status, prog_resp = make_request(f"/api/users/{user_id}/learning-progress/{first_course_id}", method="PUT", token=token, body={
        "status": "COMPLETED",
        "completionPercentage": 100
    })
    assert prog_resp["status"] == "COMPLETED" and prog_resp["completionPercentage"] == 100
    print(f"    [+] Progress Saved: status={status}, courseStatus={prog_resp['status']}, completion={prog_resp['completionPercentage']}%")
    results["12_public_progress_put"] = "PASS"
    
    # 13. Learning Progress Retrieval (GET /api/users/{userId}/learning-progress)
    print(f"\n13. Testing GET /api/users/{user_id}/learning-progress...")
    status, prog_list = make_request(f"/api/users/{user_id}/learning-progress", token=token)
    assert len(prog_list) >= 1, "Expected at least 1 progress record"
    print(f"    [+] Progress List: status={status}, count={len(prog_list)}, completed={prog_list[0]['status']}")
    results["13_public_progress_get"] = "PASS"
    
    # 14. Adaptive Learning Engine (POST /api/learning-paths/users/{userId}/adapt)
    print(f"\n14. Testing POST /api/learning-paths/users/{user_id}/adapt...")
    status, adapt_resp = make_request(f"/api/learning-paths/users/{user_id}/adapt", method="POST", token=token, body={
        "careerId": career_id
    })
    assert "adapted" in adapt_resp, "Invalid adapt response"
    print(f"    [+] Path Adapted: status={status}, adapted={adapt_resp['adapted']}, reason='{adapt_resp.get('changeReason')}'")
    results["14_public_adapt"] = "PASS"
    
    # 15. Dashboard Synchronization with Active Path & Progress (GET /api/users/{userId}/dashboard)
    print(f"\n15. Testing GET /api/users/{user_id}/dashboard (Synchronized State)...")
    status, dash_synced = make_request(f"/api/users/{user_id}/dashboard", token=token)
    assert dash_synced["activeLearningPath"] is not None, "Expected active learning path on dashboard"
    assert dash_synced["progressSummary"]["totalCoursesTracked"] >= 1, "Expected progress tracking on dashboard"
    print(f"    [+] Dashboard Synced: status={status}, tracked={dash_synced['progressSummary']['totalCoursesTracked']}, overallRate={dash_synced['progressSummary']['overallCompletionRate']}%")
    results["15_public_dashboard_synced"] = "PASS"
    
    # 16. Security Validation over Public Tunnel
    print("\n16. Testing Security Invariants over Public Cloudflare Tunnel...")
    # 16a. Unauthenticated access rejected with 401
    status_401, err_401 = make_request("/api/auth/me", token=None, expected_status=401)
    assert status_401 == 401, f"Expected 401, got {status_401}"
    print(f"    [+] Unauthenticated Protected Route: status={status_401} (Unauthorized)")
    
    # 16b. Cross-User access rejected with 403
    foreign_user_id = str(uuid.uuid4())
    status_403, err_403 = make_request(f"/api/users/{foreign_user_id}/dashboard", token=token, expected_status=403)
    assert status_403 == 403, f"Expected 403, got {status_403}"
    print(f"    [+] Cross-User Unauthorized Route: status={status_403} (Forbidden)")
    results["16_public_security"] = "PASS"
    
    print("\n" + "=" * 70)
    print("STEP 11 PUBLIC CLOUDFLARE FULL-STACK VALIDATION RESULTS")
    print("=" * 70)
    for test, res in results.items():
        print(f"  {test:40s}: {res}")
    print("\nOVERALL PUBLIC FULL-STACK VERDICT: ALL TESTS PASSED (100%)")
    print("=" * 70)

if __name__ == "__main__":
    main()
