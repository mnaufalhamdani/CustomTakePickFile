plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = Configs.moduleId
    compileSdk = Configs.compileSdk

    defaultConfig {
        minSdk = Configs.minSdk

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
        sourceCompatibility = Configs.javaVersion
        targetCompatibility = Configs.javaVersion
    }
    kotlinOptions {
        jvmTarget = Configs.javaVersion.toString()
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

afterEvaluate {
    publishing {
        publications {
            // Creates a Maven publication called "release".
            create<MavenPublication>("release") {
                // Applies the component for the release build variant.
                // NOTE : Delete this line code if you publish Native Java / Kotlin Library
                from(components["release"])
                groupId = PublishSetting.groupId
                artifactId = PublishSetting.artifactId
                version = PublishSetting.version
            }
        }
    }
}