plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.jjb20"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.jjb20"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {

    // 레트로핏
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // 스칼라 변환기
    implementation("com.squareup.retrofit2:converter-scalars:2.6.4")
    // gson 변환기
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Material Calendar View 라이브러리
    implementation ("com.applandeo:material-calendar-view:1.9.2")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.activity:activity:1.9.3")
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}