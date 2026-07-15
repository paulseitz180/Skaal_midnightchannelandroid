# SYSTEM ARCHITECTURE — Midnight Channel Android Application

**Source of truth:** Grande Document v1.0 — Documents 2–4, Section 08  
**Architecture style:** Stateless native WebView shell around a single remote HTTPS destination  
**Implementation status:** Matches shipped MVP (TASKS 01–38); documentation finalized TASK 40

---

## 1. Architectural Intent

The Android application is a **delivery shell**. All product logic (environmental DJ engine, playlist selection, tap-to-unmute, CRT visuals, geo/weather signals) lives in the existing web application at `midnightchannel.live`. The native layer owns only:

- Process / Activity lifecycle (pause/resume WebView; Compose-owned destroy)
- Immersive presentation (Compose host + edge-to-edge system bars)
- WebView configuration and navigation policy (HTTPS same-origin only)
- Transient UI state: splash transition, offline/error, retry recovery
- Exit confirmation and external-link Intent handoff
- Connectivity observation for cold-start Offline and Retry enablement

There is **no** native data model, **no** authentication, and **no** backend owned by this app.

---

## 2. Android Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Android OS                                             │
│  ┌───────────────────────────────────────────────────┐  │
│  │  MainActivity (single Activity)                   │  │
│  │  connectivity/ + recovery/ bound to ShellController│  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │  MidnightShell (Compose)                    │  │  │
│  │  │  ┌─────────┐ ┌──────────────┐ ┌──────────┐ │  │  │
│  │  │  │ Splash  │ │ Offline/Error│ │ Channel  │ │  │  │
│  │  │  │ Screen  │ │ Screen       │ │ Host     │ │  │  │
│  │  │  └─────────┘ └──────────────┘ │ (WebView)│ │  │  │
│  │  │                               └────┬─────┘ │  │  │
│  │  └────────────────────────────────────┼───────┘  │  │
│  └───────────────────────────────────────┼──────────┘  │
│                                          │ HTTPS       │
│                                          ▼             │
│                             midnightchannel.live       │
│                             (Netlify; YouTube IFrame   │
│                              + site JS in WebView)     │
└─────────────────────────────────────────────────────────┘
```

### Confirmed stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin |
| UI host | Jetpack Compose |
| Web engine | Platform Android `WebView` (`android.webkit`) |
| Min API | 26 |
| Compile / target SDK | 36 |
| Package | `com.skaalsolutions.midnightchannel` |
| Application class | None — `MainActivity` is the sole entry |

### Single-destination assumptions

- One Activity
- No tab bar, navigation drawer, bottom sheets, or native modal dialogs over the WebView during normal playback
- No Fragment-based multi-destination navigation graph for product features
- Keyboard never invoked (no text entry)

---

## 3. Compose Architecture

Compose is the **native host shell** only — not a reimplementation of the Midnight Channel UI.

| Responsibility | Compose |
|----------------|---------|
| Splash Screen | Native Compose UI (CRT field, wordmark, caption `tuning in…`, phosphor blink) |
| Offline / Error Screen | Native Compose UI (`SIGNAL LOST`, Retry) |
| Main Channel View | `ChannelWebViewHost` → `MidnightWebView` (`AndroidView`) |
| Shell orchestration | `MidnightShell` driven by `ShellController` |
| Exit confirmation | Toast + TalkBack announce (not a Compose dialog / not a screen) |
| Site content, CRT treatment, TAP TO UNMUTE | **Not** Compose — site inside WebView |

### Composition principles (from Interaction Philosophy / Production Bible)

- Full-bleed / edge-to-edge; status bar and navigation bar theme-matched to CRT background `#0A0A0A`
- Accent `#00FF41` (site theme-color / v1.2 green); phosphor `#00FF00` for glow marks
- Corner radius **0** — hard edges; no generic Material-looking chrome on native surfaces
- Primary CTA style on Offline/Error: plain monospace text link — not a filled Material button
- Motion: splash floor 400–600ms (default 500ms); phosphor blink 2000ms (site tap-prompt parity); cross-fades 200ms; Retry label swap 150ms; double-back window 2000ms
- Pull-to-refresh: **disabled** at the native layer
- Haptics: **none**

---

## 4. WebView Architecture

WebView settings are **architectural decisions**. Incorrect configuration changes product behavior, not merely implementation detail.

### 4.1 Architecture-critical settings (summary)

| Concern | Decision |
|---------|----------|
| JavaScript | Enabled — DJ engine + tap-to-unmute are JS |
| DOM storage | Enabled — site localStorage shuffle/state |
| Database / IndexedDB | Enabled — same site-side state layer |
| `mediaPlaybackRequiresUserGesture` | **false** — site’s tap-to-unmute remains single source of truth |
| Mixed content | Never allow — HTTPS-only site |
| User agent | Default WebView UA — **not overridden** |
| Multiple windows | Unsupported (`setSupportMultipleWindows = false`) |
| File access | Disallowed |

