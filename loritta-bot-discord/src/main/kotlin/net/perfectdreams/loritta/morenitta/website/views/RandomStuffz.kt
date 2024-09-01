package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents

/**
 * Setups `hx-push-url` to be the same URL as the configured `href`
 */
fun A.htmxGetAsHref() {
    attributes["hx-get"] = this.href
}

fun A.htmxDiscordLikeLoadingButtonSetup(
    i18nContext: I18nContext,
    buttonContent: DIV.() -> (Unit)
) {
    classes += "htmx-discord-like-loading-button"
    attributes["hx-indicator"] = "this"
    // Anchor tags can't be disabled
    // attributes["hx-disabled-elt"] = "this"

    div {
        buttonContent.invoke(this)
    }

    div(classes = "loading-text-wrapper") {
        img(src = LoadingSectionComponents.list.random())

        text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
    }
}

fun BUTTON.htmxDiscordLikeLoadingButtonSetup(
    i18nContext: I18nContext,
    buttonContent: DIV.() -> (Unit)
) {
    attributes["hx-indicator"] = "find .htmx-discord-like-loading-button"
    attributes["hx-disabled-elt"] = "this"

    div(classes = "htmx-discord-like-loading-button") {
        div {
            buttonContent.invoke(this)
        }

        div(classes = "loading-text-wrapper") {
            img(src = LoadingSectionComponents.list.random())

            text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
        }
    }
}