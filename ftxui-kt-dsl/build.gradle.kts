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
        hostOs.startsWith("Linux") && hostArch == "aarch64" -> "linuxArm64"
        hostOs.startsWith("Linux") -> "linuxX64"
        else -> error("Unsupported host OS: $hostOs ($hostArch)")
    }

kotlin {
    val nativeTarget = when (nativeTargetName) {
        "macosArm64" -> macosArm64()
        "linuxArm64" -> linuxArm64()
        "linuxX64" -> linuxX64()
        else -> error("Unsupported target: $nativeTargetName")
    }

    nativeTarget.apply {
        compilations.getByName("main") {
            defaultSourceSet {
                dependencies {
                    implementation("nl.ncaj.ftxui:ftxui-kt:${property("ftxuiKtVersion")}")
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
