plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
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

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.nour.islamic"
    minSdk = 24
    targetSdk = 36
    versionCode = 2
    versionName = "1.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Release signing uses env-provided credentials when available. Without an
  // upload keystore (no KEYSTORE_PATH file and no STORE_PASSWORD), release
  // builds fall back to the built-in Android debug keystore so
  // `assembleRelease` still works for local verification. CI/production must
  // provide a real keystore:
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
    logger.warn("Nour: no upload keystore configured; release builds will use the debug keystore (not for production).")
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
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Real upload keystore when configured, otherwise the debug keystore.
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
