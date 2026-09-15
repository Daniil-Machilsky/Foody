plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.softcat.database"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val s3AccessKey = property("s3AccessKey")?.toString() ?:
        error("No s3AccessKey api key defined in gradle.properties.")
        buildConfigField("String", "S3_ACCESS_KEY", "\"$s3AccessKey\"")

        val s3SecretKey = property("s3SecretKey")?.toString() ?:
        error("No s3SecretKey api key defined in gradle.properties.")
        buildConfigField("String", "S3_SECRET_KEY", "\"$s3SecretKey\"")

        val s3Region = property("s3Region")?.toString() ?:
        error("No s3Region api key defined in gradle.properties.")
        buildConfigField("String", "S3_REGION", "\"$s3Region\"")

        val s3Endpoint = property("s3Endpoint")?.toString() ?:
        error("No s3Endpoint api key defined in gradle.properties.")
        buildConfigField("String", "S3_ENDPOINT", "\"$s3Endpoint\"")

        val bucketName = property("bucketName")?.toString() ?:
        error("No bucketName api key defined in gradle.properties.")
        buildConfigField("String", "BUCKET_NAME", "\"$bucketName\"")
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

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.database)
    implementation(libs.dagger.core)
    ksp(libs.dagger.compiler)
    implementation(libs.timber)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    api(libs.firebase.auth)
    api(libs.aws.android.sdk.s3)
    api(libs.aws.android.sdk.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}