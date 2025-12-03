plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mam"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mam"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "2.2"

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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.generativeai)
    implementation(libs.play.services.analytics.impl)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.material3)
    implementation(libs.accompanist.navigation.animation)
    implementation("com.mapbox.maps:android:11.11.0")
    implementation("com.mapbox.extension:maps-compose:11.11.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.accompanist:accompanist-permissions:0.37.3")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("co.yml:ycharts:2.1.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation("androidx.paging:paging-compose:1.0.0-alpha20")
    implementation ("androidx.paging:paging-runtime:3.3.6")
    implementation ("androidx.paging:paging-compose:3.3.6")

    implementation("com.google.zxing:core:3.5.2") // Thư viện tạo mã QR
}