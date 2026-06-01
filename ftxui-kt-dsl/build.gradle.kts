plugins {
    kotlin("multiplatform")
    id("publishing-conventions")
}

publishing {
    publications.withType<MavenPublication>().all {
        pom {
            description.set("FTXUI Kotlin/Native bindings with custom DSL")
        }
    }
}

val hostOs = System.getProperty("os.name")
val hostArch = System.getProperty("os.arch")

val nativeTargetName = (findProperty("native.target") as String?)
    ?: when {
        hostOs.startsWith("Mac") && hostArch == "aarch64" -> "macosArm64"
        hostOs.startsWith("Linux") -> "linuxX64"
        else -> error("Unsupported host OS: $hostOs ($hostArch)")
    }

kotlin {
    val macosTarget = macosArm64()
    val linuxTarget = linuxX64()

    // Same approach as ftxui-kt: keep nativeMain empty so metadata compilation
    // doesn't need cinterop klibs from non-host targets.
    sourceSets {
        val nativeMain by getting { kotlin.setSrcDirs(emptyList<Any>()) }
        val nativeTest by getting { kotlin.setSrcDirs(emptyList<Any>()) }
        val macosArm64Main by getting { kotlin.srcDir("src/nativeMain/kotlin") }
        val linuxX64Main by getting { kotlin.srcDir("src/nativeMain/kotlin") }
        val macosArm64Test by getting { kotlin.srcDir("src/nativeTest/kotlin") }
        val linuxX64Test by getting { kotlin.srcDir("src/nativeTest/kotlin") }
    }

    val nativeTarget = when (nativeTargetName) {
        "macosArm64" -> macosTarget
        "linuxX64" -> linuxTarget
        else -> error("Unsupported target: $nativeTargetName")
    }

    nativeTarget.apply {
        compilations.getByName("main") {
            defaultSourceSet {
                dependencies {
                    implementation(project(":ftxui-kt"))
                }
            }
        }
        compilations.getByName("test") {
            defaultSourceSet {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
        }
    }
}
