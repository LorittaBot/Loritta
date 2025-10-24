package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData

fun FlowContent.configurableRoleListInput(
    i18nContext: I18nContext,
    guild: Guild,
    rolesName: String,
    swapToElementId: String,
    addEndpoint: String,
    removeEndpoint: String,
    roleIds: Set<Long>
) {
    controlsWithButton {
        growInputWrapper {
            roleSelectMenu(
                guild,
                null
            ) {
                this.name = "roleId"
            }
        }

        discordButton(ButtonStyle.SUCCESS) {
            id = "add-role-button"
            attributes["bliss-indicator"] = "this"
            attributes["bliss-post"] = addEndpoint
            attributes["bliss-include-json"] = "[name='roleId'],[name='$rolesName[]']"
            attributes["bliss-swap:200"] = "body (innerHTML) -> #$swapToElementId (innerHTML)"
            attributes["bliss-sync"] = "#add-role-button"

            div {
                text("Adicionar")
            }

            div(classes = "loading-text-wrapper") {
                loadingSpinnerImage()

                text(i18nContext.get(I18nKeysData.Website.Dashboard.Loading))
            }
        }
    }

    div {
        id = swapToElementId

        configurableRoleList(
            i18nContext,
            guild,
            removeEndpoint,
            roleIds
        )
    }
}