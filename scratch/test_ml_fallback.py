import urllib.request
import urllib.error
import json
import uuid

BACKEND_URL = "http://localhost:5173/api"

def test_ml_fallback():
    # 1. Login admin
    req_body = json.dumps({"email": "admin@learnai.local", "password": "ChangeThisAdminPassword123!"}).encode("utf-8")
    req = urllib.request.Request(f"{BACKEND_URL}/auth/login", data=req_body, headers={"Content-Type": "application/json"}, method="POST")
    with urllib.request.urlopen(req) as resp:
        body = json.loads(resp.read().decode("utf-8"))
        token = body.get("accessToken")
        user_id = body.get("user", {}).get("id")
    
    # 2. Get career
    req = urllib.request.Request(f"{BACKEND_URL}/careers?page=0&size=1", headers={"Authorization": f"Bearer {token}"})
    with urllib.request.urlopen(req) as resp:
        career_id = json.loads(resp.read().decode("utf-8"))["content"][0]["id"]
    
    # 3. Request recommendations while ML is offline
    req = urllib.request.Request(f"{BACKEND_URL}/users/{user_id}/recommendations?careerId={career_id}&limit=5", headers={"Authorization": f"Bearer {token}"})
    with urllib.request.urlopen(req) as resp:
        status = resp.status
        recs_body = json.loads(resp.read().decode("utf-8"))
        recs = recs_body.get("recommendations", [])
        print(f"Fallback recommendations: status={status}, count={len(recs)}")
        if recs:
            r0 = recs[0]
            print(f"Top fallback recommendation: '{r0.get('courseTitle')}', ruleScore={r0.get('ruleBasedScore')}, mlScore={r0.get('mlScore')}, finalScore={r0.get('finalScore')}")
            return status == 200 and len(recs) > 0 and (r0.get("mlScore") is None or r0.get("finalScore") == r0.get("ruleBasedScore"))
    return False

if __name__ == "__main__":
    success = test_ml_fallback()
    print(f"ML Fallback Verification: {'PASS' if success else 'FAIL'}")
