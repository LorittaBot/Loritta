// Kotlin 1.5.0 compiler issue with Ktor workaround, See: https://youtrack.jetbrains.com/issue/KT-46082
config.resolve.alias = {
    "crypto": false,
}