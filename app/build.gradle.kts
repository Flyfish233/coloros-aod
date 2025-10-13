plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.flyfish233.aodwallpaper"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.flyfish233.aodwallpaper"
        minSdk = 35
        targetSdk = 36
        versionName = "5"
        versionCode = 5

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
    buildFeatures {
        buildConfig = true
    }
    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xno-param-assertions",
                "-Xno-call-assertions",
                "-Xno-receiver-assertions"
            )
        }
        jvmToolchain(21)
    }
}

dependencies {
    compileOnly(libs.xposed.api)
    implementation(libs.yuki.hookapi)
    ksp(libs.yuki.hookapi.ksp.xposed)
    implementation(libs.core.ktx)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)
}