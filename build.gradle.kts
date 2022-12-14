import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "github.christechs"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://repository.aspose.com/repo")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {

            dependencies {

                implementation(compose.desktop.currentOs)

                implementation("com.aspose:aspose-pdf:22.9:jdk17")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

                implementation("com.guardsquare:proguard-gradle:7.3.0")

            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {

        buildTypes.release.proguard {
            obfuscate.set(false)
        }

        mainClass = "github.christechs.Main"
        nativeDistributions {
            targetFormats(TargetFormat.AppImage)
            packageName = "PDF Converter"
            packageVersion = "1.0.0"
            includeAllModules = true
            jvmArgs += listOf(
                "-XX:+UseSerialGC",
                "-Xms220M", "-Xmx220M"
            )
        }
    }
}