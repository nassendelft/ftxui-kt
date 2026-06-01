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

kotlin {
    macosArm64()
    linuxX64()
}

kotlin.sourceSets.matching { it.name == "nativeMain" }.configureEach {
    dependencies {
        implementation(project(":ftxui-kt"))
    }
}
kotlin.sourceSets.matching { it.name == "nativeTest" }.configureEach {
    dependencies {
        implementation(kotlin("test"))
    }
}
