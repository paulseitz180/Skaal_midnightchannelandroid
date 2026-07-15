# PROJECT STRUCTURE — Midnight Channel Android Application

**Source of truth:** Grande Document v1.0 — Assignment Identity, Documents 5–6, Sections 08–09  
**Implementation status:** Reflects the shipped repository after TASKS 01–41 (repository cleanup)

---

## 1. Repository Structure (As Shipped)

```
Skaal_midnightchannelandroid/
├── Midnight_Channel_Android_Grande_Document_v1.0.docx   # Authoritative assignment foundation
├── docs/                                                # Solution documentation (this pack)
│   ├── SOLUTION_OVERVIEW.md
│   ├── SYSTEM_ARCHITECTURE.md
│   ├── PROJECT_STRUCTURE.md
│   ├── ENGINEERING_SPECIFICATION.md
│   ├── BUILD_SEQUENCE.md
│   ├── CODING_STANDARDS.md
│   └── QA_STRATEGY.md
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/skaalsolutions/midnightchannel/
│       │   └── res/
│       ├── test/                      # JVM unit tests (JUnit / Robolectric)
│       │   └── java/com/skaalsolutions/midnightchannel/
│       │       ├── testing/           # Shared fixtures + conventions
│       │       ├── navigation/
│       │       ├── util/
│       │       ├── webview/
│       │       ├── connectivity/
│       │       ├── recovery/
│       │       ├── a11y/
│       │       ├── theme/
│       │       └── regression/        # Cross-TASK scenarios (TASK 35)
│       └── androidTest/               # Instrumentation + Compose UI tests
│           └── java/com/skaalsolutions/midnightchannel/
│               ├── testing/           # Compose rules + conventions
│               └── ui/
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── codemagic.yaml
├── keystore.properties.example
└── .gitignore
```

### Ignored / local-only (must not be committed)

- `local.properties`, signing keystores, `keystore.properties`
- Build outputs (`app/build/`, `.gradle/`)
- Emulator / IDE caches

Optional later: root `README.md` onboarding pointer to `/docs` (not required for MVP).

---

## 2. Package Structure (As Shipped)

**Root package:** `com.skaalsolutions.midnightchannel`

```
com.skaalsolutions.midnightchannel/
├── MainActivity.kt                 # Single Activity; Compose setContent; lifecycle glue
├── ui/
│   ├── theme/                      # CRT colors, typography, spacing, shapes, motion, CrtField.kt
│   ├── shell/                      # MidnightShell + ShellBackHandler
│   ├── splash/                     # SplashScreen (Logo Expand)
│   ├── title/                      # TitlePageScreen (1500ms floor ∧ WebView ready)
│   ├── channel/                    # ChannelWebViewHost
│   ├── offline/                    # OfflineErrorScreen
│   ├── a11y/                       # TalkBack / contrast / reduce-motion helpers
│   └── testing/                    # Shared Compose test tags (production)
├── webview/
│   ├── ChannelDestination.kt       # HOST + HOME_URL
│   ├── CrtShellChrome.kt           # Shared CRT hex for WebView surfaces
│   ├── MidnightWebView.kt          # AndroidView host + dispose
│   ├── MidnightWebSettings.kt      # Exact Section 08 WebSettings
│   ├── MidnightWebViewClient.kt    # Origin policy + main-frame errors
│   ├── MidnightWebChromeClient.kt  # Progress / title (optional)
│   ├── MidnightOriginPolicy.kt     # Same-origin + external handoff schemes
│   ├── MainFrameFailure.kt
│   ├── ShellWebViewRouting.kt      # WebView callbacks → ShellEvent
│   └── WebViewLifecycleCoordinator.kt
├── navigation/
│   ├── ShellState.kt / ShellEvent.kt
│   ├── ShellReducer.kt / ShellStore.kt
│   └── ShellController.kt          # Single dispatch entry; recovery hooks
├── connectivity/
│   ├── ConnectivityStatus.kt
│   ├── ConnectivityMonitor.kt
│   └── ConnectivityShellBridge.kt  # NetworkLost → shell
├── recovery/
│   └── ChannelRecoveryController.kt # Explicit Retry → reload
└── util/
    ├── ExternalLinkNavigator.kt
    ├── DoubleBackExitGate.kt
    ├── ShellBackNavigator.kt
    ├── WebViewHistoryBackHandler.kt # MVP: Disabled
    └── BackNavigationResult.kt
```

