import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.kover)
  alias(libs.plugins.detekt)
}

// ---------------------------------------------------------------------------
// Host capability gate for Robolectric.
//
// Robolectric 4.15+ dropped the legacy interpreter; every Robolectric test now
// needs the `nativeruntime` shared library, which is published only for
// linux-x86_64 (plus macOS). On any other host - notably Linux aarch64, e.g.
// Termux on an ARM device - the runner aborts with
//   "The Robolectric native runtime is not supported on Linux (aarch64)"
// before a single assertion runs, which turns the whole build red for a
// platform reason rather than a code reason.
//
// So detect the host, and on unsupported hosts exclude exactly the classes that
// use RobolectricTestRunner (discovered from source, so new ones are picked up
// automatically) and say so loudly. The pure-JVM tests keep running everywhere.
// ---------------------------------------------------------------------------
val osName = System.getProperty("os.name").orEmpty()
val osArch = System.getProperty("os.arch").orEmpty()
val arm64 = osArch.equals("aarch64", true) || osArch.equals("arm64", true)
val robolectricSupported = !(osName.startsWith("Linux") && arm64)

val robolectricTestClasses: List<String> =
  file("src/test").walkTopDown()
    .filter { it.isFile && (it.name.endsWith(".kt") || it.name.endsWith(".java")) }
    .filter { it.readText().contains("RobolectricTestRunner") }
    .map { f ->
      val rel = f.relativeTo(file("src/test/java")).path
      rel.substringBeforeLast('.').replace(File.separatorChar, '.')
    }
    .sorted()
    .toList()

if (!robolectricSupported) {
  logger.lifecycle(
    "Nour: Robolectric is not supported on $osName/$osArch - skipping " +
      "${robolectricTestClasses.size} Robolectric test class(es): ${robolectricTestClasses.joinToString()}. " +
      "Run them on an x86_64 or macOS host."
  )
}

tasks.withType<Test>().configureEach {
  if (!robolectricSupported) {
    robolectricTestClasses.forEach { cls -> filter { excludeTestsMatching(cls) } }
  }
}

