# CODING STANDARDS — Midnight Channel Android Application

**Source of truth:** Grande Document v1.0 — Scope Discipline, Interaction Philosophy, Sections 08–09  
**Scope of this file:** Rules for Kotlin / Compose / WebView implementation.  
**Implementation status:** Aligned to shipped MVP (TASKS 01–38); documentation finalized TASK 40

---

## 1. Guiding Principles

1. **Shell, not rebuild** — Do not reimplement Midnight Channel product logic in Kotlin.
2. **Inherit, don’t re-decide** — DJ engine, tap-to-unmute, CRT visuals, zero-controls philosophy remain site-owned.
3. **Exact WebView config** — Section 08 / `ENGINEERING_SPECIFICATION.md` values are normative (`MidnightWebSettings` only).
4. **Minimal surface area** — Prefer the fewest classes that fulfill Splash, Main Channel, Offline/Error.
5. **One TASK at a time** — No speculative Phase 2 work (background audio, keep-awake, Play Store publish) without an approved TASK.
6. **Unidirectional shell state** — All presentation changes go through `ShellController.dispatch` → `ShellReducer`.

---

## 2. Language & Project Conventions

| Topic | Standard |
|-------|----------|
| Language | Kotlin only for app code |
| UI | Jetpack Compose for native screens; `AndroidView` for WebView host |
| Package root | `com.skaalsolutions.midnightchannel` |
| Min API | 26 |
| Compile / target SDK | 36 |
| Gradle | Kotlin DSL + version catalog |
| Dependency restraint | Compose UI, platform `android.webkit`, lifecycle/activity — no unused SDK sprawl |

### Naming

- Prefer screen-aligned names (`SplashScreen`, `OfflineErrorScreen`, `ChannelWebViewHost`, `MidnightWebView`).
- Avoid inventing product feature modules (`playlist`, `mood`, `weather`) in the native app.
- Use `ShellState` / `ShellEvent` for native presentation — not site mood/DJ terminology.
- Do not add a native `Listening` state; unmute is site-owned.

---

## 3. Architecture Boundaries (Hard Rules)

| Allowed in native code | Forbidden in native code |
|------------------------|--------------------------|
| Splash / Offline UI | Playlist selection, DJ engine, mood matrix |
| WebView host + settings | Overriding or bypassing site tap-to-unmute |
| Same-origin / external-link policy | JavaScript injection that alters product UX (MVP) |
| In-memory `ShellState` + connectivity | Persisting product state that duplicates WebView storage |
| Toast exit confirmation | Modal dialogs over the WebView during playback |
| `INTERNET` + `ACCESS_NETWORK_STATE` | Location, mic, camera, storage, notifications (MVP) |
| Explicit Retry via `ChannelRecoveryController` | Auto-retry loops / silent restore to Ready |

Violating a “Forbidden” row is treated as an architecture defect in review.

---

## 4. Compose Standards

- Native screens follow CRT constraints: background `#0A0A0A`, accent `#00FF41`, monospace type, corner radius **0**, accent as text — not filled Material primary buttons.
- Retry control is a **plain monospace text link**, not a rounded filled button.
- Minimum touch target **48dp** on native interactive controls.
- Respect system font scaling and reduce-motion on Splash and Offline/Error.
- No bottom sheets, tab bars, swipe-to-refresh, or haptic feedback.
- Motion timings from `CrtMotion` (splash floor 400–600ms, phosphor blink 2000ms, cross-fade 200ms, Retry label 150ms, exit window 2000ms).
- CRT field is painted once at theme level (`CrtField.kt`); do not duplicate field layers on individual screens.
- Keep WebView mounted under Splash/Offline overlays (`View.INVISIBLE`); do not tear down the instance for overlay transitions.

---

## 5. WebView Standards

1. Apply WebSettings only in `MidnightWebSettings`.
2. Never set a custom user agent.
3. Never set `mediaPlaybackRequiresUserGesture = true`.
4. Never enable file access or mixed content.
5. Main-frame errors must route to native Offline — never leave the default browser error page visible.
6. Same-origin = HTTPS `midnightchannel.live` (+ subdomains); everything else via `ExternalLinkNavigator` (main-frame Intents only).
7. Do not add a `JavascriptInterface` for MVP.
8. Disable pull-to-refresh / accidental gesture reloads at the native layer.
9. Teardown: Compose `DisposableEffect` owns `disposeWebView` (idempotent). Activity coordinator must not double-`destroy`.

---

## 6. State & Concurrency

- Shell flags are transient / in-memory (`ShellStore`).
- Prefer unidirectional UI state for Splash ↔ Loading ↔ Ready ↔ Offline ↔ Retrying.
- Do not introduce Room, DataStore, or SharedPreferences for product features in MVP.
- Connectivity Flows: collect while Needed (e.g. Offline surface / STARTED lifecycle) — avoid churn while Ready.
- Destroy / detach WebView cleanly; use weak refs in `WebViewLifecycleCoordinator`.

---

## 7. Lifecycle & Media Rules

- Do **not** implement a foreground Service or MediaSession in MVP.
- Do **not** force keep-screen-on unless Phase 2 is approved and tasked.
- When the app backgrounds, pause the WebView (`onHostPause`) so audio stops with default Android rules.
- Absorb config changes via Manifest `configChanges` unless Orchestrator mandates portrait lock.

---

## 8. Error Handling & UX Copy

- Connectivity / main-frame failure → full-screen native `SIGNAL LOST` (not toast, not Snackbar, not WebView default page).
- Exit confirmation → toast + TalkBack announce only.
- Copy must match Screen Blueprints / `strings.xml`; do not invent alternate marketing messaging.

---

## 9. Testing Expectations for Implementers

Cursor confirms **compile cleanliness** for the TASK under work. Runtime device/emulator verification is Orchestrator responsibility.

Still write code that is *testable* where cheap:

- Pure helpers (`MidnightOriginPolicy`, `DoubleBackExitGate`, `ShellReducer`) should be unit-testable without WebView.
- Do not block TASK completion on full Emulator suites unless Orchestrator requests it.

See `QA_STRATEGY.md` for formal test and CI expectations. **128** JVM unit tests across 24 suites ship under `app/src/test` and run on Codemagic CI and release workflows (`:app:testDebugUnitTest`).

---

## 10. Review Checklist (Every TASK)

- [ ] No new product features beyond the current TASK scope  
- [ ] No extra permissions beyond INTERNET + NETWORK_STATE  
- [ ] WebSettings unchanged from normative table  
- [ ] No white flash / Material-chrome break of CRT immersion on native surfaces  
- [ ] No modal over WebView during playback  
- [ ] No background-audio scaffolding “for later”  
- [ ] Package remains under `com.skaalsolutions.midnightchannel`  
- [ ] Diff matches one TASK only  
- [ ] Docs updated only when TASK scope includes documentation (or Orchestrator directs)  

---

## 11. Documentation Sync

If implementation must diverge from `/docs` or the Grande Document, **stop and escalate to the Orchestrator**. Do not silently redesign. After approved changes, update `/docs` (as in TASK 40) before treating the divergence as permanent.
