package net.perfectdreams.dokyo

object RoutePath {
    const val HOME = "/"
    const val SUPPORT = "/support"
    const val LEGACY_COMMANDS = "/commands/legacy/{category?}"
    const val APPLICATION_COMMANDS = "/commands/slash/{category?}"
    const val PREMIUM = "/donate"
    const val EXTRAS = "/extras/{renderPage...}"
}