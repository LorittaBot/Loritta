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
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.xprewards.RoleReward

fun FlowContent.configurableRoleRewards(
    i18nContext: I18nContext,
    guild: Guild,
    roles: List<RoleReward>
) {
    // This is stupid, but we *need* to have a div to allow the save bar to detect the swap!
    div {
        if (roles.isNotEmpty()) {
            div(classes = "cards") {
                for ((index, roleReward) in roles.sortedBy { it.xp }.withIndex()) {
                    div(classes = "card") {
                        style = "flex-direction: row; align-items: center; gap: 0.5em;"

                        div {
                            style = "flex-grow: 1;"
                            text("Ao chegar em ")
                            b {
                                text("${roleReward.xp} XP")
                            }
                            text(" (Nível ${ExperienceUtils.getCurrentLevelForXp(roleReward.xp)})")
                            text(", ")
                            text("o usuário receberá o cargo ")

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
                        }

                        hiddenInput {
                            attributes["xp-action-add-element"] = "true"
                            attributes["bliss-parse-to-json"] = "true"
                            attributes["loritta-config"] = "roles[]"
                            attributes["role-rewards-role"] = "true"
                            name = "roles[]"
                            value = buildJsonObject {
                                put("xp", roleReward.xp)
                                put("roleId", roleReward.roleId)
                            }.toString()
                        }

                        discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                            attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.idLong}/xp-rewards/remove"
                            attributes["bliss-vals-json"] = buildJsonObject {
                                put("index", index)
                            }.toString()
                            attributes["bliss-swap:200"] = "body (innerHTML) -> #role-rewards (innerHTML)"
                            attributes["bliss-include-json"] = "[role-rewards-role]"

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