Full must-implement table: `ENGINEERING_SPECIFICATION.md` / Grande Document Section 08. Applied in `MidnightWebSettings`.

### 4.2 WebViewClient policy (`MidnightWebViewClient`)

| Callback / policy | Behavior |
|-------------------|----------|
| `shouldOverrideUrlLoading` | HTTPS same-origin (`midnightchannel.live` / subdomains) → stay in WebView. Main-frame non-origin + approved schemes → external Intent. Subframes never spawn Intents. |
| `onReceivedError` / `onReceivedHttpError` | **Main frame only** → Offline. Suppress default browser error page (CRT blank document). |
| `onReceivedSslError` | Always `cancel()`. Offline only when failing URL matches top-level WebView URL. |
| Subframe / resource errors | Do not force Offline (immersion preserved). |

Same-origin is **HTTPS-only** (cleartext to the product host is rejected by policy and network security config).

### 4.3 WebChromeClient policy

- Progress / title callbacks available; splash advance is driven by `MainFrameLoadFinished` (not chrome progress).
- Geolocation prompts and `PermissionRequest` are **explicitly denied** (Manifest declares no location/camera/microphone).
- Safe Browsing hits (API 27+): `backToSafety(true)` — never `proceed`.

### 4.4 What WebView must not do

- Must not reimplement tap-to-unmute natively
- Must not inject scripts that alter site behavior
- Must not allow outbound same-WebView navigation to third-party origins

### 4.5 External integrations (inside WebView context only)

| System | Native involvement |
|--------|--------------------|
| midnightchannel.live (Netlify) | App loads `ChannelDestination.HOME_URL` via HTTPS |
| YouTube IFrame API | Site loads it inside WebView JS context — native code does **not** call it |
| IP geo / weather signals | Server-side / site-side; native shell **not involved** |

**Location permission:** None.

---

## 5. Application Lifecycle Strategy

| Event | Expected behavior |
|-------|-------------------|
| Cold start | System splash → Compose Splash (≥ ~400–600ms floor **and** main-frame finished) → Ready / Main Channel |
| WebView OS init failure | Splash / load → Offline |
| Main-frame load / HTTP / SSL (main) error | Offline |
| Retry from Offline | Explicit user Retry → Retrying (RECONNECTING…) → Loading → Ready or Offline (30s hung-load timeout) |
| Connection drop mid-session | `NetworkLost` and/or next main-frame error → Offline |
| System back | Toast “Press back again to exit”; second press within ~2s exits (`WebViewHistoryBackHandler` disabled) |
| App backgrounded / screen locked | `WebView.onPause` — audio stops (MVP); no MediaSession |
| Configuration change | Manifest `configChanges` absorbs orientation / screenSize / uiMode etc. — WebView not recreated |
| External link | Validated Intent; Midnight Channel remains in background |
| Activity finish | Compose `DisposableEffect` owns `WebView.destroy` (idempotent); Activity coordinator clears refs only |

---

## 6. State Management Strategy

The native app owns **no persisted product data**. Shell state is transient and in-memory (`ShellStore` / `ShellController`).

### Shell presentation states (shipped)

```
Splash ──(floor ∧ MainFrameLoadFinished)──► Ready
   │                                           │
   │ NetworkLost / fail                        │ MainFrameLoadStarted
   ▼                                           ▼
Offline ◄── fail ────────────────────────── Loading ──finish──► Ready
   │                                           ▲
   │ RetryRequested                            │
   ▼                                           │
Retrying ──RetryLoadStarted────────────────────┘
```

| State | Surface |
|-------|---------|
| `Splash` | Splash overlay (WebView may load underneath) |
| `Loading` | Main Channel visible; reload / recovery in flight |
| `Ready` | Main Channel; site may show tap-to-unmute |
| `Offline` | `SIGNAL LOST` + Retry (when network Available) |
| `Retrying` | Offline surface with `RECONNECTING…` |

**Listening / unmute** is site-owned inside the WebView. Native code does **not** model a Listening state.

WebView process-death navigation state may be saved/restored via `WebViewLifecycleCoordinator` — that is browser state, not a native product store. There is **no** Room, DataStore, or SharedPreferences product layer.

Connectivity: `ConnectivityMonitor` + `ConnectivityShellBridge` emit `NetworkLost` on cold offline and Available→Unavailable. Probe requires `NET_CAPABILITY_VALIDATED`. Restoration does **not** auto-leave Offline — Retry enablement reads live connectivity.

