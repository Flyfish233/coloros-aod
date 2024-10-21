plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.flyfish233.aodwallpaper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.flyfish233.aodwallpaper"
        minSdk = 35
        targetSdk = 35
        versionName = "2"
        versionCode = 2

    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/**"
            excludes += "/DebugProbesKt.bin"
        }
    }
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xno-param-assertions", "-Xno-call-assertions", "-Xno-receiver-assertions"
        )
    }
    buildFeatures {
        buildConfig = true
    }
    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
    compileOnly(libs.xposed.api)
    implementation(libs.yuki.hookapi)
    ksp(libs.yuki.hookapi.ksp.xposed)
    implementation(libs.core.ktx)
}