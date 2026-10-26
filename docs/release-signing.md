# Release signing

Owner-facing guide. This file contains **placeholders only** (`<keystore-path>`,
`<keystore-password>`, …) — no real secret may ever be written into a repo file.
Run every command yourself in a safe terminal; sessions never commit or run
release builds.

## What the build expects

Since the release guard landed (`app/build.gradle.kts`, the "Release signing
needs env-provided credentials" block): without the environment variables,
`assembleRelease` **fails before any release task runs** — it can no longer
fall back to the debug keystore. When the env vars are absent the build throws:

```
Nour release cannot complete: release signing env vars are missing. Set
KEYSTORE_PATH, STORE_PASSWORD, KEY_ALIAS (defaults to "upload") and
KEY_PASSWORD, then retry. Hint: see .env.example for the documented values.
```

(One-line warning also prints at configure time; `assembleDebug` is unaffected.)

The four variables, exactly as in `.env.example` (nothing loads `.env` from the
repo automatically — read them from the shell):

| Variable | Meaning |
|----------|---------|
| `KEYSTORE_PATH` | path to the upload keystore (`.jks`) file |
| `STORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | key alias — default `upload` |
| `KEY_PASSWORD` | key password |

## One-time: create the upload key

Run in a safe terminal (prompts keep the passwords out of shell history). Use
your real name/organisation at the prompts; the distinguished name is
informational for a private upload key.

```bash
keytool -genkeypair \
  -alias <alias> \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -keystore <keystore-path>/nour-upload.jks
```

`-validity 10000` (≈27 years) matches the Play Console requirement for upload
keys. Verify it lands with:

```bash
keytool -list -v -keystore <keystore-path>/nour-upload.jks
```

Then secure the file: keep it on encrypted storage you control, back it up
offline, and **verify it is not inside the repository directory** (`.gitignore`
excludes `debug.keystore`, but never rely on ignore rules for the real keystore).
Do not put the keystore or any password in Downloads or shared device storage.

## Upload-signed release build

```bash
KEYSTORE_PATH=<keystore-path>/nour-upload.jks \
STORE_PASSWORD=<keystore-password> \
KEY_ALIAS=<alias> \
KEY_PASSWORD=<key-password> \
./gradlew :app:assembleRelease
```

Confirm the APK signature before any upload:

```bash
$ANDROID_HOME/build-tools/<version>/apksigner verify --print-certs \
  app/build/outputs/apk/release/app-release.apk
```

The signed artifact must show your upload certificate — a debug cert here means
the guard somehow never fired, which is a stop-ship problem.

## Version bump flow (NUR-041)

The app's version has a single source of truth: `nour.versionName` in
`gradle.properties` (format `X.Y.Z`, each component ≤ 99). `app/build.gradle.kts`
derives `versionCode` from it as `Major*10000 + Minor*100 + Patch` (release
v1.0 = 10000; versionCode must stay under 2,147,483,647, which caps major at
214,748).

When preparing a release, bump `nour.versionName` in `gradle.properties` as part
of your release checklist step, then run the signed release build above and
confirm the version entered in Play Console matches (e.g. `1.2.0` → versionCode
`10200`). There is no automated/CI bump — the bump is deliberately manual.

## Rotation (when the upload key must change)

Play App Signing keeps one stable **app-signing key**; your `upload` key only
proves provenance, so rotating it does not affect existing installs:

1. Generate a **new alias** in the same (or a fresh) keystore with the same
   `keytool` command above, e.g. `-alias <new-alias>`.
2. Build with `KEY_ALIAS=<new-alias>` and its own `KEY_PASSWORD`; keep
   `STORE_PASSWORD` tied to the keystore file. The old alias can stay in the
   keystore file, offline.
3. In Play Console → *App signing* (under Setup → App integrity), request an
   **upload-key reset**. Play asks to prove you own the account, then walks
   through registering the new upload key's public certificate. This is a
   guided console/support flow, not a field you edit — follow the on-screen
   steps and the console's stated identity checks.
4. After approval, releases signed with the new alias are accepted, and users
   see no difference (the app-signing key never changed).

Keep the old keystore (exported certificates too) until the new key is fully
proven in one shipped release.

## Never

- never commit the keystore or passwords to any repo (or paste them into
  docs/tickets/commit messages);
- never put the keystore file into Downloads / other shared device storage;
- never set `KEY_*` variables in a file that ships (`.env` is local-only);
- never ship a debug-signed release — since the guard, `assembleRelease`
  cannot produce one, so treat any signed-with-debug artifact as a build
  failure to investigate.
