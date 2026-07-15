# QA STRATEGY — Midnight Channel Android Application

**Source of truth:** Grande Document v1.0 — Sections 10–11, Success Metrics (Document 1), Cursor role boundary (Section 09)  
**Implementation status:** Shell code complete (TASKS 01–26); device QA and signed Phase 1 artifact remain Orchestrator-owned (TASK 27 docs sync)

---

## 1. Testing Strategy Overview

This app is a **WebView shell**. Most product behavior is owned by midnightchannel.live. QA therefore splits into:

| Layer | What is verified | Who runs it |
|-------|------------------|-------------|
| Native shell correctness | Splash, Offline/Error, WebSettings, back-press, external links, lifecycle, Retry | Orchestrator on device / emulator; Codemagic for compile |
| Browser-parity product behavior | Tap-to-unmute, CRT visuals, DJ/playlist selection, geo/weather mood | Orchestrator comparing app vs mobile Chrome |
| Site regressions | DJ engine / playlist / visual bugs | Midnight Channel **web** assignment — not fixed in this app |

**Cursor does not run the emulator or certify runtime QA.** Cursor confirms the TASK compiles. Orchestrator validates behavior. TASK 25 performed code-path compliance validation and compile gates — not device certification.

---

## 2. Stage Gates

### 2.1 Stage 1–3 (Architecture — recap)

- [x] Product Brief / MVP / out-of-scope boundaries documented  
- [x] Entities / roles / integrations named (stateless shell)  
- [x] Screens and error states mapped  
- [x] Interaction Philosophy / Screen Blueprints / Production Bible (partial-but-sufficient)  

### 2.2 Stage 5/6 — Android Shell-Specific QA (Orchestrator)

- [ ] WebView settings verified against Section 08 **line-by-line** on a device build  
- [ ] Airplane mode from cold start → Offline/Error screen appears, no crash  
- [ ] Rapid tap-to-unmute → matches mobile web experience exactly  
- [ ] Back-button double-press exit → toast on first press; exit on second within window  
- [ ] Device rotation tested under shipped `configChanges` (or after portrait lock decision)  
- [ ] Cold-start performance tested on a device at or near API 26 floor  
- [ ] **Signed** APK distributed via Codemagic for sideload QA  
- [ ] If Play Store proceeds: Data Safety form drafted and reviewed  

**Code-side (Cursor / CI) — done when green:**

- [x] Kotlin compile (debug + release R8)  
- [x] Lint as configured in Codemagic  
- [x] WebSettings table implemented in `MidnightWebSettings`  

---

## 3. Test Plan by Risk Area

### 3.1 Compile / Static (CI)

| Check | Where |
|-------|-------|
| Kotlin compile | Codemagic + local Android Studio |
| Lint | Codemagic `midnight-channel-ci` / `:app:lintDebug` |
| Release minify | Codemagic release workflow / `:app:assembleRelease` |
| WebSettings constant review | Human review vs `ENGINEERING_SPECIFICATION.md` |

### 3.2 Unit (JVM)

**Framework:** JUnit 4 + Truth + coroutines-test + Robolectric (for Android types such as `Uri`).  
**Location:** `app/src/test/java/com/skaalsolutions/midnightchannel/`  
**CI:** `:app:testDebugUnitTest` on Codemagic `midnight-channel-ci`.

Target pure logic without a live WebView:

- Same-origin vs external URL classification (`MidnightOriginPolicy`)
- Double-back press window (~2s) (`DoubleBackExitGate`)
- Shell state transitions (`ShellReducer`: Splash → Ready → Offline → Retrying)

Shared fixtures live under `…/testing/` (`ControllableClock`, `ShellReducerFixtures`, `OriginTestFixtures`, `TestConventions`).  
**TASK 31** shipped JVM unit suites for reducer/store/controller, origin policy, recovery, connectivity helpers, double-back / back navigator, external Intent validation, and WebView→shell routing. Run `:app:testDebugUnitTest`. Suites do **not** replace Orchestrator device QA.

### 3.2c WebView validation (TASK 33)

