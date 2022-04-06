pluginManagement {
    repositories {
        // I don't know why but if "gradlePluginPortal()" is before our custom Maven repo, the i18nHelper plugin isn't found
        maven("https://repo.perfectdreams.net/")
        gradlePluginPortal()
    }
}

rootProject.name = "cinnamon-parent"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.6.20")
            version("kotlinx-serialization", "1.3.2")
            version("ktor", "1.6.7")
            version("jib", "3.2.1")
            version("exposed", "0.37.3")

            alias("kotlinx-coroutines-core").to("org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.6.1")
            alias("kotlin-logging").to("io.github.microutils", "kotlin-logging").version("2.1.21")

            alias("kotlinx-serialization-core").to("org.jetbrains.kotlinx", "kotlinx-serialization-core").versionRef("kotlinx-serialization")
            alias("kotlinx-serialization-json").to("org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef("kotlinx-serialization")
            alias("kotlinx-serialization-protobuf").to("org.jetbrains.kotlinx", "kotlinx-serialization-protobuf").versionRef("kotlinx-serialization")
            alias("kotlinx-serialization-hocon").to("org.jetbrains.kotlinx", "kotlinx-serialization-hocon").versionRef("kotlinx-serialization")
            alias("ktor-client-core").to("io.ktor", "ktor-client-core").versionRef("ktor")

            alias("exposed-core").to("org.jetbrains.exposed", "exposed-core").versionRef("exposed")
            alias("exposed-jdbc").to("org.jetbrains.exposed", "exposed-jdbc").versionRef("exposed")
            alias("exposed-javatime").to("org.jetbrains.exposed", "exposed-java-time").versionRef("exposed")

            alias("hikaricp").to("com.zaxxer", "HikariCP").version("5.0.1")
        }
    }
}

include(":common")

// ===[ PUDDING ]===
// The reason this is not named "common" is because IDEA was getting a bit confusing due to duplicated names
// (errors related to class not found)
include(":pudding:data")
include(":pudding:client")

// ===[ DISCORD ]===
// The reason this is not named "common" is because IDEA was getting a bit confusing due to duplicated names
// (errors related to class not found)
include(":discord:discord-common")
include(":discord:commands")
include(":discord:web-server")
include(":discord:gateway")

// ===[ MICROSERVICES ]===
include(":microservices:broker-tickers-updater")
include(":microservices:stats-collector")
include(":microservices:direct-message-processor")
include(":microservices:daily-tax")
include(":microservices:correios-package-tracker")

// ===[ WEB ]===
include(":web:showtime:web-common")
include(":web:showtime:backend")
include(":web:showtime:frontend")
