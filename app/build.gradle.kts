plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "2.1.0"
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.hackathon.dinemate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hackathon.dinemate"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}


dependencies {
    // for splash screen
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout.v214)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.credentials.v130)
    // Bridge that lets Credential Manager talk to Google Play Services’ auth
    implementation(libs.androidx.credentials.play.services.auth)

    // Google Identity Services for Android (the types you’re using)
    implementation(libs.googleid)

    // Firebase Auth (you already use it)
    implementation(libs.firebase.auth.ktx.v2300)

    // for subscription
    implementation(libs.billing)
    implementation(libs.billing.ktx.v711)

    // formatting agent message
    implementation(libs.compose.markdown)

    // for google signout
    implementation(libs.play.services.auth)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx.v262)

    // Retrofit for networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson) // Gson converter

    // OkHttp (Retrofit uses this underneath, good to include for logging interceptor etc.)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor) // Optional: For logging API requests/responses

    // Kotlin Coroutines for asynchronous operations
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)


    // ViewModel and Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose) // Use latest version
    implementation(libs.androidx.lifecycle.runtime.compose) // Use latest version

    // Icons (Extended)
    implementation(libs.androidx.material.icons.extended)

    //local storage
    implementation(libs.gson)
    implementation(libs.coil.compose)
    // Room Runtime
    implementation(libs.androidx.room.runtime)
    // Optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.volley)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)

    // Room Compiler (using KSP)
    ksp(libs.androidx.room.compiler)

    //firebase
    implementation(libs.firebase.analytics)
    implementation(platform(libs.firebase.bom))
    // icon pack
    implementation(libs.icons.lucide)
    implementation("io.coil-kt:coil-compose:2.4.0") // For AsyncImage

    // Location services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Permission handling
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.8.2")


    // smooth swipe
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.credentials)
    implementation(libs.googleid)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.room.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}