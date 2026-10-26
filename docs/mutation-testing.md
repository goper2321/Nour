# Mutation testing (NUR-026) — reasoned refusal

Status: **deferred**. No mutation run was performed; no config was wired into the
build (default builds pay nothing for this).

## Why pitest cannot run on `:app` today

`pitest-gradle-plugin` 1.15.0 registers its `pitest` extension only when the
Java plugin is applied. `:app` is an Android application module (AGP 9.1.1), so
the extension never registers and `./gradlew :app:pitest` fails at
configuration time with:

```
Extension with name 'pitest' does not exist.
```

The plugin's own `GradleVersionEnforcer` only rejects *too-old* Gradle, so
Gradle 9.3.1 is not the blocker — the missing Java-plugin application is.

## What would unblock it

Extract the pure calculation packages into a JVM library module (NUR-020's
`:core` split): `features.prayer.calculation` and `features.qibla` (the
`QiblaCalculator` math, not the Compose screen) have no Android dependencies
beyond `kotlinx.coroutines`/stdlib and are already unit-tested on the JVM.
Once they live in a JVM module, pitest applies cleanly and the item's scope
(`mutationScope = features.prayer.calculation + features.qibla`, UI/Compose
excluded) can be configured as specified.

## When to revisit

- After NUR-020 ships `:core`, OR
- If an Android-friendly mutator (e.g. a Stryker-style Kotlin runner) becomes
  available without a module split.

## Acceptance note

Item NUR-026's acceptance allows "≥3 new tests merge OR a reasoned refusal is
written." This document is the reasoned refusal; no tests were added and no
numbers were chased. The crown-gem coverage already in place:
`PrayerCalculationTest`, `QuranCorpusTest`, `QuranCorpusIntegrityTest`,
`LocationSourceResolverTest`, `QiblaCalibrationHintLogicTest`.