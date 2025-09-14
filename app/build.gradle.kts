plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pi"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
    buildToolsVersion = "35.0.0"
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Retrofit para chamadas de rede (Networking)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Conversor para JSON
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3") // Para ver logs das requisições

    // RxJava para programação assíncrona e reativa
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation("io.reactivex.rxjava3:rxjava:3.0.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0") // Adaptador do Retrofit para RxJava

    // Componentes de Arquitetura do Jetpack (ViewModel e LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.5.1")
// GSON
    implementation("com.google.code.gson:gson:2.8.9")
// RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.2.1")
// CardView
    implementation("androidx.cardview:cardview:1.0.0")
// RecyclerView para listas
    implementation("androidx.recyclerview:recyclerview:1.2.1")
// CardView para itens de lista
    implementation("androidx.cardview:cardview:1.0.0")
// GSON para manipulação de JSON
    implementation("com.google.code.gson:gson:2.8.8")
}