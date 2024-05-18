package net.perfectdreams.loritta.morenitta.website.components

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.SVGIcon.svgIcon
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal

object DiscordChannelSelectMenu {
    fun FlowContent.discordChannelSelectMenu(
        lorittaWebsite: LorittaWebsite,
        i18nContext: I18nContext,
        name: String,
        channels: List<GuildChannel>,
        selectedChannelId: Long?,
        nullOption: (SPAN.() -> (Unit))?
    ) {
        select {
            this.name = name
            attributes["loritta-select-menu"] = "true"
            style = "width: 100%;"

            if (nullOption != null) {
                option {
                    value = ""
                    attributes["loritta-select-menu-text"] = createHTML()
                        .span {
                            nullOption.invoke(this)
                        }

                    if (selectedChannelId == null)
                        selected = true

                    text("")
                }
            }

            for (channel in channels) {
                val hasPermissionToTalk = if (channel is GuildMessageChannel)
                    channel.canTalk()
                else
                    false

                option {
                    if (!hasPermissionToTalk) {
                        // Can't talk on this channel! Disable the option and show a modal when selecting the channel
                        disabled = true
                        attributes["loritta-select-menu-open-embedded-modal-on-select"] =
                            EmbeddedSpicyModalUtils.encodeURIComponent(
                                Json.encodeToString(
                                    EmbeddedSpicyModal(
                                        i18nContext.get(I18nKeysData.Website.Dashboard.ChannelNoTalkPermissionModal.Title),
                                        true,
                                        createHTML()
                                            .div {
                                                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.ChannelNoTalkPermissionModal.Description)) {
                                                    p {
                                                        text(line)
                                                    }
                                                }
                                            },
                                        listOf(
                                            createHTML()
                                                .button(classes = "discord-button") {
                                                    defaultModalCloseButton(i18nContext)
                                                }
                                        )
                                    )
                                )
                            )
                    }

                    attributes["loritta-select-menu-text"] = createHTML()
                        .div(classes = "text-with-icon-wrapper") {
                            when (channel.type) {
                                ChannelType.TEXT -> svgIcon(lorittaWebsite.svgIconManager.discordTextChannel, "text-icon")
                                ChannelType.NEWS -> svgIcon(lorittaWebsite.svgIconManager.discordNewsChannel, "text-icon")
                                else -> svgIcon(lorittaWebsite.svgIconManager.discordTextChannel, "text-icon")
                            }
                            div {
                                text(channel.name)

                                if (channel is ICategorizableChannel) {
                                    val parentCategory = channel.parentCategory
                                    if (parentCategory != null) {
                                        text(" ")
                                        span {
                                            style = "font-size: 0.8em; font-weight: bold;"
                                            text(parentCategory.name.uppercase())
                                        }
                                    }
                                }

                                if (!hasPermissionToTalk) {
                                    text(" ")
                                    span(classes = "tag warn") {
                                        text(i18nContext.get(I18nKeysData.Website.Dashboard.ChannelNoTalkPermissionModal.Title))
                                    }
                                }
                            }
                        }

                    value = channel.idLong.toString()
                    if (channel.idLong == selectedChannelId) {
                        selected = true
                    }
                    text("#${channel.name} (${channel.idLong})")
                }
            }
        }
    }
}