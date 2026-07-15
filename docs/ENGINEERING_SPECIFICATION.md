# ENGINEERING SPECIFICATION — Midnight Channel Android Application

**Source of truth:** Grande Document v1.0 — Documents 5–6, Section 08, UX Flow Map  
**Normative note:** WebView configuration values below are must-implement, not suggestions.  
**Implementation status:** Matches shipped MVP (`MidnightWebSettings`, Manifest, shell); docs synced TASK 27

---

## 1. Identity & Platform

| Field | Value |
|-------|-------|
| Application name | Midnight Channel |
| Package / applicationId | `com.skaalsolutions.midnightchannel` |
| Debug applicationId | `com.skaalsolutions.midnightchannel.debug` |
| Primary URL | `https://midnightchannel.live/` (`ChannelDestination.HOME_URL`) |
| Min SDK | API 26 (Android 8.0) |
| Compile / target SDK | 36 |
| Version | `1.0.0` / versionCode `1` |
| Language | Kotlin |
| UI | Jetpack Compose host + WebView content surface |
| WebView library | Platform `android.webkit` (WebView / WebSettings / clients) |

---

## 2. AndroidManifest Requirements

### 2.1 Permissions

| Permission | Required? |
|------------|-----------|
| `android.permission.INTERNET` | **Yes** |
| `android.permission.ACCESS_NETWORK_STATE` | **Yes** — ConnectivityManager Retry / cold Offline |
| Location (fine or coarse) | **No** |
| Camera | **No** |
| Microphone | **No** |
| Storage / media | **No** |
| Notifications | **No** |
| Foreground service (media) | **No** (Phase 2 background audio only) |

### 2.2 Application / Activity

- Single launcher Activity (`MainActivity`) hosting Compose + WebView shell.
- Immersive presentation: status / navigation bars color-matched to CRT `#0A0A0A`; SplashScreen API theme avoids white flash.
- `android:configChanges` absorbs orientation, screenSize, screenLayout, smallestScreenSize, keyboard, keyboardHidden, uiMode — WebView is not recreated on those changes.
- Orientation **lock** remains an open Grande Document §11 decision; shipped behavior is rotation-tolerant via `configChanges`.
- `android:usesCleartextTraffic="false"` + `network_security_config`.
- `android:allowBackup="false"` + `data_extraction_rules` — WebView storage must not leave the device via backup.
- Package visibility `<queries>` for https/http/mailto/tel/sms/market/intent handoff.
- WebView Safe Browsing application meta-data enabled.

### 2.3 Network

- Primary content over HTTPS only.
- Cleartext to the product destination is not permitted.

---

## 3. WebSettings — Required Configuration

Implement exactly in `MidnightWebSettings.apply`:

| Setting | Value | Why |
|---------|-------|-----|
| `javaScriptEnabled` | `true` | DJ engine and tap-to-unmute are JavaScript |
| `domStorageEnabled` | `true` | Site localStorage shuffle/state (KAIZEN-13 history) |
| `databaseEnabled` | `true` | IndexedDB / web database for site-side state |
| `mediaPlaybackRequiresUserGesture` | `false` | Site tap-to-unmute remains single source of truth |
| `mixedContentMode` | `MIXED_CONTENT_NEVER_ALLOW` | HTTPS-only site |
| `cacheMode` | `LOAD_DEFAULT` | Browser-equivalent caching |
| `setSupportMultipleWindows` | `false` | Single-destination app |
| `allowFileAccess` | `false` | No local file access needed |
| `safeBrowsingEnabled` | `true` | Standard hygiene |
| User agent | Default WebView UA — **not overridden** | Site must treat app as standard mobile browser |

**Security hardening (compatible with the table; do not reverse):**

| Setting | Value | Why |
|---------|-------|-----|
| `allowContentAccess` | `false` | No `content://` bridge into WebView |
| `allowFileAccessFromFileURLs` | `false` | Block file-URL XSS patterns |
| `allowUniversalAccessFromFileURLs` | `false` | Block cross-origin file-URL access |

**Deviation from the normative table is a defect**, not an optimization.

---

## 4. WebViewClient Behaviour

### 4.1 Navigation — `shouldOverrideUrlLoading`

| URL class | Behavior |
|-----------|----------|
| HTTPS same origin: `midnightchannel.live` (+ subdomains) | Load inside WebView |
| Shell documents `about:blank` / `data:text/html` (or `text/plain`) | Load inside WebView (CRT blank / suppression) |
| Other `about:` / `data:` / `javascript:` | **Block** — not treated as shell documents |
| Main-frame other approved schemes | Block in-WebView load; `ExternalLinkNavigator` Intent |
| Subframe non-origin | Block in-WebView load; **do not** launch Intent |

HTTP to the product host is **not** treated as same-origin.

### 4.2 Errors — `onReceivedError` / `onReceivedHttpError` / SSL

- Apply Offline routing to **main frame** failures (HTTP ≥ 400; network errors; SSL when failing URL == top-level URL).
- Always cancel SSL errors (`handler.cancel()`).
- **Do not** allow WebView’s default browser error page to render (load CRT blank document).

### 4.3 WebChromeClient

- Title / progress callbacks available; splash paint gate uses `MainFrameLoadFinished`.
- Geolocation prompts and `PermissionRequest` are **denied** (no Manifest location/camera/mic).
- Safe Browsing hits (API 27+): `backToSafety(true)` — never `proceed`.

