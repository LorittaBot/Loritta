package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import kotlinx.html.*
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.I18nData
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedNotEnoughSonhosModalOnClick
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.defaultModalCloseButton
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import java.util.UUID

/**
 * Creates an embedded toast
 */
fun createEmbeddedToast(
    type: EmbeddedToast.Type,
    title: String,
    description: (DIV.() -> (Unit))? = null
): EmbeddedToast {
    return EmbeddedToast(
        type,
        title,
        if (description == null) null else createHTML(false).div { description() }
    )
}

/**
 * Adds a "show toast" to the DOM
 */
fun FlowContent.blissShowToast(modal: EmbeddedToast) {
    script(type = "application/json") {
        attributes["bliss-show-toast"] = "true"
        attributes["bliss-toast"] = BlissHex.encodeToHexString(Json.encodeToString(modal))
    }
}