### Package rules

| Rule | Rationale |
|------|-----------|
| No `data/` / `domain/` / `repository/` for product entities | App owns no data entities |
| No `billing/`, `auth/`, `notifications/` in MVP | Explicitly out of scope |
| WebView configuration isolated under `webview/` | Section 08 settings are architecture-critical |
| Shell transitions isolated under `navigation/` | Unidirectional `ShellEvent` → `ShellReducer` |
| Site logic never duplicated in Kotlin | Shell only |

**Not present (by design):** `MidnightChannelApp.kt`, `ui/components/` — Retry lives in `ui/offline`; theme tokens cover shared style.

---

## 3. Resource Structure (As Shipped)

```
app/src/main/res/
├── mipmap-anydpi/                 # Adaptive launcher icons
├── drawable/                      # splash_mark, launcher art
├── values/
│   ├── colors.xml                 # CRT tokens (#0A0A0A, #00FF41, phosphor, …)
│   ├── strings.xml                # App label, splash, SIGNAL LOST, Retry, exit toast
│   ├── themes.xml (+ values-v28)  # Immersive + SplashScreen API theme
│   ├── dimens.xml                 # Window TextAppearance body size
│   └── typography.xml             # Window TextAppearance.MidnightChannel
└── xml/
    ├── network_security_config.xml  # Cleartext disabled
    └── data_extraction_rules.xml    # No cloud / device-transfer backup of WebView data
```

Compose owns the live spacing / motion / type scale (`ui/theme`). XML retains window/splash/launcher needs only.

Native copy (Offline/Error), per Screen Blueprints:

| Key | Copy |
|-----|------|
| Heading | `SIGNAL LOST` |
| Helper | `Check your connection and try again.` |
| CTA | `RETRY` / transient `RECONNECTING…` |
| Exit toast | `Press back again to exit` |
| App label | `Midnight Channel` |
| Splash wordmark | `MIDNIGHT CHANNEL` |
| Splash caption | `tuning in…` |

---

## 4. Mapping Screens → Packages

| Grande Document screen | Package / surface |
|------------------------|-------------------|
| Splash Screen | `ui.splash` (+ system SplashScreen theme) |
| Main Channel View | `ui.channel` + `webview.*` |
| Offline / Error Screen | `ui.offline` |
| Shell overlays / orchestration | `ui.shell` + `navigation.*` |
| Exit Confirmation (toast) | `ui.shell.ShellBackHandler` + `util.*` |
| External Link | `webview` client + `util.ExternalLinkNavigator` |
| Connectivity / Retry | `connectivity.*` + `recovery.*` |

---

## 5. Build & Tooling Layout

| Path / artifact | Purpose |
|-----------------|---------|
| Gradle Kotlin DSL + version catalog | Android Application, Compose compiler, minSdk 26, compile/target 36 |
| `codemagic.yaml` | `midnight-channel-ci` (lint + unit tests + debug APK); release workflow (lint + unit tests + R8 APK/AAB) |
| `keystore.properties.example` | Local signing template (secrets not committed) |
| `proguard-rules.pro` | Release R8 keep rules for WebView / shell |

Confirmed tool stack:

- Android Studio
- Kotlin
- Cursor (TASK execution)
- GitHub
- Codemagic (Linux clean-environment compile gate)

---

## 6. Documentation Ownership

| Artifact | Role |
|----------|------|
| Grande Document (`.docx` at repo root) | Authoritative product + architecture foundation |
| `/docs/*.md` | Engineering working set — must not contradict the Grande Document; TASK 40–41 final sync |
| Web product repo (`Skaal_midnightchannel`) | Live behavior parity reference |

If conflict arises: **Grande Document wins** until explicitly revised by the Orchestrator.
