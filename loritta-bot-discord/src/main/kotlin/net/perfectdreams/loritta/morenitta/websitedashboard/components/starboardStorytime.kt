package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.article
import kotlinx.html.b
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.i
import kotlinx.html.img
import kotlinx.html.span
import kotlinx.html.style
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.Channel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.dashboard.renderer.discordMessageAccessories
import net.perfectdreams.loritta.dashboard.renderer.discordMessageAttachments
import net.perfectdreams.loritta.dashboard.renderer.discordMessageBlock
import net.perfectdreams.loritta.dashboard.renderer.discordMessageReaction
import net.perfectdreams.loritta.dashboard.renderer.discordMessageReactions
import net.perfectdreams.loritta.dashboard.renderer.discordMessageSidebar
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.modules.StarboardModule
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons

fun FlowContent.starboardStorytime(
    i18nContext: I18nContext,
    starboardChannel: Channel?,
    requiredStars: Int,
    lorittaUser: User
) {
    val starboardChannelName = starboardChannel?.name ?: i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.Storytime.MemesOfDubiousQualityChannel)

    div {
        i {
            text(i18nContext.get(DashboardI18nKeysData.Starboard.Storytime.MeanwhileInAChannel))
        }
    }

    div(classes = "message-preview-section") {
        div(classes = "message-preview-wrapper") {
            div(classes = "message-preview") {
                div(classes = "discord-style") {
                    discordMessageBlock(
                        "Arth, o ratinho",
                        "https://stuff.loritta.website/dj-arth.png",
                        false,
                        false,
                        SVGIcons.CheckFat.html.toString()
                    ) {
                        discordMessageAccessories {
                            discordMessageAttachments(listOf("https://stuff.loritta.website/commands/terminator_anime.png"))

                            discordMessageReactions {
                                discordMessageReaction {
                                    svgIcon(SVGIcons.Star) {
                                        this.attr("style", "color: orange; width: 1em; height: 1em;")
                                    }

                                    text(requiredStars)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    div {
        i {
            text(i18nContext.get(DashboardI18nKeysData.Starboard.Storytime.TheMessageHasXStars(requiredStars)))
            text(" ")

            handleI18nString(
                i18nContext,
                I18nKeys.Website.Dashboard.Starboard.Storytime.AndNowOnTheChannel,
                appendAsFormattedText(i18nContext, emptyMap()),
            ) {
                when (it) {
                    "channel" -> {
                        TextReplaceControls.ComposableFunctionResult {
                            span(classes = "discord-mention") {
                                text("#")
                                text(starboardChannelName)
                            }
                        }
                    }
                    else -> TextReplaceControls.AppendControlAsIsResult
                }
            }
        }
    }

    div(classes = "message-preview-section") {
        div(classes = "message-preview-wrapper") {
            div(classes = "message-preview") {
                div(classes = "discord-style") {
                    discordMessageBlock(
                        lorittaUser.effectiveName,
                        lorittaUser.effectiveAvatarUrl,
                        lorittaUser.isBot,
                        true,
                        SVGIcons.CheckFat.html.toString()
                    ) {
                        div {
                            svgIcon(SVGIcons.Star) {
                                this.classNames(setOf("discord-inline-emoji"))
                                this.attr("style", "color: orange;")
                            }

                            b {
                                text("$requiredStars")
                            }

                            text(" - ")

                            span(classes = "discord-mention") {
                                text("#${i18nContext.get(DashboardI18nKeysData.Starboard.Storytime.MemesOfDubiousQualityChannel)}")
                            }
                        }
                        div(classes = "discord-message-accessories") {
                            article(classes = "discord-embed") {
                                val color = StarboardModule.calculateStarboardEmbedColor(requiredStars)
                                style = "border-color: rgb(${color.red}, ${color.green}, ${color.blue});"

                                div(classes = "discord-embed-content") {
                                    div(classes = "discord-embed-author") {
                                        img(classes = "discord-embed-icon") {
                                            src = "https://stuff.loritta.website/dj-arth.png"
                                        }

                                        span(classes = "discord-embed-text") {
                                            text("Arth, o ratinho (351760430991147010)")
                                        }
                                    }

                                    div(classes = "discord-embed-description") {
                                        svgIcon(SVGIcons.FolderColored) {
                                            this.classNames(setOf("discord-inline-emoji"))
                                        }
                                        text(" ")
                                        b {
                                            text("Arquivo")
                                        }
                                    }

                                    div(classes = "discord-embed-image") {
                                        img(src = "https://stuff.loritta.website/commands/terminator_anime.png") {
                                            style = "width: 100%;"
                                        }
                                    }
                                }
                            }

                            div(classes = "discord-components") {
                                div(classes = "discord-action-row") {
                                    button(classes = "discord-button secondary") {
                                        div(classes = "text-with-icon-wrapper") {
                                            text(i18nContext.get(I18nKeysData.Modules.Starboard.JumpToMessage))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    div {
        i {
            text(i18nContext.get(DashboardI18nKeysData.Starboard.Storytime.TheMessageIsRecorded))
        }
    }
}