plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "ru.tech.easysearch"
        minSdk = 21
        targetSdk = 31
        versionCode = 2
        versionName = "1.0.2"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.6.0-alpha01")
    implementation("androidx.constraintlayout:constraintlayout:2.1.2")

    implementation("androidx.navigation:navigation-fragment-ktx:2.4.0-rc01")
    implementation("androidx.navigation:navigation-ui-ktx:2.4.0-rc01")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.4.0-rc01")

    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")


    implementation("com.google.code.gson:gson:2.8.8")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.squareup.okhttp3:okhttp:4.9.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.2")

    implementation("com.github.beksomega:loopinglayout:0.4.1")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.webkit:webkit:1.4.0")
    kapt("androidx.lifecycle:lifecycle-common-java8:2.4.0")
    implementation("androidx.room:room-runtime:2.4.0")
    kapt("androidx.room:room-compiler:2.4.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    implementation("ru.tinkoff.scrollingpagerindicator:scrollingpagerindicator:1.0.6")
    implementation("androidx.palette:palette-ktx:1.0.0")
}