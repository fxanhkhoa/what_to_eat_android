plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.fxanhkhoa.what_to_eat_android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fxanhkhoa.what_to_eat_android"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Example keystore configuration
            // Replace these values with your actual keystore details
            storeFile = file("/Users/khoabui/keystore_fxanhkhoa_account/upload-keystore.jks")
            storePassword = "Buianhkhoa03021996!"
            keyAlias = "upload"
            keyPassword = "Buianhkhoa03021996!"

            // Uncomment if you want to use environment variables (recommended for CI/CD)
            // storeFile = file(System.getProperty("user.home") + "/.android/what_to_eat_release.jks")
            // storePassword = System.getenv("KEYSTORE_PASSWORD")
            // keyAlias = System.getenv("KEY_ALIAS")
            // keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_URL", "\"https://api.eatwhat.io.vn/\"")
            // Debug builds use debug keystore automatically
        }
        release {
            buildConfigField("String", "API_URL", "\"https://api.eatwhat.io.vn/\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use the release signing config
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.navigation:navigation-compose:2.7.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // DataStore for theme persistence
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // SwipeRefresh for pull-to-refresh functionality
    implementation("androidx.compose.material:material:1.5.4")

    // AppCompat for locale management
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // JWT parsing for token expiry detection
    implementation("com.auth0:java-jwt:4.4.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Socket.IO client
    implementation("io.socket:socket.io-client:2.1.0")

    // Image loading for user avatars
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}