# BUILD SEQUENCE — Midnight Channel Android Application

**Source of truth:** Grande Document v1.0 — Section 09 (Stage 5 TASK Sequence) + Vibe Coding pipeline notes  
**Rule:** Cursor executes **one TASK at a time**. Orchestrator never feeds the full queue in a single prompt.  
**Status:** TASKS 00–26 implemented; TASK 27 refreshes `/docs` to match shipped code.

---

## 1. Purpose of This Sequence

This assignment is a thin delivery shell. The Stage 5 TASK list feeds — and does **not** replace — the standard Vibe Coding pipeline (Phases 0A–7).

TASK 00 produced `/docs` solution documentation (no production source). Subsequent TASKs built the Android shell. This document records the **completed** sequence plus remaining ops/QA ownership.

---

## 2. Stage 5+ TASK Sequence (Completed Through 26)

### Foundation (Grande Document §09 core)

| TASK | Scope | Outcome |
|------|-------|---------|
| **00** | Solution architecture docs | `/docs` pack created |
| **01** | Project scaffold — Kotlin, min API 26, package identity | Gradle app compiles |
| **02** | App icon + native splash (CRT) | CRT splash; no white flash; floor timing |
| **03** | WebView host + exact Section 08 WebSettings | Loads midnightchannel.live |
| **04** | Offline/Error + WebViewClient error routing | `SIGNAL LOST`; no default browser error page |
| **05** | Back-button double-press exit | Toast + exit within ~2s |
| **06** | External-link Intent handling | Same-origin in-WebView; outbound Intents |
| **07** | Codemagic pipeline | Lint + debug APK; later expanded for release |

### Shell completion (orchestrated follow-ons)

| TASK | Scope | Outcome |
|------|-------|---------|
| **08–20** | Theme, splash polish, WebView clients, origin policy, connectivity, recovery, shell state machine, a11y, performance | Production shell behaviours |
| **21–23** | Release hardening (R8, lifecycle, packaging) | Minified release + lifecycle stability |
| **24** | Codemagic / distribution readiness | CI + release APK/AAB workflows; signing when secrets present |
| **25** | End-to-end functional validation vs Grande Document | Compliance fixes (destroy, HTTPS origin, main-frame gates) |
| **26** | Codebase cleanup & refactoring | Dead paths/resources removed; no behaviour change |
| **27** | Production documentation update | This pack aligned to shipped implementation |

Exact intermediate TASK titles may vary in Orchestrator chat logs; the table above is the **as-built** engineering arc. For any conflict with an individual TASK brief, the brief owned that step — docs now describe the finished system.

---

## 3. TASK Dependency Order (Historical)

```
TASK 00 (docs)
   ↓
TASK 01 (scaffold)
   ↓
TASK 02 (icon + splash)
   ↓
TASK 03 (WebView host + settings)
   ↓
TASK 04 (offline/error) ← depends on WebView host
   ↓
TASK 05 (back-press exit)
   ↓
TASK 06 (external links) ← depends on WebViewClient
   ↓
TASK 07 (Codemagic)
   ↓
TASKS 08–24 (shell completion, recovery, CI release)
   ↓
TASK 25 (E2E validation / compliance fixes)
   ↓
TASK 26 (cleanup)
   ↓
TASK 27 (docs sync)  ← current
```

Do not reopen WebSettings deviations to “get something on screen.” Do not start speculative Phase 2 (background audio, keep-awake, Play publish) without an approved TASK.

---

## 4. Standard Pipeline After / Around TASKs

Unmodified Vibe Coding sequence:

1. **Phase 0A** — ChatGPT reviews and confirms the TASK sequence  
2. **Phase 0B** — Single build directive to Cursor  
3. **Phase 1** — Cursor builds one TASK at a time (Orchestrator feeds individually)  
4. **Phase 2** — Cursor self-review  
5. **Phase 3** — Claude Code senior review (primary safety gate)  
6. **Phase 4** — Shiplight automated QA  
7. **Phase 5** — Pre-compile checklist  
8. **Phase 6** — GitHub → Codemagic compile; Android Studio for local debug  
9. **Phase 7** — Error triage, **one root error at a time**

This assignment introduces **no exception** to that sequence.

---

## 5. Cursor Role Boundary (Mandatory)

| Actor | May do | Must not do |
|-------|--------|-------------|
| **Cursor** | Implement the current TASK; confirm code compiles cleanly | Run the emulator; verify runtime behavior on device/emulator; consume the full TASK queue at once |
| **Orchestrator** | Push via GitHub Desktop; build/test in Android Studio; run the app; advance TASKs | Treat Cursor “compiles” as “behaves correctly on device” |

---

## 6. Pre-Build / Pre-Sign-Off Gates

### Architecture (Stage 1–3) — Grande Document

- [x] Product Brief / MVP / out-of-scope boundaries documented  
- [x] Screens and error states mapped  
- [x] Interaction Philosophy / Screen Blueprints / Production Bible (partial-but-sufficient)  

### Implementation complete — remaining Orchestrator ownership

- [ ] Device/emulator Stage 5/6 shell QA checklist in `QA_STRATEGY.md`  
- [ ] Orientation policy confirmed (portrait lock **or** keep `configChanges`)  
- [ ] Codemagic **signed** Phase 1 APK for sideload (configure `CM_KEYSTORE_*`)  
- [ ] WebView UA geo/weather parity vs Chrome  
- [ ] Play Store vs internal-only distribution decision  

---

## 7. Definition of “Build Complete” for the Shell MVP

**Code complete:** TASKS 01–26 shipped; compile/lint/R8 gated on Codemagic.  

**Product complete:** additionally requires Orchestrator device QA in `QA_STRATEGY.md` **and** a signed Phase 1 artifact for sideload sign-off — Cursor compile success alone is not Stage 6 close.
