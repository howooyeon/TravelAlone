import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
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
        buildConfigField("String", "CHATGPT_MODEL", "\"${properties.getProperty("CHATGPT_MODEL")}\"")
        buildConfigField("String", "KAKAO_API_KEY", "\"${properties.getProperty("KAKAO_API_KEY")}\"")
        buildConfigField("String", "WEATHER_API_KEY", "\"${properties.getProperty("WEATHER_API_KEY")}\"")
        manifestPlaceholders["KAKAO_API_KEY"] = properties.getProperty("KAKAO_API_KEY") ?: "default_api_key"


    }

//    signingConfigs {
//        create("release") {
//            keyAlias = properties.getProperty("KEY_ALIAS")
//            keyPassword = properties.getProperty("KEY_PASSWORD")
//            storeFile = file(properties.getProperty("STORE_FILE"))
//            storePassword = properties.getProperty("STORE_PASSWORD")
//        }
//    }

    buildTypes {
        getByName("release") {
           // signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        viewBinding = true
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
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.kakao.sdk)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // TabLayout 사용을 위한 의존성
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.google.android.material:material:1.4.0")

    //원형 이미지 뷰 종속성
    implementation("de.hdodenhof:circleimageview:2.2.0")

    //비밀번호 기반
    implementation("com.google.firebase:firebase-auth-ktx")

    implementation(libs.okhttp.v4100)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // 날씨 api 의존성 추가
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation (libs.circleimageview) //Circle ImageView

    //카카오 api 맵
    implementation("com.kakao.maps.open:android:2.6.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.kakao.sdk:v2-common:2.7.0")
    implementation("com.kakao.sdk:v2-talk:2.7.0")
}


