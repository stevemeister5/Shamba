plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.shambasmart"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shambasmart"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Build config fields
        buildConfigField("String", "WEATHER_API_BASE_URL", "\"https://api.openweathermap.org/data/2.5/\"")
        buildConfigField(
            "String",
            "WEATHER_API_KEY",
            "\"${project.findProperty("WEATHER_API_KEY") ?: ""}\""
        )
        buildConfigField("Double", "FARM_LATITUDE", "-5.15")
        buildConfigField("Double", "FARM_LONGITUDE", "38.48")
    }

    signingConfigs {
        create("release") {
            // Configure release signing if needed
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            packaging {
                jniLibs {
                    useLegacyPackaging = true
                }
            }
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            packaging {
                jniLibs {
                    useLegacyPackaging = true
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }


    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // Enable legacy packaging to support 16KB page sizes for non-aligned native libs
            useLegacyPackaging = true
        }
    }
}

// Resolve duplicate classes between TFLite and LiteRT/Old TFLite versions
configurations.all {
    resolutionStrategy {
        force("org.tensorflow:tensorflow-lite:2.17.0")
        force("org.tensorflow:tensorflow-lite-api:2.17.0")
        force("org.tensorflow:tensorflow-lite-support:0.4.4")
    }
    // Exclude the new LiteRT API which conflicts with TFLite
    exclude(group = "com.google.ai.edge.litert", module = "litert-api")
}

dependencies {
        // Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    // Compose Adaptive Layouts
    implementation("androidx.compose.material3.adaptive:adaptive:1.0.0")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.0.0")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.0.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Room Database
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    // KSP needs these to resolve types in Converters during Room processing
    ksp("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    ksp("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // SQLCipher for encrypted database (Updated for 16KB support)
    implementation("net.zetetic:android-database-sqlcipher:4.6.1")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    
    // Security - Android Keystore
    implementation("androidx.security:security-crypto:1.1.0-beta01")

    // WorkManager for background sync
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.54")
    ksp("com.google.dagger:hilt-compiler:2.54")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Gson for JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    // kotlinx.serialization for JSON (required by Converters.kt)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Retrofit + OkHttp for network calls
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")

    // Coil for image loading
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")

    // Charts (for analytics)
    implementation("com.patrykandpatrick.vico:compose-m3:3.0.3")

    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // Core Library Desugaring (for java.time API on older Android)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // PDF Generation
    implementation("com.github.librepdf:openpdf:1.3.30")

    // CameraX for camera capture
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // OpenCV for computer vision (HSV analysis)
    implementation("org.opencv:opencv:4.13.0")

    // ONNX Runtime (Updated for 16KB support)
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.24.3")
    
    // TensorFlow Lite (Updated for 16KB support)
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // OSMDroid for interactive farm maps (no token required)
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation("org.osmdroid:osmdroid-wms:6.1.20")
    implementation("org.osmdroid:osmdroid-mapsforge:6.1.20")
    // OSMDroid heatmap overlay
    implementation("com.github.MKergall:osmbonuspack:6.9.0")
    // Google Location Services for GPS tracking
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
