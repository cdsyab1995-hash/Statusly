plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.yourcompany.statusvault.data"
    compileSdk = 36
    buildToolsVersion = "37.0.0"

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.documentfile)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.hilt.android)
    implementation(libs.javax.inject)
    kapt(libs.room.compiler)
    kapt(libs.hilt.compiler)
}
