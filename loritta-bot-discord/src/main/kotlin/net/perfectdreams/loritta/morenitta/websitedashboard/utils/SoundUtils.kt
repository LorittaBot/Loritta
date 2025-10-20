package net.perfectdreams.loritta.morenitta.websitedashboard.utils

import kotlinx.html.*
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButton
import java.util.*
import kotlin.collections.set

/**
 * Adds a "trigger event" to the DOM
 */
fun FlowContent.blissSoundEffect(soundEffectName: String) {
    script(type = "application/json") {
        attributes["bliss-sound-effect"] = soundEffectName
    }
}