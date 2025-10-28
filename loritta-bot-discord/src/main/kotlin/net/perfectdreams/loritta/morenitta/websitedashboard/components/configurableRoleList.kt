package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.hiddenInput
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext

fun FlowContent.configurableRoleList(
    i18nContext: I18nContext,
    guild: Guild,
    removeEndpoint: String,
    roleIds: Set<Long>
) {
    div(classes = "simple-configurable-list-inset") {
        if (roleIds.isNotEmpty()) {
            for (roleId in roleIds) {
                hiddenInput {
                    name = "roles[]"
                    attributes["loritta-config"] = "roles[]"
                    value = roleId.toString()
                }

                val role = guild.getRoleById(roleId)

                div(classes = "entry") {
                    div(classes = "discord-mention") {
                        val roleColor = role?.color
                        if (roleColor != null) {
                            style = "--mention-color: rgb(${roleColor.red}, ${roleColor.green}, ${roleColor.blue});"
                        }

                        span(classes = "content") {
                            text("@")
                            text((role?.name ?: "???") + " (${roleId})")
                        }
                    }

                    discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                        style = "margin-left: auto;"

                        attributes["bliss-post"] = removeEndpoint
                        attributes["bliss-include-json"] = "[name='roles[]']"
                        attributes["bliss-vals-json"] = buildJsonObject {
                            put("roleId", roleId.toString())
                        }.toString()
                        attributes["bliss-swap:200"] = "body (innerHTML) -> #roles (innerHTML)"
                        attributes["bliss-sync"] = "#add-role-button"

                        text("Remover")
                    }
                }
            }
        } else {
            emptySection(i18nContext)
        }
    }
}