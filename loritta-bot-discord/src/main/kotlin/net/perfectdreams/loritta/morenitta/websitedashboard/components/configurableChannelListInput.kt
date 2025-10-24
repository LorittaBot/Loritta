package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.id
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData

fun FlowContent.configurableChannelListInput(
    i18nContext: I18nContext,
    guild: Guild,
    channelsName: String,
    swapToElementId: String,
    addEndpoint: String,
    removeEndpoint: String,
    channelIds: Set<Long>
) {
    controlsWithButton {
        growInputWrapper {
            channelSelectMenu(
                guild,
                null
            ) {
                this.name = "channelId"
            }
        }

        discordButton(ButtonStyle.SUCCESS) {
            id = "add-channel-button"
            attributes["bliss-indicator"] = "this"
            attributes["bliss-post"] = addEndpoint
            attributes["bliss-include-json"] = "[name='channelId'],[name='$channelsName[]']"
            attributes["bliss-swap:200"] = "body (innerHTML) -> #$swapToElementId (innerHTML)"
            attributes["bliss-sync"] = "#add-channel-button"

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

        configurableChannelList(
            i18nContext,
            guild,
            removeEndpoint,
            channelIds
        )
    }
}