---

## 5. Screen Specifications (Native)

### 5.1 Splash Screen

| Aspect | Spec (shipped) |
|--------|----------------|
| Purpose | Bridge cold start → first WebView main-frame finish; establish CRT tone |
| Entry | App icon tap |
| Layout | Full-bleed `#0A0A0A`; centred monospace wordmark `MIDNIGHT CHANNEL`; optional CRT flicker |
| Hold time | Floor ~400–600ms (default 500ms via `CrtMotion.splashFlickerMs`) |
| Transition | Cross-fade (200ms) when **both** floor elapsed **and** `MainFrameLoadFinished` |
| Error | WebView init / network / main-frame fail → Offline (do not hang on Splash) |
| System bars | Color-matched; edge-to-edge; SplashScreen API + Compose CRT field |

### 5.2 Main Channel View

| Aspect | Spec (shipped) |
|--------|----------------|
| Purpose | Host live midnightchannel.live exactly as mobile browser |
| Entry | From Splash (normal) or Retry (from Offline) |
| Layout | Full-bleed WebView; **no** native address bar, toolbar, or bottom nav |
| Primary action | Tap anywhere — site JS tap-to-unmute; native must not intercept |
| Loading overlay | Not added natively — defer to site |
| Error | Main-frame failures → Offline |
| Exit | Double-press back confirmation; or standard backgrounding |
| Screen timeout | Not overridden in MVP |

### 5.3 Offline / Error Screen

| Aspect | Spec (shipped) |
|--------|----------------|
| Purpose | On-brand fallback when site unreachable |
| Entry | No connectivity at launch, or mid-session main-frame / NetworkLost |
| Background | Same CRT field as Splash |
| Heading | `SIGNAL LOST` |
| Helper | `Check your connection and try again.` |
| CTA | Plain monospace text link `RETRY` (48dp target, focus ring) |
| On Retry | Label → `RECONNECTING…` (150ms) then one reload via `ChannelRecoveryController` |
| Retry gate | Enabled only when connectivity is Available; no auto-retry loops |
| Success | Main Channel Loading → Ready |
| Failure | Remain on Offline |
| Accessibility | Content descriptions; font scaling; TalkBack announcements |

### 5.4 Exit Confirmation (not a screen)

| Aspect | Spec (shipped) |
|--------|----------------|
| Trigger | System back (history back disabled) |
| First press | Toast + TalkBack: `Press back again to exit` |
| Second press | Within ~2 seconds → `Activity.finish` |
| Pattern | Toast only — **no** modal dialog |

---

## 6. Visual / Production Bible Constraints (Binding)

| Element | Spec (shipped) |
|---------|----------------|
| Primary colour | `#0A0A0A` |
| Accent | `#00FF41` |
| Phosphor (marks / glow) | `#00FF00` |
| Error colour | None separate — stay green-on-black monospace |
| Typeface | `FontFamily.Monospace` (Compose); window TextAppearance monospace |
| Base type size | ≥ 16sp on native screens |
| Spacing | 8dp base unit (`CrtSpacing`) |
| Corner radius | 0 — hard edges |
| Launcher / splash mark | CRT dial / phosphor ring drawables |
| Pull to refresh | Disabled |
| Haptics | None |

Motion grammar (`CrtMotion`):

| Motion | Duration | When |
|--------|----------|------|
| Splash boot flicker / floor | 400–600ms (default 500) | Cold start |
| Screen cross-fade | 200ms | Splash↔Channel; Offline overlays |
| Retry label swap | 150ms | RETRY → RECONNECTING… |
| Exit confirmation window | 2000ms | Double-back |

---

## 7. Lifecycle & Playback Constraints

| Topic | MVP requirement (shipped) |
|-------|---------------------------|
| Background / lock-screen audio | **Out of scope** — `WebView.onPause` on Activity pause |
| Keep screen awake | **Not** overridden |
| Connectivity | Monitored while STARTED; Offline at cold unavailable; Retry gated on Available |
| Site state | Lives in WebView storage; native does not duplicate |
| WebView teardown | Compose dispose owns destroy (idempotent) |

---

## 8. Accessibility Baseline

- Launcher icon and app label descriptive for TalkBack: `Midnight Channel`.
- Splash and Offline/Error: content descriptions, 48dp Retry, font scaling, reduce-motion flicker off, high-contrast copy helpers.
- Main Channel View accessibility inherited from website — not modified by this assignment.
- Exit toast supplemented with `announceForAccessibility`.

---

## 9. Distribution & Signing

| Phase | Artifact | Channel |
|-------|----------|---------|
| Phase 1 | Signed APK when keystore configured | Codemagic → direct sideload QA |
| CI | Debug APK | Codemagic `midnight-channel-ci` |
| Release validation | R8 APK + AAB | Codemagic release workflow (unsigned if no keystore) |
| Phase 2 | Play Store **or** internal-only | Open decision — **no** Play publish in `codemagic.yaml` |

---

## 10. Non-Goals (Engineering)

- No JS injection to change site UX.
- No custom UA string.
- No location permission “just in case.”
- No native settings screen.
- No offline caching / downloading of audio.
- No Chromecast.
- No tablet-specific layout.
- No push notifications.
- No accounts / auth / session tokens.
- No native Listening / unmute bridge.
- No WebView history back in MVP (double-back exit only).
