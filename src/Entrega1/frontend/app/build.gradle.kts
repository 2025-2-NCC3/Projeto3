plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.serialization") version "1.8.20"

}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SUPABASE_URL", "\"https://vhmvsbhbjugfbhluhetx.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZobXZzYmhianVnZmJobHVoZXR4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTgyMzYxNDEsImV4cCI6MjA3MzgxMjE0MX0.VUj4g8YVv7cqzMDzXtk6jk8U9BKyR8nppNndLVOmtzQ\"")
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
    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
    ndkVersion = "12"
    buildToolsVersion = "36.0.0"
}

dependencies {
    implementation ("com.google.zxing:core:3.5.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("androidx.appcompat:appcompat:1.7.0")
    implementation ("com.google.android.material:material:1.12.0")
    implementation ("androidx.activity:activity:1.9.2")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.airbnb.android:lottie:6.4.0")
    implementation ("androidx.core:core-splashscreen:1.0.1")


}