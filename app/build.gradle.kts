plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}


configurations.all {
    exclude(group = "androidx.annotation", module = "annotation-jvm")
}

android {
    namespace = "com.example.habs_mainpage"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.habs_mainpage"
        minSdk = 24
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation(libs.firebase.database)
    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation ("com.google.firebase:firebase-analytics:21.5.0")
    implementation ("androidx.annotation:annotation:1.7.0")
    implementation(libs.annotation.jvm)
    implementation("org.jetbrains:annotations:24.0.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
