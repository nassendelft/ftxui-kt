import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

plugins {
    kotlin("multiplatform") version "2.3.21"
    id("publishing-conventions")
}

group = "nl.ncaj.ftxui"

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

val ftxuiCVersion = "v1.2.0"

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
        val maxAttempts = 5
        for (attempt in 1..maxAttempts) {
            logger.lifecycle("Downloading $url (attempt $attempt/$maxAttempts)")
            try {
                val connection = URI(url).toURL().openConnection() as HttpURLConnection
                connection.connectTimeout = 30_000
                connection.readTimeout = 60_000
                connection.instanceFollowRedirects = true
                connection.inputStream.use { input -> dest.outputStream().use { input.copyTo(it) } }
                return@doLast
            } catch (e: IOException) {
                if (attempt == maxAttempts) throw e
                val backoffSeconds = attempt * 5L
                logger.lifecycle("Download failed (${e.message}), retrying in ${backoffSeconds}s...")
                Thread.sleep(backoffSeconds * 1000)
            }
        }
    }
}

val extractFtxuiC = tasks.register("extractFtxuiC") {
    val srcDir = file("../ftxui-c")
    val localApiHeader = File(srcDir, "ftxui_c_api.h")
    val useLocal = localApiHeader.exists()

    if (useLocal) {
        inputs.dir(srcDir)
    } else {
        dependsOn(downloadFtxuiC)
        inputs.file(archiveFile)
    }

    val destDir = nativeDir.map { it.asFile }
    outputs.dir(nativeDir.map { it.dir("lib") })

    doLast {
        val dest = destDir.get()
        dest.mkdirs()

        if (useLocal) {
            logger.lifecycle("Using local ftxui-c repository at ${srcDir.absolutePath}")
            File(dest, "include").mkdirs()
            File(dest, "lib").mkdirs()
            copy {
                from(File(srcDir, "ftxui_c_api.h"))
                into(File(dest, "include"))
            }
            copy {
                from(File(srcDir, "build/libftxui_c_binding.a"))
                into(File(dest, "lib"))
            }
            copy {
                from(File(srcDir, "build/ftxui_build/libftxui-component.a"))
                into(File(dest, "lib"))
            }
            copy {
                from(File(srcDir, "build/ftxui_build/libftxui-dom.a"))
                into(File(dest, "lib"))
            }
            copy {
                from(File(srcDir, "build/ftxui_build/libftxui-screen.a"))
                into(File(dest, "lib"))
            }
        } else {
            logger.lifecycle("Extracting downloaded ftxui-c archive from ${archiveFile.get().asFile.absolutePath}")
            project.providers.exec {
                commandLine("tar", "-xzf", archiveFile.get().asFile.absolutePath, "-C", dest.absolutePath)
            }.result.get()
        }
    }
}

kotlin {
    val macosTarget = macosArm64()
    val linuxTarget = linuxX64()

    //    @file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

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

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
        }
    }
}

// Both cinterop tasks need headers from the extract step — the non-host also uses the host's archive.
tasks.getByName("cinteropFtxui_cMacosArm64").dependsOn(extractFtxuiC)
tasks.getByName("cinteropFtxui_cLinuxX64").dependsOn(extractFtxuiC)
