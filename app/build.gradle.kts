plugins {
    alias(libs.plugins.android.application)
    ("com.android.application")
    ("com.google.gms.google-services") // Ensure this is added at the bottom

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.m_learning_onlinecources"
    compileSdk = 34  // Make sure this line is present and correctly declared

    defaultConfig {
        applicationId = "com.example.m_learning_onlinecources"
        minSdk = 25
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }


}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation("androidx.core:core:1.13.0")
    // Firebase dependencies using BOM
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage:20.0.0")
    implementation("com.google.firebase:firebase-firestore:24.5.0")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-auth:21.3.0")



    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("com.google.android.exoplayer:exoplayer:2.19.0")
    implementation ("androidx.gridlayout:gridlayout:1.0.0")


    // Other dependencies
    implementation("com.squareup.picasso:picasso:2.8")


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

