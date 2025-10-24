package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.b
import kotlinx.html.div
import kotlinx.html.hiddenInput
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.ExperienceUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprates.RoleRate

fun FlowContent.configurableRoleRates(
    i18nContext: I18nContext,
    guild: Guild,
    roles: List<RoleRate>
) {
    // This is stupid, but we *need* to have a div to allow the save bar to detect the swap!
    div {
        if (roles.isNotEmpty()) {
            div(classes = "cards") {
                for ((index, roleReward) in roles.sortedBy { it.rate }.withIndex()) {
                    div(classes = "card") {
                        style = "flex-direction: row; align-items: center; gap: 0.5em;"

                        div {
                            style = "flex-grow: 1;"
                            text("Usuários com o cargo ")
                            val role = guild.getRoleById(roleReward.roleId)
                            span(classes = "discord-mention") {
                                val roleColor = role?.color
                                style = if (roleColor != null) {
                                    "--mention-color: rgb(${roleColor.red}, ${roleColor.green}, ${roleColor.blue}); height: fit-content;"
                                } else {
                                    "height: fit-content;"
                                }

                                text("@")
                                text((role?.name ?: "???") + " (${roleReward.roleId})")
                            }
                            text(" irão ganhar ")
                            b {
                                text("${roleReward.rate}x")
                            }
                            text(" mais XP")
                        }

                        hiddenInput {
                            attributes["xp-action-add-element"] = "true"
                            attributes["bliss-parse-to-json"] = "true"
                            attributes["loritta-config"] = "roles[]"
                            attributes["role-rates-role"] = "true"
                            name = "roles[]"
                            value = buildJsonObject {
                                put("rate", roleReward.rate)
                                put("roleId", roleReward.roleId)
                            }.toString()
                        }

                        discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-rates/remove"
                            attributes["bliss-vals-json"] = buildJsonObject {
                                put("index", index)
                            }.toString()
                            attributes["bliss-swap:200"] = "body (innerHTML) -> #role-rates (innerHTML)"
                            attributes["bliss-include-json"] = "[role-rates-role]"

                            text("Remover")
                        }
                    }
                }
            }
        } else {
            emptySection(i18nContext)
        }
    }
}