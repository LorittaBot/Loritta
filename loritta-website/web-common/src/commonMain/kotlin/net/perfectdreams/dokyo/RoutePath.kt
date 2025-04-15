package net.perfectdreams.dokyo

object RoutePath {
    const val HOME = "/"
    const val SUPPORT = "/support"
    const val COMMANDS_REDIRECT = "/commands/{category?}"
    const val LEGACY_COMMANDS = "/commands/legacy/{category?}"
    const val APPLICATION_COMMANDS = "/commands/slash/{category?}"
    const val PREMIUM = "/donate"
    const val EXTRAS = "/extras/{renderPage...}"
    const val BLOG = "/blog"
    const val BLOG_POST = "/blog/{post}"
    const val TEAM = "/staff"
    const val CONTACT = "/contact"
    const val DASHBOARD = "/dashboard"
}