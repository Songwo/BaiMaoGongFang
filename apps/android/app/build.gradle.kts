plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.videoapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.videoapp"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.drawerlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation ("com.squareup.picasso:picasso:2.8")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.google.android.exoplayer:exoplayer:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.19.1")
    implementation ("com.google.android.exoplayer:extension-mediasession:2.19.1")
    implementation ("com.google.android.exoplayer:extension-okhttp:2.19.1")
    implementation ("androidx.drawerlayout:drawerlayout:1.2.0")

}
