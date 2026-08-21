import urllib.request
import urllib.error
import json

ORIGIN = "https://buf-correct-pad-booking.trycloudflare.com"
BASE_URL = "http://localhost:8080/api"

print("=================================================================")
print("TESTING CLOUDFLARE ORIGIN CORS & AUTHENTICATION")
print(f"Origin: {ORIGIN}")
print("=================================================================\n")

# 1. Test Preflight OPTIONS /api/auth/login
req = urllib.request.Request(
    f"{BASE_URL}/auth/login",
    headers={
        "Origin": ORIGIN,
        "Access-Control-Request-Method": "POST",
        "Access-Control-Request-Headers": "Content-Type,Authorization"
    },
    method="OPTIONS"
)
try:
    with urllib.request.urlopen(req) as resp:
        print(f"1. Preflight OPTIONS /api/auth/login: status={resp.status}")
        print(f"   Access-Control-Allow-Origin: {resp.headers.get('Access-Control-Allow-Origin')}")
        print(f"   Access-Control-Allow-Credentials: {resp.headers.get('Access-Control-Allow-Credentials')}")
        print(f"   Access-Control-Allow-Methods: {resp.headers.get('Access-Control-Allow-Methods')}")
        opts_pass = (resp.status == 200 and resp.headers.get('Access-Control-Allow-Origin') == ORIGIN)
except Exception as e:
    print(f"1. Preflight OPTIONS failed: {e}")
    opts_pass = False

# 2. Test Valid Login with Origin header
login_data = json.dumps({
    "email": "admin@learnai.local",
    "password": "ChangeThisAdminPassword123!"
}).encode("utf-8")

req = urllib.request.Request(
    f"{BASE_URL}/auth/login",
    data=login_data,
    headers={
        "Content-Type": "application/json",
        "Origin": ORIGIN
    },
    method="POST"
)
try:
    with urllib.request.urlopen(req) as resp:
        body = json.loads(resp.read().decode("utf-8"))
        token = body.get("accessToken")
        user = body.get("user", {})
        print(f"\n2. Valid Login with Cloudflare Origin: status={resp.status}")
        print(f"   Access-Control-Allow-Origin: {resp.headers.get('Access-Control-Allow-Origin')}")
        print(f"   JWT token present: {token is not None}")
        print(f"   User: {user.get('email')} (role: {user.get('role')})")
        login_pass = (resp.status == 200 and token is not None and resp.headers.get('Access-Control-Allow-Origin') == ORIGIN)
except Exception as e:
    print(f"2. Valid Login failed: {e}")
    login_pass = False
    token = None

# 3. Test Invalid Login with Cloudflare Origin (Verify bad password rejected)
bad_login_data = json.dumps({
    "email": "admin@learnai.local",
    "password": "WrongPassword123!"
}).encode("utf-8")

req = urllib.request.Request(
    f"{BASE_URL}/auth/login",
    data=bad_login_data,
    headers={
        "Content-Type": "application/json",
        "Origin": ORIGIN
    },
    method="POST"
)
try:
    with urllib.request.urlopen(req) as resp:
        print(f"\n3. Bad Login status: {resp.status} (unexpected success)")
        bad_pass = False
except urllib.error.HTTPError as e:
    print(f"\n3. Bad Login correctly rejected: status={e.code}")
    print(f"   Access-Control-Allow-Origin: {e.headers.get('Access-Control-Allow-Origin')}")
    bad_pass = (e.code in [400, 401] and e.headers.get('Access-Control-Allow-Origin') == ORIGIN)
except Exception as e:
    print(f"3. Bad Login error: {e}")
    bad_pass = False

# 4. Test Authenticated /api/auth/me with Cloudflare Origin
if token:
    req = urllib.request.Request(
        f"{BASE_URL}/auth/me",
        headers={
            "Authorization": f"Bearer {token}",
            "Origin": ORIGIN
        },
        method="GET"
    )
    try:
        with urllib.request.urlopen(req) as resp:
            me_body = json.loads(resp.read().decode("utf-8"))
            print(f"\n4. Authenticated /api/auth/me: status={resp.status}")
            print(f"   Access-Control-Allow-Origin: {resp.headers.get('Access-Control-Allow-Origin')}")
            print(f"   Email: {me_body.get('email')}")
            me_pass = (resp.status == 200 and me_body.get('email') == "admin@learnai.local")
    except Exception as e:
        print(f"4. /api/auth/me failed: {e}")
        me_pass = False
else:
    me_pass = False

print("\n=================================================================")
print("SUMMARY:")
print(f"  Preflight OPTIONS: {'PASS' if opts_pass else 'FAIL'}")
print(f"  Cloudflare Login (Valid): {'PASS' if login_pass else 'FAIL'}")
print(f"  Cloudflare Login (Invalid rejected): {'PASS' if bad_pass else 'FAIL'}")
print(f"  Cloudflare /api/auth/me: {'PASS' if me_pass else 'FAIL'}")
print("=================================================================")
