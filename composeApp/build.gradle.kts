
import io.gitlab.trixnity.gradle.CargoHost
import io.gitlab.trixnity.gradle.cargo.dsl.android
import io.gitlab.trixnity.gradle.cargo.dsl.jvm
import io.gitlab.trixnity.gradle.cargo.rust.targets.RustWindowsTarget
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.cargoKotlinMultiplatform)
    alias(libs.plugins.uniffiKotlinMultiplatform)
    alias(libs.plugins.kotlinAtomicFU)

    kotlin("plugin.serialization") version "2.1.10"
}

var version = "3.2.1"//beta



cargo {
    packageDirectory = project.layout.projectDirectory.dir("../vnative")
    builds.jvm {
        if (CargoHost.Platform.Windows.isCurrent) {
            jvm = rustTarget == RustWindowsTarget.X64
        } else {
            jvm = rustTarget == CargoHost.current.hostTarget
        }
    }
    builds.android {
        ndkLibraries.addAll("c++_shared")
    }
}

uniffi {

    /*bindgenFromGitRevision(
        repository = "https://gitlab.com/trixnity/uniffi-kotlin-multiplatform-bindings",
        revision = "a97a6a0f5cd243d4a41acedf8d57fe33cb3da5e8"
    )*/

    if (CargoHost.Platform.Windows.isCurrent) {
        generateFromUdl {
            namespace = "vnative"
            build = RustWindowsTarget.X64.friendlyName
            udlFile = project.layout.projectDirectory.file("../vnative/src/vnative.udl")
        }
    } else {
        generateFromUdl {
            namespace = "vnative"
            udlFile = project.layout.projectDirectory.file("../vnative/src/vnative.udl")
        }
    }
}





kotlin {

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class  )
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    jvm("desktop")


    if (CargoHost.Platform.MacOS.isCurrent) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }



    
    sourceSets {
        val desktopMain by getting

        if (CargoHost.Platform.MacOS.isCurrent) {
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation("javazoom:jlayer:1.0.1")
            implementation("ai.picovoice:picollm-android:1.1.0")



            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            api(libs.moko.permissions)
            api(libs.moko.permissions.compose)


        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation("io.ktor:ktor-server-cio:3.0.0-rc-1")
            implementation("io.ktor:ktor-server-websockets:3.0.0-rc-1")
            implementation(libs.ktor.client.okhttp)

            implementation("com.squareup.okio:okio:3.9.1")

            implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha06")
            implementation("io.coil-kt.coil3:coil-network-ktor:3.0.0-alpha06")


            implementation("com.github.terrakok:adaptivestack:1.0.0")

            // TODO: Other ui theme

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")



        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            implementation("javazoom:jlayer:1.0.1")

            implementation("ai.picovoice:picovoice-java:3.0.3")

            //implementation("ch.qos.logback:logback-classic:1.5.6")

        }
    }
}

android {
    namespace = "com.vladceresna.virtel"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.vladceresna.virtel"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = version
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        //isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
        implementation(libs.kotlinx.coroutines.android)

        //coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")
    }
}
dependencies {
    testImplementation(libs.junit.jupiter)
}

compose.desktop {

    application {
        mainClass = "com.vladceresna.virtel.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg,
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm,
                TargetFormat.AppImage)
            packageName = "com.vladceresna.virtel"
            packageVersion = version

            macOS {
                iconFile.set(project.file("res/logo.icns"))
            }
            windows {
                iconFile.set(project.file("res/logo.ico"))
            }
            linux {
                iconFile.set(project.file("res/logo.png"))
            }

        }
    }
}
