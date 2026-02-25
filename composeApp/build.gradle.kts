import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    // alias(libs.plugins.composeHotReload) // Disabled due to compatibility issues with Kotlin 2.0.20
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.desktop.currentOs)
            // SQLite Driver
            implementation("org.xerial:sqlite-jdbc:3.45.1.0")
            // Coroutines for fast file scanning
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("io.github.vinceglb:filekit-compose:0.8.2")
        }
    }
}


compose.desktop {
    application {
        // Option A: If using the .set() syntax
        mainClass = "com.clusterview.demo.MainKt"

        // Option B: If using the = syntax (standard in many templates)
        // mainClass = "com.clusterview.demo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "demo"
            packageVersion = "1.0.0"
        }
    }
}

