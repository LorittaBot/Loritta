package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIconUtils
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons

fun FlowContent.channelSelectMenu(
    guild: Guild,
    selectedChannelId: Long?,
    additionalOptions: SELECT.() -> (Unit) = {},
    attrs: SELECT.() -> (Unit) = {}
) {
    select {
        attributes["bliss-component"] = "fancy-select-menu"
        attributes["fancy-select-menu-chevron-svg"] = SVGIcons.CaretDown.html.toString()

        attrs()

        additionalOptions()

        for (channel in guild.channels) {
            if (channel is GuildMessageChannel) {
                option {
                    this.label = channel.name
                    this.value = channel.id
                    this.selected = selectedChannelId == channel.idLong
                    this.disabled = false
                    this.attributes["fancy-select-menu-label"] = createHTML(false)
                        .div {
                            div {
                                style = "display: flex; gap: 4px; align-items: center;"

                                val svgIcon = SVGIconUtils.getSVGIconForChannel(guild, channel)

                                svgIcon(svgIcon) {
                                    this.attr("style", "height: 1em; width: 1em; opacity: 0.75;")
                                }

                                span {
                                    text(channel.name)
                                }
                            }
                        }
                }
            }
        }
    }
}