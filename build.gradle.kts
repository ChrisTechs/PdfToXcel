import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("java")
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

            tasks.withType(Jar::class.java) {
                manifest {
                    attributes["Main-Class"] = "github.christechs.Main"
                }
            }

            tasks.withType(ShadowJar::class.java) {
                isZip64 = true
            }

            dependencies {

                implementation(compose.desktop.currentOs)

                implementation("com.aspose:aspose-pdf:22.9:jdk17")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {

        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard-rules.pro"))
        }

        mainClass = "github.christechs.Main"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.AppImage)
            packageName = "PDF Converter"
            packageVersion = "1.0.0"
            includeAllModules = true
            jvmArgs += listOf(
                "-XX:+UseSerialGC",
                "-Xms350M", "-Xmx350M"
            )
        }
    }
}