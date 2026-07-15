# SOLUTION OVERVIEW — Midnight Channel Android Application

**Source of truth:** `Midnight_Channel_Android_Grande_Document_v1.0.docx` (Grande Document v1.0)  
**Assignment type:** Mobile — Android (WebView Shell)  
**Parent product:** [midnightchannel.live](https://midnightchannel.live) (live, Netlify-hosted, vanilla HTML/CSS/JS)  
**Build system:** Skaal Vibe Coding Product Build System v7.3  
**Stakeholder / Orchestrator:** Paul Seitz (Skaal Solutions)  
**Implementation status:** MVP shell complete (TASKS 01–26); `/docs` refreshed in TASK 27

---

## 1. Product Purpose

Midnight Channel is an existing broadcast-style, browser-based ambient music experience. Today it is reachable only through a mobile browser — with bookmark friction, browser chrome, and repeated autoplay-policy clearance each session.

This assignment builds a **thin native Android shell** whose sole job is to deliver that **exact** web experience through a home-screen app icon instead of a browser tab. It does **not** rebuild the product, the DJ engine, playlist logic, tap-to-unmute gate, or visual system. Those remain owned by the Midnight Channel web assignment.

**Core Job To Be Done**

> When I want ambient, context-aware background sound without picking anything, let me tap one icon and be listening — instantly, and exactly the way it sounds on the website.

**User persona:** An existing or prospective Midnight Channel listener on an Android phone (home, office, or car; Wi-Fi or mobile data). They already know what the product does; they need a faster door back in — one icon, no browsing, no choosing.

---

## 2. Project Scope

| Attribute | Value |
|-----------|-------|
| Package name | `com.skaalsolutions.midnightchannel` |
| Tech stack | Kotlin + Jetpack Compose host shell + Android `WebView` (platform `android.webkit`) |
| Min API | 26 (Android 8.0) |
| Compile / target SDK | 36 |
| Version | `1.0.0` (versionCode `1`) |
| Backend | None — no server, database, or account system owned by the app |
| Content host | Unchanged Netlify deployment of midnightchannel.live |
| Pipeline | GitHub → Codemagic (Linux) → debug APK (CI) + release APK/AAB (R8); Phase 1 sideload |

**Scope discipline:** This is a shell, not a rebuild. Every product decision already made for the web experience is **inherited, not re-decided**.

---

## 3. In-Scope Functionality (MVP) — Implemented

1. **Native Android app shell** that loads `https://midnightchannel.live/` inside a full-screen embedded WebView and reproduces the browser experience — same environmental DJ engine, same tap-to-unmute gate, same CRT visual treatment. Nothing added, nothing removed.
2. **Home-screen launcher icon and app label** (`Midnight Channel`).
3. **Native splash screen** (system + Compose CRT) bridging cold start to first main-frame finish, floor ~400–600ms (default 500ms).
4. **Native offline / connection-error screen** (`SIGNAL LOST` + Retry / RECONNECTING…) replacing the WebView default browser error page.
5. **Double-press back exit** (~2s window) with toast + TalkBack announcement — no modal; WebView history back is **disabled** in MVP.
6. **External-link handling** — HTTPS same-origin stays in WebView; other approved schemes open via validated external Intents; app remains in background.

Supporting shell infrastructure shipped with MVP: connectivity monitoring (`ACCESS_NETWORK_STATE`), recovery controller (explicit Retry only — no auto-retry loops), Activity/`configChanges` lifecycle, and R8 release builds.

---

## 4. Explicit Out-of-Scope Functionality

| Item | Status |
|------|--------|
| Background audio playback (screen lock / app backgrounded) | Phase 2 — requires foreground Service + MediaSession; **not** MVP |
| Features not already on the website | Out — no accounts, settings, offline caching/download of audio, push notifications, Chromecast, tablet-specific layout |
| Changes to DJ engine, playlist logic, or visual design | Out — owned by the web assignment |
| iOS | Separate assignment if pursued |
| Location permissions | Not needed — site uses server-side IP geo, not device GPS |
| Native duplication of tap-to-unmute | Out — site JS is the single source of truth |
| Native overlays/modals during playback | Out — breaks ambient philosophy |
| Pull-to-refresh | Explicitly disabled |
| Haptic feedback | Explicitly not used |
| Push notifications | None in MVP |
| Native `Listening` / unmute bridge | Out — site owns listening; shell stays at Ready after main-frame load |

---

## 5. Success Metrics

- Cold start to first audio-ready state at or faster than a mobile Chrome cold load of the same page.
- Zero-crash session rate across a one-week internal QA window.
- Tap-to-unmute completion rate at parity with the mobile web experience (no shell-introduced regression).
- If Play Store distribution proceeds: ANR rate below 0.5% in Play Console vitals.

---

## 6. Distribution Strategy

| Phase | Approach |
|-------|----------|
| **Phase 1** | Signed APK via Codemagic (when `CM_KEYSTORE_*` / local `keystore.properties` configured), shared for sideload QA (Paul, Marina). Unsigned release builds still validate R8 when signing secrets are absent. |
| **Phase 2** | Open business decision: public Play Store listing vs. internal-only utility |

Codemagic **does not** publish to Google Play in this assignment.

---

## 7. Critical Path (UX)

```
Icon tap → Splash → Main Channel View loads → User taps to unmute (site JS) → Continuous ambient playback
```

Four steps; one decision (the unmute tap); by design.

**Native screens only:** Splash, Main Channel View (WebView host), Offline/Error.  
Exit Confirmation is a toast (+ accessibility announcement), not a screen.

---

## 8. Documents in This Pack

| Document | Role |
|----------|------|
| `SOLUTION_OVERVIEW.md` | Product purpose, scope, success criteria (this file) |
| `SYSTEM_ARCHITECTURE.md` | Android / Compose / WebView architecture, lifecycle, state, navigation, security |
| `PROJECT_STRUCTURE.md` | Repository and package layout (as shipped) |
| `ENGINEERING_SPECIFICATION.md` | Manifest, WebSettings, screens, config must-haves |
| `BUILD_SEQUENCE.md` | Stage 5+ TASK sequence (00–27) and pipeline gates |
| `CODING_STANDARDS.md` | Implementation constraints |
| `QA_STRATEGY.md` | Testing, CI/CD, release, and stage gates |

---

## 9. Authoritative Boundaries

1. Do **not** redesign the application or invent product features.
2. Do **not** simplify architectural decisions documented in the Grande Document.
3. Do **not** change WebSettings from Section 08 of the Grande Document / `ENGINEERING_SPECIFICATION.md`.
4. Do **not** request permissions beyond `INTERNET` and `ACCESS_NETWORK_STATE` (connectivity Retry UX).
5. Do **not** override the WebView user agent.
6. Do **not** intercept or reimplement the site’s tap-to-unmute logic.
7. Cursor executes **one TASK at a time**; Cursor does **not** run the emulator or verify runtime behavior — the Orchestrator does that in Android Studio.
