package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.serializable.ColorTheme

fun FlowContent.authorizationFailedFullScreenError(
    i18nContext: I18nContext,
    errorToBeDisplayed: String?,
    authUrl: String
) {
    // We don't use the user's theme because we are NOT logged in, so we don't know which theme the user prefers
    div(classes = "full-screen-error ${ColorTheme.SYNC_WITH_SYSTEM.className}") {
        img(src = "https://stuff.loritta.website/emotes/lori-sob.png") {
            width = "192"
            height = "192"
        }

        h1 {
            text(i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.Title))
        }

        if (errorToBeDisplayed != null) {
            div {
                style = "font-weight: bold;"

                text(errorToBeDisplayed)
            }
        }

        a(href = "https://youtu.be/fRh_vgS2dFE", target = "_blank") {
            attributes["aria-hidden"] = "true"
            style = "font-style: italic; margin-top: 8px; color: inherit; text-decoration: none; background: none; font-weight: inherit; font-style: italic;"
            text(i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.IsItTooLateNowToSaySorry))
        }

        div {
            discordButtonLink(ButtonStyle.PRIMARY, href = authUrl) {
                style = "margin-top: 24px;"

                text(i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedFullScreenError.TryAgain))
            }
        }
    }
}