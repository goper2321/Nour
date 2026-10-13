# Android 16 + Material 3 Expressive — Migration Plan

Ordered, independently verifiable tasks. Risk = chance of breaking working
behavior. Each task names its verification. Baseline is recorded in
`BASELINE.md`; final results in `TEST_REPORT.md`.

## Status (final)

- Stage 1 Baseline — **done**
- Stage 2 Plan — **done**
- Stage 3 Platform/Android 16 hardening — **done for 3.1, 3.2, 3.4; 3.3 mitigated** (device visual check still pending)
- Stage 4 Design foundation — **done** (4.1, 4.2, 4.3)
- Stage 5 Screen-by-screen — **scoped**: only confirmed defects touched (see `HANDOVER.md`); no broad reskin
- Stage 6 Integration & regression — **done** (debug build + 34/34 tests pass)
- Stage 7 Release readiness — **done** (`assembleRelease` + `lintVitalRelease` pass; device acceptance pending)
- Explicit non-goals — **not started** (by design)

## Guiding decisions

- The app is **already on API 36** (`compileSdk`/`targetSdk` 36, minSdk 24) and
  already Compose + Material 3 with a hand-built expressive token layer. This is
  therefore a *refinement and hardening* project, not a green-field migration.
- Do **not** upgrade the Compose BOM / `material3` in this pass purely to reach
  the newer M3E APIs. That is a high-blast-radius dependency change that cannot
  be visually validated here (no device). It is listed as an explicit follow-up
  with its own verification gate.
- Preserve the existing hand-built design system (it is coherent); extend it
  rather than replace it.

## Stage 1 — Baseline (done)

- Inspect structure, SDK/deps, manifest, screens, tokens, tests.
- Run `:app:assembleDebug` and `:app:testDebugUnitTest`.
- Record confirmed defects separately from suspicions. → `BASELINE.md`.

## Stage 2 — Plan (this document)

- Choose scope, order, risk, verification. → here.

## Stage 3 — Platform/Android 16 hardening (low risk)

| # | Task | Risk | Verification |
|---|---|---|---|
| 3.1 | Confirm/declare predictive-back compatibility and correct back behavior (targetSdk 36 enables predictive back by default) | Low | build + manual reasoning + BackHandler compile |
| 3.2 | Verify `PendingIntent` mutability flags (IMMUTABLE) in the adhan scheduler | Low | build; code review |
| 3.3 | Verify edge-to-edge insets on every screen (no content behind system bars, no double insets) | Med | build; document per-screen insets audit |
| 3.4 | Verify large-screen/resizability behavior (no fixed orientation; `BoxWithConstraints` nav already adapts) | Low | code review; document |

## Stage 4 — Design foundation (low risk)

| # | Task | Risk | Verification |
|---|---|---|---|
| 4.1 | Add a reduced-motion-aware spring helper to `NourMotion` | Low | compile; existing tests |
| 4.2 | Route remaining direct `spring(...)`/unguarded `tween(...)` through it (`SeamlessPrayerHeader`, `SetupWizard`) | Low | compile; reduced-motion reasoning |
| 4.3 | Keep the token/component inventory as the single source of truth | Low | docs |

## Stage 5 — Screen-by-screen refinement (med risk, scoped)

- Only touch screens with a **confirmed** inconsistency or defect; otherwise
  leave working UI alone. Candidate: consistency of motion + insets.
- Product-integrity surfaces (Quran reading, Qibla honesty) change only with a
  clear usability reason and never at the cost of textual accuracy.

## Stage 6 — Integration & regression

- Rebuild debug; rerun unit tests (JVM + Robolectric on this x86_64 host).
- Compare against baseline; fix regressions at the root cause.

## Stage 7 — Release readiness

- `assembleRelease` compiles (debug-signed fallback is expected without creds).
- Remove temporary workarounds; document remaining limitations.
- → `TEST_REPORT.md` and the final handover summary.

## Explicit non-goals this pass

- Compose BOM / material3 upgrade to native M3E components.
- Database schema changes or data migration.
- New features.
- Anything requiring a physical device to verify is documented as "not verified",
  not claimed as done.