**Location:** `app/src/test/.../webview/` (+ `FakeWebResourceRequest` helper).  
**Suites:** `MidnightWebSettingsValidationTest`, `MidnightWebViewClientNavigationTest`, `MidnightWebViewClientErrorRoutingTest`, `WebViewLifecycleCoordinatorTest`, `WebViewOfflineAndIntentValidationTest` (plus existing origin/routing suites).  
**CI:** included in `:app:testDebugUnitTest`. Asserts Grande Document §08 WebSettings, main-frame-only error/Offline routing, external Intent gating, and lifecycle attach/pause/destroy.

### 3.2d Regression suite (TASK 35)

**Command:** `./gradlew :app:testDebugUnitTest` (CI) + optional `:app:connectedDebugAndroidTest` (local device).  

| Area | Automated coverage | Suite(s) |
|------|--------------------|----------|
| Splash timing / CRT tokens | Floor 400–500ms, phosphor blink, colour field | `CrtTokenRegressionTest`; Compose floor + coerce |
| Runtime / state transitions | Splash↔Ready↔Offline↔Retry↔Loading | `ShellRegressionFlowTest` + existing reducer/store/controller |
| WebView init / config | Settings apply, chrome progress/title, origin home | `NavigationAndWebViewRegressionTest`, `MidnightWebChromeClientTest`, §3.2c |
| Offline + Retry | Recovery success/fail, NetworkLost mid-session | `ShellRegressionFlowTest`, `ChannelRecoveryControllerTest` |
| External links | Intent builders + same-origin reject | `NavigationAndWebViewRegressionTest`, `ExternalLinkNavigatorTest` |
| Back navigation | Double-back MVP (history disabled) | `ShellRegressionFlowTest`, `ShellBackNavigatorTest` |
| Lifecycle | Coordinator attach/pause/destroy/restore | `WebViewLifecycleCoordinatorTest` |
| Connectivity | Bridge cold-start + Available→Unavailable | `ConnectivityShellBridgeTest`, `ConnectivityStatusTest` |
| Accessibility | High-contrast secondary/disabled colours; Compose semantics | `ShellAccessibilityColorTest`; Splash/Offline Compose |

**Not reasonably automated (manual / Orchestrator):** live Chromium loads; airplane-mode NetworkCallback on device; TalkBack announcements / reduce-motion system setting; Toast + Activity.finish; external app hand-off staying backgrounded; tap-to-unmute / geo / CRT inside the site; process death; signed sideload; orientation feel; system SplashScreen API plate.

### 3.3 Instrumented / Device (Orchestrator)

| Scenario | Expected |
|----------|----------|
| Cold start online | Splash (400–600ms floor ∧ main-frame finish) → Channel; no white flash |
| Airplane mode cold start | Offline `SIGNAL LOST`; Retry enabled when online; Reload → Ready or Offline |
| Mid-session connectivity loss | NetworkLost / main-frame error → Offline |
| Tap-to-unmute | Parity with mobile Chrome; shell does not steal touches |
| Back press | Toast then exit within ~2s; no modal; no WebView history pop |
| External credit/link | External browser / app Intent; Midnight Channel stays backgrounded |
| Same-origin navigation | Remains inside WebView (HTTPS only) |
| Subframe / resource error | Does **not** force Offline |
| API 26 floor device | Acceptable cold start; unmute parity |
| Background app / lock | Audio stops (MVP); no crash |
| Orientation | Per `configChanges` until portrait-lock decided |

### 3.4 Browser-parity product checks

| Check | Expected |
|-------|----------|
| CRT visuals | Match site; no native Material overlay competing |
| Time-block / weather playlist selection | Expected playlist under known conditions |
| Geo/weather under WebView UA | Explicit Stage 6 pass — UA not overridden; confirm identical selection vs Chrome |

---

## 4. Success Metrics (from Product Brief)

Track during internal QA window:

1. Cold start → audio-ready ≤ mobile Chrome cold load of same page  
2. Zero-crash sessions across a one-week internal QA window  
3. Tap-to-unmute completion rate at parity with mobile web  
4. If Play Store: ANR rate &lt; 0.5% (Play Console vitals)

---

## 5. CI/CD Strategy

```
Developer / Cursor TASK commit
        ↓
     GitHub
        ↓
Codemagic (Linux)
  ├─ midnight-channel-ci: lint + debug APK
  └─ release workflow: R8 APK + AAB
        (+ signing when CM_KEYSTORE_* configured)
        ↓
Orchestrator sideload QA (Paul, Marina)
        ↓
Phase 2 (optional): Play Console — NOT configured in codemagic.yaml
```

