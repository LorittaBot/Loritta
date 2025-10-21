package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.hiddenInput
import kotlinx.html.style
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.warnactions.WarnAction

fun FlowContent.configurableWarnList(
    i18nContext: I18nContext,
    guild: Guild,
    warns: List<WarnAction>
) {
    // This is stupid, but we *need* to have a div to allow the save bar to detect the swap!
    div(classes = "cards") {
        for ((index, warn) in warns.sortedBy { it.count }.withIndex()) {
            div(classes = "card") {
                style = "flex-direction: row; align-items: center; gap: 0.5em;"

                div {
                    style = "flex-grow: 1;"
                    text("Ao chegar em ")
                    b {
                        text("${warn.count} avisos")
                    }
                    text(", ")
                    text("o usuário será ")
                    b {
                        text(warn.action.name)
                    }

                    if (warn.action == PunishmentAction.MUTE && warn.time != null) {
                        text(" por ")
                        b {
                            text(warn.time)
                        }
                    }
                }

                hiddenInput {
                    attributes["warn-action-add-element"] = "true"
                    attributes["bliss-parse-to-json"] = "true"
                    attributes["loritta-config"] = "actions[]"
                    attributes["warn-action-warn"] = "true"
                    name = "actions[]"
                    value = buildJsonObject {
                        put("count", warn.count)
                        put("action", warn.action.name)
                        put("time", warn.time)
                    }.toString()
                }

                discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                    attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/warn-actions/remove"
                    attributes["bliss-vals-json"] = buildJsonObject {
                        put("index", index)
                    }.toString()
                    attributes["bliss-swap:200"] = "body (innerHTML) -> #warn-actions (innerHTML)"
                    attributes["bliss-include-json"] = "[warn-action-warn]"

                    text("Remover")
                }
            }
        }
    }
}