import java.net.URI

plugins {
    kotlin("multiplatform")
    id("publishing-conventions")
}

publishing {
    publications.withType<MavenPublication>().all {
        pom {
            description.set("FTXUI Kotlin/Native bindings")
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

val ftxuiCVersion = "v1.0.0"

val ftxuiCPlatform = when {
    hostOs.startsWith("Linux") -> "linux-x86_64"
    hostOs.startsWith("Mac") -> "macos-arm64"
    else -> error("Unsupported platform: $hostOs ($hostArch)")
}

val nativeDir = layout.buildDirectory.dir("ftxui-c-native")
val archiveName = "ftxui-c-$ftxuiCVersion-$ftxuiCPlatform.tar.gz"
val archiveFile = nativeDir.map { it.file(archiveName) }

val downloadFtxuiC = tasks.register("downloadFtxuiC") {
    outputs.file(archiveFile)
    doFirst { nativeDir.get().asFile.mkdirs() }
    doLast {
        val dest = archiveFile.get().asFile
        val url = "https://github.com/nassendelft/ftxui-c/releases/download/$ftxuiCVersion/$archiveName"
        logger.lifecycle("Downloading $url")
        URI(url).toURL().openStream().use { input -> dest.outputStream().use { input.copyTo(it) } }
    }
}

val extractFtxuiC = tasks.register<Exec>("extractFtxuiC") {
    dependsOn(downloadFtxuiC)
    inputs.file(archiveFile)
    outputs.dir(nativeDir.map { it.dir("lib") })
    commandLine("tar", "-xzf", archiveFile.get().asFile.absolutePath, "-C", nativeDir.get().asFile.absolutePath)
}

kotlin {
    val macosTarget = macosArm64()
    val linuxTarget = linuxX64()

    val nativeTarget = when (nativeTargetName) {
        "macosArm64" -> macosTarget
        "linuxX64" -> linuxTarget
        else -> error("Unsupported target: $nativeTargetName")
    }

    // Apply the cinterop to both targets so the commonizer can produce a nativeMain klib.
    // The non-host target uses the host's downloaded headers and .a files — the FTXUI C API is
    // identical on all platforms, and the non-host binary is never linked on this machine.
    listOf(macosTarget, linuxTarget).forEach { target ->
        target.compilations.getByName("main") {
            val ftxui_c by cinterops.creating {
                includeDirs(nativeDir.map { it.dir("include").asFile })
                extraOpts("-libraryPath", nativeDir.map { it.dir("lib").asFile.absolutePath }.get())
            }
        }
    }
}

// Both cinterop tasks need headers from the extract step — the non-host also uses the host's archive.
tasks.getByName("cinteropFtxui_cMacosArm64").dependsOn(extractFtxuiC)
tasks.getByName("cinteropFtxui_cLinuxX64").dependsOn(extractFtxuiC)
