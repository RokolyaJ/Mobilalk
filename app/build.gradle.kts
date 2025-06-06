plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.goplaneticket"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.goplaneticket"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("com.google.android.material:material:1.11.0")



    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-firestore:24.9.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
