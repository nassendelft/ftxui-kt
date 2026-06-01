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

// Same lazy source-set approach as ftxui-kt.
kotlin.sourceSets.matching { it.name in setOf("nativeMain", "nativeTest") }.configureEach {
    kotlin.setSrcDirs(emptyList<Any>())
}
kotlin.sourceSets.matching { it.name in setOf("macosArm64Main", "linuxX64Main") }.configureEach {
    kotlin.srcDir("src/nativeMain/kotlin")
}
kotlin.sourceSets.matching { it.name in setOf("macosArm64Test", "linuxX64Test") }.configureEach {
    kotlin.srcDir("src/nativeTest/kotlin")
}
