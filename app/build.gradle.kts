
plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // 파이어베이스
}

android {
    namespace = "com.kotlinsun.mobileapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kotlinsun.mobileapp"
        minSdk = 27
        targetSdk = 33
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
}

dependencies {

    // 파이어베이 sdk 모듈
    implementation (platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation ("com.google.firebase:firebase-analytics-ktx")
    implementation ("com.google.firebase:firebase-auth:22.3.0") // 파이어베이스 인증
    implementation ("com.google.firebase:firebase-firestore:23.0.3") // 파이어베이스 Firestore
    //그래프를 위한 모듈 설치
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0") // 여기에 추가
    //로티 모듈
    implementation ("com.github.skydoves:balloon:1.6.3")
    implementation ("com.github.prolificinteractive:material-calendarview:2.0.1")
    implementation ("com.jakewharton.threetenabp:threetenabp:1.2.1")
    implementation ("com.airbnb.android:lottie:6.1.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}