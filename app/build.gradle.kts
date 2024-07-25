import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    // 구글 서비스 플러그인
    id("com.google.gms.google-services")
}

val properties = Properties().apply {
    load(project.rootProject.file("local.properties").inputStream())
}

android {
    namespace = "com.guru.travelalone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.guru.travelalone"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "OPENAI_API", "\"${properties.getProperty("OPENAI_KEY")}\"")
        buildConfigField(
            "String",
            "CHATGPT_MODEL",
            "\"${properties.getProperty("CHATGPT_MODEL")}\""
        )

        manifestPlaceholders["OPENAI_API"] = properties.getProperty("OPENAI_KEY") ?: "default_api_key"
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // 챗봇관련 의존성
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    // 파이어베이스 의존성
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