// ---------------------------------------------------------------------------
// NUR-041 version scheme — single source of truth is `nour.versionName` in
// gradle.properties (format X.Y.Z).
//
// versionCode = Major*10000 + Minor*100 + Patch, each component <= 99.
//
// Ceiling: versionCode is a signed 32-bit int and must stay under
// 2,147,483,647; with this formula that caps the MAJOR component at 214,748.
// Named scheme: release v1.0 = versionCode 10000.
//
// Sample bumps: patch 1.1.0 -> 1.1.1 changes 10100 -> 10101;
//               minor 1.1.0 -> 1.2.0 changes 10100 -> 10200;
//               major 1.1.0 -> 2.0.0 changes 10100 -> 20000.
// ---------------------------------------------------------------------------
val nourVersionName = providers.gradleProperty("nour.versionName").get()
val nourVersionComponents = nourVersionName.split(".").map { part ->
    part.toIntOrNull()
        ?: error("nour.versionName must be three integers X.Y.Z (got: \"$nourVersionName\")")
}
check(nourVersionComponents.size == 3) {
    "nour.versionName must be X.Y.Z with exactly three components (got: \"$nourVersionName\")"
}
check(nourVersionComponents.all { it in 0..99 }) {
    "each nour.versionName component must be <= 99 for the versionCode formula (got: \"$nourVersionName\")"
}
val nourVersionCode = nourVersionComponents[0] * 10000 +
    nourVersionComponents[1] * 100 +
    nourVersionComponents[2]

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.nour.islamic"
    minSdk = 24
    targetSdk = 36
    versionCode = nourVersionCode
    versionName = nourVersionName

    // NUR-033: the Settings diagnostics card shows when this APK was
    // assembled ("build date hash"). Stamped once at configuration time from
    // UTC so a clean rebuild of the same source can still be told apart.
    // (Script-local imports: inside build.gradle.kts the name `java`.
    //  resolves to the plugin extension accessor, not the package.)
    val nourBuildTime = LocalDateTime.now(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    buildConfigField("String", "NOUR_BUILD_TIME", "\"$nourBuildTime\"")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Release signing needs env-provided credentials; CI/production must supply
  // a real upload keystore. Without one, `assembleRelease` must FAIL — never
  // silently fall back to the debug keystore (a debug-signed release APK must
  // be impossible to produce by accident). The check runs in the task-graph
  // callback below, not as a bare throw: configuration is evaluated for EVERY
  // Gradle invocation, so an eager throw would break `assembleDebug` too. The
  // required variables (all documented in .env.example) are:
  //   KEYSTORE_PATH, STORE_PASSWORD, KEY_ALIAS (default "upload"), KEY_PASSWORD
  //
  // Both build types use AGP's built-in `debug` signing config, whose keystore
  // lives at the standard `~/.android/debug.keystore` and is created by AGP on
  // first use. Earlier revisions pointed at a repo-root `debug.keystore`, but
  // that path is gitignored, so a clean checkout could not build at all
  // (`validateSigningDebug` failed: "Keystore file '<rootDir>/debug.keystore'
  // not found"). The standard location removes that footgun without weakening
  // release signing: a real upload keystore is still required for production.
  val uploadKeystore = System.getenv("KEYSTORE_PATH")?.let { file(it) }?.takeIf { it.exists() }
  val hasReleaseCreds = uploadKeystore != null && !System.getenv("STORE_PASSWORD").isNullOrEmpty()
  if (!hasReleaseCreds) {
    // Fires only when a task graph containing a release task is about to run;
    // `assembleDebug` and every other task configure and run exactly as before.
    gradle.taskGraph.whenReady {
      if (allTasks.any { it.name.contains("Release") }) {
        throw GradleException(
          "Nour release cannot complete: release signing env vars are missing. " +
            "Set KEYSTORE_PATH, STORE_PASSWORD, KEY_ALIAS (defaults to \"upload\") and KEY_PASSWORD, " +
            "then retry. Hint: see .env.example for the documented values."
        )
      }
    }
    logger.warn("Nour: release signing env vars missing; assembleRelease will fail (see .env.example).")
  }

  signingConfigs {
    if (hasReleaseCreds) {
      create("release") {
        storeFile = uploadKeystore
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Real upload keystore when configured. The debug fallback below is a
      // stand-in only — the guard above fails the build before any release
      // signing could use it.
      signingConfig =
        if (hasReleaseCreds) signingConfigs.getByName("release")
        else signingConfigs.getByName("debug")
    }
    // Uses AGP's built-in debug signing config (~/.android/debug.keystore).
    debug { }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions {
    unitTests { isIncludeAndroidResources = true }
  }

  // The app ships all 14 locales in one APK and lets the user switch language in-app
  // (LocalizationManager -> LocaleManager on API 33+). If the Play App Bundle split
  // per language, a user whose device locale is not one of ours could get a split
  // without the strings the in-app switcher needs. Keep all languages in one artifact.
  bundle {
    language {
      enableSplit = false
    }
  }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Offline-first: no network client, JSON converter, logging interceptor, or
// Firebase backend is used (prayer times, Qibla, and Quran all run on-device),
// so those dependencies were removed. Re-add any of them only together with a
// concrete feature that needs them.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.play.services.location)
  implementation(libs.material.library)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}

// ---------------------------------------------------------------------------
// NUR-029 - offline-permission guard. Nour is offline-first with no INTERNET
// permission (AGENTS.md "Offline-first", checklist guard G5). This verification
// task dumps the permissions of the debug APK with aapt2 and fails the build if
// a forbidden permission ever sneaks into app/src/main/AndroidManifest.xml.
// The signed release APK is owner-verified with the same task (aapt2 path) and
// CI runs this check on the debug APK in the unit-tests job.
// ---------------------------------------------------------------------------
tasks.register("verifyOfflineManifest") {
  group = "verification"
  description =
    "Fails if app-debug.apk declares INTERNET / ACCESS_NETWORK_STATE / BILLING (offline-first guard)."
  // The APK this check reads is the one assembleDebug produces, so it must be built first.
  dependsOn("assembleDebug")

  // Resolved at configuration time and captured as plain values, so the doLast
  // block stays configuration-cache compatible (no Gradle script object refs).
  val appBuildDir = layout.buildDirectory.get().asFile
  val sdkHome = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
  val buildToolsRoot = if (sdkHome.isNullOrEmpty()) null else file("$sdkHome/build-tools").takeIf { it.isDirectory }

  val forbiddenPermissions = listOf(
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "com.android.vending.BILLING",
  )

  doLast {
    val toolsRoot = buildToolsRoot
      ?: throw GradleException(
        "Nour offline check: ANDROID_HOME or ANDROID_SDK_ROOT must point at an " +
          "SDK with a build-tools directory to locate aapt2."
      )
    // "36.0.0" -> 36_000_000; plain single-integer keys keep maxByOrNull's
    // Comparable inference simple (build-tools version numbers are small).
    fun versionKey(dirName: String): Long =
      dirName.split('.').fold(0L) { acc, part -> acc * 1_000L + (part.toIntOrNull() ?: 0) }
    val aapt2 = toolsRoot.listFiles()
      ?.filter { it.isDirectory && it.resolve("aapt2").isFile }
      ?.maxByOrNull { versionKey(it.name) }
      ?.resolve("aapt2")
      ?: throw GradleException("Nour offline check: no aapt2 found under $toolsRoot.")

    val apkFile = appBuildDir.resolve("outputs/apk/debug/app-debug.apk")
    if (!apkFile.exists()) {
      throw GradleException("Nour offline check: $apkFile not found (assembleDebug did not run?).")
    }

    val process = ProcessBuilder(listOf(aapt2.absolutePath, "dump", "badging", apkFile.absolutePath))
      .redirectErrorStream(true)
      .start()
    val dump = process.inputStream.bufferedReader().readText()
    if (process.waitFor() != 0) {
      throw GradleException("Nour offline check: aapt2 dump failed on $apkFile:\n$dump")
    }

    val usesPermissionLines = dump.lineSequence()
      .filter { it.startsWith("uses-permission") }
      .map { it.trim() }
      .toList()
    println("Nour offline check: uses-permission lines from aapt2 dump badging $apkFile:")
    usesPermissionLines.forEach { println("Nour offline check:   $it") }

    // Badging prints `uses-permission: name='...'`; other aapt forms print the
    // bare name. Strip the wrapper so the forbidden names compare correctly.
    val declaredNames = usesPermissionLines
      .map { it.substringAfter(':').trim() }
      .map {
        if (it.startsWith("name='")) {
          it.removePrefix("name='").substringBefore('\'')
        } else {
          it.substringBefore(' ')
        }
      }
      .toSet()
    val hits = forbiddenPermissions.filter { it in declaredNames }
    if (hits.isNotEmpty()) {
      throw GradleException(
        "Nour offline check: FAILED - forbidden permission(s) found in app-debug.apk: " +
          "${hits.joinToString()}. Nour is offline-first; remove them from " +
          "app/src/main/AndroidManifest.xml (checklist guard G5)."
      )
    }
    println("Nour offline check: OK")
  }
}

// ---------------------------------------------------------------------------
// NUR-021 - Room schema export. Schema stays at version 1 and the entities are
// untouched, so existing user data is safe (AGENTS.md §6). Room writes
// app/schemas/com.example.database.NourDatabase/1.json through KSP, which is
// what makes every future schema change diffable and the required migration
// reviewable. app/schemas is not gitignored: the JSON is meant to be tracked.
// ---------------------------------------------------------------------------
ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

// ---------------------------------------------------------------------------
// NUR-023 - coverage gate: >= 60% LINE coverage measured ONLY for the
// features/ package set (the item explicitly forbids a whole-project gate).
// Wildcards cross dots, so "com.example.features.*" covers every features
// subpackage. Robolectric test classes live outside features/ and are never
// measured, and no @Preview composables exist in features/ (checked by grep),
// so the item's exclusion requirements are satisfied by this single filter.
// Host note: on x86_64 (CI + the usual dev host) the suite runs in full and
// the gate is exact; on ARM hosts that exclude Robolectric the measured
// number would under-count UI paths, since those tests are not run there.
// koverVerify automatically runs testDebugUnitTest first when invoked alone.
// ---------------------------------------------------------------------------
kover {
  reports {
    filters {
      includes {
        classes("com.example.features.*")
      }
    }
    verify {
      rule {
        minBound(60)
      }
    }
  }
}

// ---------------------------------------------------------------------------
// NUR-027 - detekt static analysis, MINIMAL surface, local only.
// - Not wired into any lifecycle task here: `./gradlew :app:detekt` is run on
//   demand, so normal builds (assemble*/test*) pay nothing for it.
// - baseline records the violations of existing code; only NEW findings fail
//   future runs. Never auto-fix: the baseline is the record, not a patch.
// - ruleset choices live in config/detekt/detekt.yml (complexity + naming +
//   empty-blocks, magic numbers OFF for the calculation math).
// ---------------------------------------------------------------------------
detekt {
  config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
  buildUponDefaultConfig = true
  baseline = file("$rootDir/config/detekt/baseline.xml")
}

// NUR-026 - mutation testing: DEFERRED (reasoned refusal, 2026-10-25).
// pitest-gradle-plugin 1.15.0 registers its 'pitest' extension only when the
// Java plugin is applied; :app is an Android application module (AGP 9.1.1),
// so the extension never registers and the task cannot be configured. Running
// pitest on the prayer/Qibla math requires extracting those packages into a JVM
// library module first (NUR-020). See docs/mutation-testing.md.
