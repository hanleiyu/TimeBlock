plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.shijiankuai.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shijiankuai.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "1.3.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            // Pinned cloud debug key (this VM, 2026-10-26). Android defaults:
            // alias androiddebugkey / password android. Do not regenerate.
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = false
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("org.json:json:20240303")
}

tasks.register<Copy>("exportDebugApk") {
    dependsOn("assembleDebug")
    from(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
    into("/opt/cursor/artifacts")
    rename { "shijiankuai-debug.apk" }
    doLast {
        val apk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
        copy {
            from(apk)
            into(rootProject.layout.projectDirectory.dir("artifacts"))
            rename { "shijiankuai-debug.apk" }
        }
        listOf(
            "shijiankuai-debug-v10.apk",
            "shijiankuai-debug-v10-pinned.apk",
            "时间块-v10-清单统计.apk",
        ).forEach { name ->
            copy {
                from(apk)
                into("/opt/cursor/artifacts")
                rename { name }
            }
        }
    }
}
