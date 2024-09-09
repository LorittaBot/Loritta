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

            val selectedChannel = channels.firstOrNull {
                it.idLong == selectedChannelId
            }

            // If the selected channel is null, but the selected channel ID is NOT null, then it means that the channel has been deleted or something!
            // Show that it is an unknown channel
            if (selectedChannelId != null && selectedChannel == null) {
                option {
                    attributes["loritta-select-menu-open-embedded-modal-on-select"] =
                        EmbeddedSpicyModalUtils.encodeURIComponent(
                            Json.encodeToString(
                                EmbeddedSpicyModalUtils.createSpicyModal(
                                    i18nContext.get(I18nKeysData.Website.Dashboard.ChannelDoesNotExist.Modal.Title),
                                    true,
                                    {
                                        div {
                                            for (line in i18nContext.get(I18nKeysData.Website.Dashboard.ChannelDoesNotExist.Modal.Description)) {
                                                p {
                                                    text(line)
                                                }
                                            }
                                        }
                                    },
                                    EmbeddedSpicyModalUtils.modalButtonListOnlyCloseModalButton(i18nContext)
                                )
                            )
                        )

                    attributes["loritta-select-menu-text"] = createHTML()
                        .div(classes = "text-with-icon-wrapper") {
                            div {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.ChannelDoesNotExist.UnknownChannel(selectedChannelId.toString())))
                            }
                        }

                    selected = true
                    disabled = true
                    value = selectedChannelId.toString()

                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ChannelDoesNotExist.UnknownChannel(selectedChannelId.toString())))
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
                                    EmbeddedSpicyModalUtils.createSpicyModal(
                                        i18nContext.get(I18nKeysData.Website.Dashboard.ChannelNoTalkPermissionModal.Title),
                                        true,
                                        {
                                            div {
                                                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.ChannelNoTalkPermissionModal.Description)) {
                                                    p {
                                                        text(line)
                                                    }
                                                }
                                            }
                                        },
                                        EmbeddedSpicyModalUtils.modalButtonListOnlyCloseModalButton(i18nContext)
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