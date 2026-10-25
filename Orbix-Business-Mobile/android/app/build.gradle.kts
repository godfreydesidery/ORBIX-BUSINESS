import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

// Release signing comes from android/key.properties, which points at a
// keystore kept OUTSIDE the repo. Both are git-ignored (android/.gitignore).
// The same key must sign every release: an APK signed with another key will
// not install over an existing one.
val keyProperties = Properties().apply {
    val f = rootProject.file("key.properties")
    if (f.isFile) f.inputStream().use { load(it) }
}
val hasReleaseKey = keyProperties.getProperty("storeFile")
    ?.let { file(it).isFile } == true

android {
    namespace = "net.otapp.orbix.manager"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    defaultConfig {
        // Orbix Manager application ID — owned by OTAPP (net.otapp.orbix.manager).
        applicationId = "net.otapp.orbix.manager"
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    // ONE FLAVOR PER BRAND, discovered from brands/<id>/brand.json — adding a
    // customer is `python tool/brand.py new ...`, never an edit here. `orbix`
    // is the generic app and keeps net.otapp.orbix.manager; a customer brand is
    // its own app beside it (net.otapp.orbix.manager.<id>), with its own name,
    // icon and splash colour from brands/<id>/generated/android/res. Build with
    // dist/build.ps1, which also passes the brand's dart-defines
    // (lib/app/brand.dart).
    flavorDimensions += "brand"
    val brandDirs = (file("../../brands").listFiles() ?: emptyArray())
        .filter { File(it, "brand.json").isFile }
        .sortedBy { it.name }
    productFlavors {
        brandDirs.forEach { dir ->
            create(dir.name) {
                dimension = "brand"
                if (dir.name != "orbix") applicationIdSuffix = "." + dir.name
            }
        }
    }
    sourceSets {
        brandDirs.forEach { dir ->
            val res = File(dir, "generated/android/res")
            if (res.isDirectory) getByName(dir.name).res.srcDir(res)
        }
    }

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                storeFile = file(keyProperties.getProperty("storeFile"))
                storePassword = keyProperties.getProperty("storePassword")
                keyAlias = keyProperties.getProperty("keyAlias")
                keyPassword = keyProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Never falls back to the debug key: a release without the real
            // key fails below instead of shipping something that cannot be
            // updated later.
            if (hasReleaseKey) signingConfig = signingConfigs.getByName("release")
        }
    }
}

gradle.taskGraph.whenReady {
    if (!hasReleaseKey && allTasks.any { it.name.contains("Release") }) {
        throw GradleException(
            "No release signing key. Create android/key.properties " +
                "(storeFile, storePassword, keyAlias, keyPassword) pointing at " +
                "the Orbix Manager keystore, or build a debug APK instead."
        )
    }
}

flutter {
    source = "../.."
}
