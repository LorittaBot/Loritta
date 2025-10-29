package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import io.ktor.server.application.ApplicationCall
import kotlinx.html.FlowContent
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast

/**
 * Shows the generic toast for when the configuration was saved + other tidbits
 */
fun FlowContent.configSaved(i18nContext: I18nContext) {
    blissShowToast(
        createEmbeddedToast(
            EmbeddedToast.Type.SUCCESS,
            "Configuração salva!"
        )
    )

    blissEvent("resyncState", "[bliss-component='save-bar']")

    blissSoundEffect("configSaved")
}

/**
 * Creates the Loritta styled dashboard title
 */
fun dashboardTitle(i18nContext: I18nContext, section: String) = "$section • Painel da Loritta"

suspend fun ApplicationCall.respondConfigSaved(
    i18nContext: I18nContext,
    block: FlowContent.() -> Unit = {}
) {
    this.respondHtmlFragment {
        configSaved(i18nContext)

        block()
    }
}

/**
 * Calculates Discord's Guild Icon Short Name (the text used in the icon when the server does not have an icon)
 */
fun calculateGuildIconShortName(guildName: String): String {
    val builder = StringBuilder()
    val regex = Regex("[A-Za-z0-9]")

    // Replaces "MrPowerGamerBR's Server" with "MrPowerGamerBR Server" (result: "Ms")
    val preparedGuildName = guildName
        .replace(Regex("'s(\\s|\$)"), " ")

    var lastWasMatch = false
    for (char in preparedGuildName) {
        if (char.toString().matches(regex)) {
            if (!lastWasMatch) {
                builder.append(char)
                lastWasMatch = true
            }
            continue
        }

        if (char.isWhitespace()) {
            lastWasMatch = false
            continue
        }

        builder.append(char)
        lastWasMatch = false
    }

    return builder.toString()
}