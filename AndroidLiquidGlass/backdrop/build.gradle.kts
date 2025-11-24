buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.34.0")
    }
}

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

android {
    namespace = "com.kyant.backdrop"
    compileSdk = 36
    buildToolsVersion = "36.1.0"

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}
kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xskip-metadata-version-check"
        )
    }
}

dependencies {
    implementation("androidx.compose.runtime:runtime:1.9.5")
    implementation("androidx.compose.foundation:foundation:1.9.5")
    implementation("androidx.compose.ui:ui:1.9.5")
    implementation("androidx.compose.ui:ui-graphics:1.9.5")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("io.github.kyant0", "backdrop", "1.0.0")

    pom {
        name.set("Backdrop")
        description.set("Jetpack Compose blur and Liquid Glass effects")
        inceptionYear.set("2025")
        url.set("https://github.com/Kyant0/AndroidLiquidGlass")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("Kyant0")
                name.set("Kyant")
                url.set("https://github.com/Kyant0")
            }
        }
        scm {
            url.set("https://github.com/Kyant0/AndroidLiquidGlass")
            connection.set("scm:git:git://github.com/Kyant0/AndroidLiquidGlass.git")
            developerConnection.set("scm:git:ssh://git@github.com/Kyant0/AndroidLiquidGlass.git")
        }
    }
}
