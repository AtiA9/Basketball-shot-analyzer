// קובץ הבנייה של מודול האפליקציה
plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.basketballshotanalyzer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.basketballshotanalyzer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // ML Kit - זיהוי תנוחת גוף (הטכנולוגיה החדשה בפרויקט)
    implementation("com.google.mlkit:pose-detection:18.0.0-beta5")
}
