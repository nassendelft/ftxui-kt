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

// ftxui-kt is released independently with its own version numbers. Override its `version`
// here so the dependency declared in this module's published POM points at the ftxui-kt
// release this module was actually built against, rather than whatever `publishVersion`
// this build invocation passes for ftxui-kt-dsl (which may not be a published ftxui-kt version).
project(":ftxui-kt").version = property("ftxuiKtVersion") as String

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
