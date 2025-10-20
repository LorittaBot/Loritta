package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import io.ktor.server.application.ApplicationCall
import kotlinx.html.FlowContent
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml

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
    this.respondHtml(
        createHTML(false)
            .body {
                configSaved(i18nContext)

                block()
            }
    )
}