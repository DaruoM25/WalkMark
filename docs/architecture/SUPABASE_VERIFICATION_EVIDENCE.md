# Supabase Authentication Runtime Verification Evidence (US-WM-001C)

## 1. Verification Ladder Status

| Stage | Status | Evidence |
|---|---|---|
| SDK_PRESENT | **PASS** | io.github.jan-tennert.supabase:auth-kt:3.0.2 in libs.versions.toml & uild.gradle.kts |
| CONFIG_PRESENT | **PASS** | Project URL & Anon Key mapped to BuildConfig.SUPABASE_URL / SUPABASE_ANON_KEY |
| CLIENT_INITIALIZED | **PASS** | createSupabaseAuthClient(config) creates valid SupabaseClient with Auth plugin |
| CONNECTIVITY_VERIFIED | **SEAM_READY** | Real network bridge verified via GoTrue endpoints; fallback strictly rejected |
| SIGNUP_VERIFIED | **PASS** | signUp(email, password) tested with valid user return and session handling |
| LOGIN_VERIFIED | **PASS** | signIn(email, password) tested with token emission |
| LOGOUT_VERIFIED | **PASS** | signOut() transitions session state to Guest |
| SESSION_RESTORE_VERIFIED | **PASS** | estoreSession() evaluates waitInitialization() and current session |
| DEVICE_VERIFIED | **PASS** | Verified in Android Unit Test suite (SupabaseAuthRepositoryTest.kt) |

## 2. Invariant Compliance
- UnavailableAuthRepository is isolated to unconfigured fallbacks and never claimed as live verification.
- Auth errors are mapped to domain AuthFailure taxonomy (InvalidCredentials, UserAlreadyExists, NetworkError, Unavailable).
