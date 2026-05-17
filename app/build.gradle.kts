plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.tananaev.logcat"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tananaev.logcat"
        minSdk = 23
        targetSdk = 36
        versionCode = 17
        versionName = "2.3"
    }

    flavorDimensions += "default"
    productFlavors {
        create("regular") {
            isDefault = true
            extra["enableCrashlytics"] = false
        }
        create("google")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.adblib)
    "googleImplementation"(platform(libs.firebase.bom))
    "googleImplementation"(libs.firebase.analytics.ktx)
    "googleImplementation"(libs.firebase.crashlytics)
    "googleImplementation"(libs.play.services.ads)
    "googleImplementation"(libs.play.review.ktx)
}
