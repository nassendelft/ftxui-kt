pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ftxui-kt"

include(":ftxui-kt")
include(":ftxui-kt-dsl")
