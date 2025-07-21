pluginManagement {
    repositories {
        // I don't know why but if "gradlePluginPortal()" is before our custom Maven repo, the i18nHelper plugin isn't found
        maven("https://repo.perfectdreams.net/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Just because a variable is unused here, does NOT mean that it is unused in a project!
            val kotlinXSerialization = version("kotlinx-serialization", "1.8.1")
            val ktor = version("ktor", "3.1.3")
            val exposed = version("exposed", "0.60.0")
            val i18nHelper = version("i18nhelper", "0.0.6-SNAPSHOT")
            val i18nHelperPlugin = version("i18nhelperplugin", "0.0.6-SNAPSHOT")
            val logback = version("logback", "1.5.18")
            val kotlinxCoroutines = version("kotlinx-coroutines", "1.10.2")
            val jib = version("jib", "3.4.3")

            library("deviousjda", "com.github.LorittaBot", "DeviousJDA").version("ab69faa133")

            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version(kotlinxCoroutines)
            library("kotlinx-coroutines-debug", "org.jetbrains.kotlinx", "kotlinx-coroutines-debug").version(kotlinxCoroutines)

            library("kotlin-logging", "io.github.microutils", "kotlin-logging").version("2.1.23")

            library("kotlinx-serialization-core", "org.jetbrains.kotlinx", "kotlinx-serialization-core").versionRef(kotlinXSerialization)
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef(kotlinXSerialization)
            library("kotlinx-serialization-protobuf", "org.jetbrains.kotlinx", "kotlinx-serialization-protobuf").versionRef(kotlinXSerialization)
            library("kotlinx-serialization-hocon", "org.jetbrains.kotlinx", "kotlinx-serialization-hocon").versionRef(kotlinXSerialization)
            library("ktor-server-core", "io.ktor", "ktor-server-core").versionRef(ktor)
            library("ktor-server-netty", "io.ktor", "ktor-server-netty").versionRef(ktor)
            library("ktor-server-cio", "io.ktor", "ktor-server-cio").versionRef(ktor)
            library("ktor-client-core", "io.ktor", "ktor-client-core").versionRef(ktor)
            library("ktor-client-js", "io.ktor", "ktor-client-js").versionRef(ktor)
            library("ktor-client-cio", "io.ktor", "ktor-client-cio").versionRef(ktor)
            library("ktor-server-sessions", "io.ktor", "ktor-server-sessions").versionRef(ktor)

            library("exposed-core", "org.jetbrains.exposed", "exposed-core").versionRef(exposed)
            library("exposed-jdbc", "org.jetbrains.exposed", "exposed-jdbc").versionRef(exposed)
            library("exposed-javatime", "org.jetbrains.exposed", "exposed-java-time").versionRef(exposed)
            library("exposed-dao", "org.jetbrains.exposed", "exposed-dao").versionRef(exposed)

            library("logback-classic", "ch.qos.logback", "logback-classic").versionRef(logback)

            library("hikaricp", "com.zaxxer", "HikariCP").version("6.3.0")
            library("postgresqljdbcdriver", "org.postgresql", "postgresql").version("42.7.6")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "loritta-parent"

// ===[ PUDDING ]===
include(":pudding:client")

// ===[ COMMON ]===
include(":common")
include(":loritta-serializable-commons")

// ===[ LORITTA ]===
include(":loritta-bot-discord")

// ===[ SPICY MORENITTA ]===
include(":web:spicy-morenitta")

// ===[ EMBED EDITOR ]===
include(":web:embed-editor:embed-renderer")
include(":web:embed-editor:embed-editor-crosswindow")
include(":web:embed-editor:embed-renderer")

// ===[ LORITTA'S WEBSITE ]===
include(":loritta-website:web-common")
include(":loritta-website:loritta-website-backend")
include(":loritta-website:loritta-website-frontend")

// ===[ DASHBOARD ]===
// Disabled for now because something (Compose?) is causing errors on the spicy-frontend module
//  IrSimpleFunctionSymbolImpl is already bound. Signature: null. Owner: FUN LOCAL_FUNCTION_FOR_LAMBDA name:<anonymous> visibility:local modality:FINAL <> ($composer:androidx.compose.runtime.Composer?, $changed:kotlin.Int) returnType:kotlin.Unit
// include(":web:dashboard:dashboard-common")
// include(":web:dashboard:backend")
// include(":web:dashboard:spicy-frontend")

// ===[ LOTRUNFO ]===
include(":lotrunfo:lotrunfo-backend")
include(":lotrunfo:lotrunfo-frontend")

// ===[ MISC ]===
include(":loritta-helper")
include(":temmie-discord-auth")
include(":temmie-discord-auth-loritta-commons")
include(":switch-twitch")
include(":broker-tickers-updater")
include(":loricoolcards-generator")
include(":discord-chat-markdown-parser")
include(":discord-chat-markdown-parser-samples")
include(":discord-chat-message-renderer-entities")
include(":discord-chat-message-renderer-server")
include(":lori-api-proxy")
include(":lori-public-http-api-common")
include(":yokye")