plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.4"
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
    buildFeatures {
        viewBinding = true
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

    // Applandeo CalendarView
    implementation("com.applandeo:material-calendar-view:1.9.2")

    // 캘린더 커스텀
    implementation("com.jakewharton.threetenabp:threetenabp:1.1.1")
    implementation("com.kizitonwose.calendar:view:2.5.4")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth")


    //구글 맵
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:19.2.0")

    //gilde 디펜던시
    implementation ("com.github.bumptech.glide:glide:4.11.0")
    implementation(libs.legacy.support.v4)
    implementation(libs.recyclerview)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")

    //새로고침
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")

    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.activity:activity:1.9.3")
    implementation(libs.constraintlayout)

    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Material (중복이지만 문제는 없음 – 유지)
    implementation("com.google.android.material:material:1.12.0")


}