---

## 7. Navigation Strategy

| Concept | Decision |
|---------|----------|
| Destinations | Single remote destination: `https://midnightchannel.live/` |
| Native screens | Splash, Main Channel, Offline/Error — switched by `ShellState`, not a feature nav graph |
| Deep links / App Links | Not in MVP |
| Back stack | Single Activity; double-back exit (no in-WebView history pop in MVP) |
| External navigation | Main-frame only; schemes in `MidnightOriginPolicy.EXTERNAL_HANDOFF_SCHEMES` via `ExternalLinkNavigator` |

---

## 8. Security Model

| Control | Policy |
|---------|--------|
| Authentication | None |
| Permissions | `INTERNET` + `ACCESS_NETWORK_STATE` |
| Secrets / API keys | None in the Android app |
| Transport | HTTPS; `usesCleartextTraffic=false` + `network_security_config` |
| File access from WebView | Disabled |
| Safe Browsing | Enabled (settings + application meta-data); hits never `proceed` |
| App backup | Disabled (`allowBackup=false` + data extraction rules) |
| Multiple WebView windows | Disabled |
| User agent spoofing | Forbidden |
| External opens | Main-frame / approved schemes; validated + resolved before launch |

### WebView security policy (normative)

1. In-WebView navigation: HTTPS `midnightchannel.live` (and subdomains) only, plus shell `about:` / `data:` documents.
2. All other origins / handoff schemes: leave via external Intent.
3. `allowFileAccess = false`.
4. `mixedContentMode = MIXED_CONTENT_NEVER_ALLOW`.
5. `safeBrowsingEnabled = true`.
6. No product `JavascriptInterface` in MVP.
7. No geo/camera/mic runtime permissions.

---

## 9. Manifest Requirements (Architecture Level)

| Requirement | Detail |
|-------------|--------|
| Package | `com.skaalsolutions.midnightchannel` |
| Permissions | `INTERNET`, `ACCESS_NETWORK_STATE` |
| Explicitly absent | Location, camera, microphone, storage, notifications, foreground service |
| Launcher Activity | `MainActivity` |
| Orientation | Open in Grande Document; **shipped:** absorbed via `android:configChanges` (no portrait lock yet) |
| Network security | Cleartext disabled; system trust anchors |

---

## 10. Hosting & Deployment Environment

| Concern | Owner |
|---------|-------|
| Web content hosting | Existing Netlify site (unchanged) |
| App binary | Codemagic debug APK (CI) + release APK/AAB (R8); signing when keystore env present |
| CI compile gate | Codemagic lint + JVM unit tests + debug/release artifacts on Linux |
| Local iteration | Android Studio (Orchestrator) |
| Source control | GitHub |

---

## 11. Implementation Decisions (Captured During Build)

| Decision | Rationale |
|----------|-----------|
| No native Listening state | Site owns unmute; avoids unused shell paths |
| Splash advances on `MainFrameLoadFinished` ∧ floor | Reliable gate without depending on chrome progress |
| HTTPS-only same-origin | Matches HTTPS-only product + cleartext policy |
| Main-frame Intent gating | Prevents iframe / subframe from launching external apps |
| Explicit Retry only | No auto-retry loops; Retry gated on validated connectivity |
| WebView history back disabled | Grande Document: double-back exit on Main Channel |
| `configChanges` instead of portrait lock | Preserves WebView across rotation until Orchestrator locks orientation |
| Compose owns `WebView.destroy` | Avoids double-destroy crash on Activity finish |
| Release R8 + optional signing | Codemagic validates minify even without keystore; Phase 1 prefers signed sideload |
| Radial CRT field + phosphor/accent split | Site visual fidelity (TASK 34); single theme-level field paint (TASK 38) |
| Security hardening beyond §08 baseline | Backup off, content/file URL flags, permission deny, Intent sanitization (TASK 37) |
| JVM regression suite on CI | 128 unit tests across reducer, WebView, recovery, tokens (TASKS 31–35) |

---

## 12. Known Architectural Risks (from Grande Document §11)

Carry forward — partially mitigated where noted:

1. CRT accent resolved at `#00FF41` / phosphor `#00FF00`; monospace is system Monospace until a licensed Courier New asset is bundled.
2. Screen orientation / rotation policy still open (recommend portrait-lock for MVP; currently `configChanges`).
3. Background audio deferred to Phase 2.
4. Play Store vs internal-only distribution undecided.
5. IP geo/weather under WebView UA must be Orchestrator QA-verified.
6. Screen keep-awake not overridden in MVP.
7. Phase 1 signed artifact depends on Codemagic/local keystore configuration.
