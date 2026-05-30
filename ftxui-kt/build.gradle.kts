import java.net.URI

plugins {
    kotlin("multiplatform")
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

val ftxuiCVersion = "v1.0.0"

val ftxuiCPlatform = when {
    hostOs.startsWith("Linux") && hostArch == "aarch64" -> "linux-arm64"
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
    val nativeTarget = when (nativeTargetName) {
        "macosArm64" -> macosArm64()
        "linuxArm64" -> linuxArm64()
        "linuxX64" -> linuxX64()
        else -> error("Unsupported target: $nativeTargetName")
    }

    nativeTarget.apply {
        compilations.getByName("main") {
            val ftxui_c by cinterops.creating {
                includeDirs(nativeDir.map { it.dir("include").asFile })
                extraOpts(
                    "-libraryPath", nativeDir.map { it.dir("lib").asFile.absolutePath }.get()
                )
            }
        }
    }
}

val cinteropTask = "cinteropFtxui_c${nativeTargetName.replaceFirstChar { it.uppercase() }}"
tasks.getByName(cinteropTask).dependsOn(extractFtxuiC)
