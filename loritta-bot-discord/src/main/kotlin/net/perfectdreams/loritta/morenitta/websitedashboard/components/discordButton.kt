package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.A
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.button

fun FlowContent.discordButton(type: ButtonStyle, block: BUTTON.() -> (Unit)) {
    button(classes = "discord-button ${type.className}") {
        block()
    }
}

fun FlowContent.discordButtonLink(type: ButtonStyle, href: String?, block: A.() -> (Unit)) {
    a(href = href, classes = "discord-button ${type.className}") {
        block()
    }
}

enum class ButtonStyle(val className: String) {
    PRIMARY("primary"),
    SUCCESS("success"),
    DANGER("danger"),
    NO_BACKGROUND_THEME_DEPENDENT_LIGHT_TEXT("no-background-theme-dependent-light-text"),
    NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT("no-background-theme-dependent-dark-text"),
}