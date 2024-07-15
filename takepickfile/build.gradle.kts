plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mnaufalhamdani.takepickfile"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)

    // If you want to additionally use the CameraX Lifecycle library
    implementation(libs.camerax.lifecycle)

    // If you want to additionally use the CameraX View class
    implementation(libs.camerax.view)

    implementation(libs.dexter)

    implementation(libs.fragment.fragment)
    implementation(libs.fragment.ktx)

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(libs.sdp)

    //Google
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.face.detection)
}