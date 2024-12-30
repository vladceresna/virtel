import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)

    id("io.gitlab.trixnity.uniffi.kotlin.multiplatform") version "0.1.0"
}


var version = "2.0.1"



kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")
    
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
    
    sourceSets {
        val desktopMain by getting

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
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



            /*val editorVersion = "0.23.2"
            implementation("io.github.Rosemoe.sora-editor:editor:$editorVersion")
            implementation("io.github.Rosemoe.sora-editor:language-textmate:$editorVersion")
            implementation("io.github.Rosemoe.sora-editor:language-treesitter:$editorVersion")
*/







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
            implementation("com.squareup.okio:okio:3.9.1")

            implementation(libs.ktor.client.okhttp)


            implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha06")
            implementation("io.coil-kt.coil3:coil-network-ktor:3.0.0-alpha06")



            // TODO: Other ui theme

        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            implementation("javazoom:jlayer:1.0.1")

            implementation("com.fifesoft:rsyntaxtextarea:3.1.3")

            implementation("ai.picovoice:picovoice-java:3.0.3")

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
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
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



cargo {
    packageDirectory = layout.projectDirectory.dir("math") // путь к Rust-проекту
}

uniffi {
    generateFromLibrary {
        namespace = "math" // пространство имен для генерируемых классов
        build = "release" // или другой профиль сборки Rust
    }
}