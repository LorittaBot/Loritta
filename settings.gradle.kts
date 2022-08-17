pluginManagement {
    repositories {
        // I don't know why but if "gradlePluginPortal()" is before our custom Maven repo, the i18nHelper plugin isn't found
        maven("https://repo.perfectdreams.net/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        gradlePluginPortal()
    }
}

rootProject.name = "cinnamon-parent"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlin = version("kotlin", "1.7.10")
            val kotlinXSerialization = version("kotlinx-serialization", "1.4.0-RC")
            val ktor = version("ktor", "2.0.3")
            val jib = version("jib", "3.2.1")
            val exposed = version("exposed", "0.38.2")
            val i18nHelper = version("i18nhelper", "0.0.5-SNAPSHOT")
            val logback = version("logback", "1.3.0-alpha16")
            val kotlinxCoroutines = version("kotlinx-coroutines", "1.6.4")

            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version(kotlinxCoroutines)
            library("kotlinx-coroutines-debug", "org.jetbrains.kotlinx", "kotlinx-coroutines-debug").version(kotlinxCoroutines)

            library("kotlin-logging", "io.github.microutils", "kotlin-logging").version("2.1.23")

            library("kotlinx-serialization-core", "org.jetbrains.kotlinx", "kotlinx-serialization-core").versionRef(kotlinXSerialization)
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef(kotlinXSerialization)
            library("kotlinx-serialization-protobuf", "org.jetbrains.kotlinx", "kotlinx-serialization-protobuf").versionRef(kotlinXSerialization)
            library("kotlinx-serialization-hocon", "org.jetbrains.kotlinx", "kotlinx-serialization-hocon").versionRef(kotlinXSerialization)
            library("ktor-server-netty", "io.ktor", "ktor-server-netty").versionRef(ktor)
            library("ktor-client-core", "io.ktor", "ktor-client-core").versionRef(ktor)
            library("ktor-client-js", "io.ktor", "ktor-client-js").versionRef(ktor)
            library("ktor-client-cio", "io.ktor", "ktor-client-cio").versionRef(ktor)

            library("exposed-core", "org.jetbrains.exposed", "exposed-core").versionRef(exposed)
            library("exposed-jdbc", "org.jetbrains.exposed", "exposed-jdbc").versionRef(exposed)
            library("exposed-javatime", "org.jetbrains.exposed", "exposed-java-time").versionRef(exposed)

            library("logback-classic", "ch.qos.logback", "logback-classic").versionRef(logback)

            library("hikaricp", "com.zaxxer", "HikariCP").version("5.0.1")
        }
    }
}

include(":common")

// ===[ PUDDING ]===
include(":pudding:data")
include(":pudding:client")

// ===[ DISCORD ]===
// The reason this is not named "common" is because IDEA was getting a bit confusing due to duplicated names
// (errors related to class not found)
include(":discord:discord-common")
include(":discord:web-server")
include(":discord:gateway")

// ===[ MICROSERVICES ]===
include(":microservices:broker-tickers-updater")
include(":microservices:stats-collector")
include(":microservices:interactions-http-coordinator")

// ===[ SHOWTIME ]===
include(":web:showtime:web-common")
include(":web:showtime:backend")
include(":web:showtime:showtime-frontend")

// ===[ DASHBOARD ]===
include(":web:dashboard:common")
include(":web:dashboard:backend")
include(":web:dashboard:spicy-frontend")