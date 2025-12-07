package net.perfectdreams.loritta.morenitta.websitedashboard.components

import dev.minn.jda.ktx.generics.getChannel
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.hiddenInput
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIconUtils

fun FlowContent.configurableChannelList(
    i18nContext: I18nContext,
    guild: Guild,
    swapToElementId: String,
    channelsName: String,
    removeEndpoint: String,
    channelIds: Set<Long>
) {
    div(classes = "simple-configurable-list-inset") {
        if (channelIds.isNotEmpty()) {
            for (channelId in channelIds) {
                hiddenInput {
                    name = "$channelsName[]"
                    attributes["loritta-config"] = "$channelsName[]"
                    value = channelId.toString()
                }

                val channel = guild.getChannel(channelId)

                div(classes = "entry") {
                    val svgIcon = SVGIconUtils.getSVGIconForChannelFallbackIfNull(guild, channel)

                    div(classes = "discord-mention has-icon") {
                        svgIcon(svgIcon)

                        span(classes = "content") {
                            text((channel?.name ?: "???") + " (${channelId})")
                        }
                    }

                    discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT) {
                        style = "margin-left: auto;"

                        attributes["bliss-post"] = removeEndpoint
                        attributes["bliss-include-json"] = "[name='$channelsName[]']"
                        attributes["bliss-remap-json-keys"] = buildJsonObject {
                            put(channelsName, "channels")
                        }.toString()
                        attributes["bliss-vals-json"] = buildJsonObject {
                            put("channelId", channelId.toString())
                            put("swapToElementId", swapToElementId)
                            put("channelsName", channelsName)
                        }.toString()
                        attributes["bliss-swap:200"] = "body (innerHTML) -> #$swapToElementId (innerHTML)"
                        attributes["bliss-sync"] = "#add-channel-button"

                        text("Remover")
                    }
                }
            }
        } else {
            emptySection(i18nContext)
        }
    }
}