import org.gradle.internal.classpath.Instrumented.systemProperty

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.recommender"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.all {
            // Применяем правильный синтаксис для Kotlin DSL
            systemProperty("org.bytedeco.javacpp.platform", "macosx-arm64")
            systemProperty("org.bytedeco.javacpp.platform.compiler", "macosx-arm64")

            // На всякий случай включаем логирование загрузчика
            systemProperty("org.bytedeco.javacpp.logger.debug", "true")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(libs.multik.core.v030)
    implementation(libs.multik.kotlin)

    implementation(libs.dagger.core)
    ksp(libs.dagger.compiler)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(project(":domain"))
    implementation(project(":database"))
}