| Environment | Role |
|-------------|------|
| Local Android Studio | Orchestrator iteration, sideload APK, device debugging |
| Codemagic | Trusted clean compile; debug + release artifacts |
| Netlify (web) | Unchanged content host |

**Pipeline rules**

- Green Codemagic ≠ product-QA complete.
- Unsigned release builds may still validate R8 — Phase 1 sideload **requires** configuring signing secrets.
- Error triage (Phase 7): fix **one root error at a time**.

---

## 6. Release Strategy

| Phase | Distribution | QA bar |
|-------|--------------|--------|
| **Phase 1** | Signed APK via Codemagic → sideload (Paul, Marina) | Shell QA checklist + one-week crash bar |
| **Phase 2** | Open decision: public Play Store **or** internal-only | If Play Store: Data Safety, store graphics, ASO |

Release does **not** include:

- Background audio (unless separately approved and tasked)
- iOS
- Web DJ/playlist changes (web assignment)
- Google Play publishing from this repo’s Codemagic config

### Pre-release Orchestrator checklist

- [ ] Section 08 WebSettings verified on release candidate  
- [ ] Airplane-mode and Retry paths verified  
- [ ] Tap-to-unmute parity verified vs Chrome  
- [ ] Double-back exit verified  
- [ ] Orientation policy confirmed and tested  
- [ ] API 26-class device smoke passed  
- [ ] Codemagic-**signed** artifact used for final sideload QA  
- [ ] Open questions in §8 closed or explicitly deferred with owners  

---

## 7. Known Engineering Risks

| Risk | Why it matters | Status / next step |
|------|----------------|--------------------|
| Monospace font-family vs site Courier New | Splash/Offline may feel slightly off | System Monospace shipped; optional licensed font later |
| Orientation undecided | Landscape may stress phone-first site layout | Prefer portrait-lock for MVP; currently `configChanges` |
| Background audio deferred | Service + MediaSession | Confirm demand before any TASK |
| Play Store vs internal-only | Data Safety, store assets | Business decision |
| WebView UA vs Chrome geo/weather | Playlist selection could diverge | Explicit Stage 6 QA |
| Keep-awake not in MVP | Long sessions may dim screen | Phase 2-adjacent |
| Unsigned CI release without keystore | Blocks Phase 1 sideload bar | Configure Codemagic android_signing |
| Renderer process gone | Host returns true but does not remount WebView | Retry may hit a dead Chromium process — remount follow-up |

---

## 8. Open Engineering Questions

These remain open from the Grande Document and must not be silently answered in code:

1. ~~Exact terminal-green hex~~ — shipped `#00FF41`; optional Courier New asset still open.  
2. Final screen orientation / rotation policy (portrait-lock recommended).  
3. Whether Phase 2 background audio is justified by listener demand.  
4. Play Store public listing vs internal-only distribution.  
5. Verified behavioral parity of IP geo/weather under WebView UA.  
6. Whether future Phase 2 should override screen timeout (keep-awake).

---

## 9. Failure Class Handling

| Failure class | User-visible handling | Support escalation |
|---------------|----------------------|--------------------|
| Connectivity / main-frame load | Native `SIGNAL LOST` + Retry | None — self-service |
| WebView OS init failure | Fall through to Offline | Rare device-level; note in QA log |
| Site/JS/player defects | Inherited from website | Route to web assignment |
| Native crash / ANR | Phase 1 relies on manual QA window | Orchestrator triage |

There are no accounts to reset, no native settings to misconfigure, and no user data recovery paths in MVP.

---

## 10. Validation Already Performed (TASK 25)

Code-path / compile validation covered:

- Cold launch, splash floor ∧ main-frame finish, WebSettings table, Offline/Retry, connectivity bridge, external origin policy, double-back, lifecycle pause/resume, `configChanges`, bg/fg teardown safety  
- Compliance fixes: HTTPS-only origin, main-frame Intent/SSL gating, idempotent WebView destroy  

**Not certified by TASK 25:** device tap-to-unmute parity, geo/weather UA parity, signed sideload artifact, portrait-lock decision.
