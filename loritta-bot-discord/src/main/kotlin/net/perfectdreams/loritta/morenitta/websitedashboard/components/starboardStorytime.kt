package net.perfectdreams.loritta.morenitta.websitedashboard.components

import dev.minn.jda.ktx.generics.getChannel
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
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.modules.StarboardModule
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.appendAsFormattedText
import net.perfectdreams.loritta.morenitta.website.components.TextReplaceControls.handleI18nString
import net.perfectdreams.loritta.morenitta.websitedashboard.DashboardI18nKeysData

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
                    div(classes = "discord-message") {
                        div(classes = "discord-message-sidebar") {
                            img(src = "https://stuff.loritta.website/dj-arth.png", classes = "discord-message-avatar")
                        }

                        div(classes = "discord-message-content") {
                            h2(classes = "discord-message-header") {
                                span(classes = "discord-message-username") {
                                    style = "color: rgb(233, 30, 99);"
                                    text("Arth, o ratinho")
                                }

                                span(classes = "discord-message-timestamp") {
                                    text("Today at 09:07")
                                }
                            }

                            div(classes = "discord-message-accessories") {
                                div(classes = "discord-message-attachments") {
                                    img(classes = "discord-message-attachment", src = "https://stuff.loritta.website/commands/terminator_anime.png") {

                                    }
                                }

                                div(classes = "discord-message-reactions") {
                                    div(classes = "discord-message-reaction") {
                                        style = "display: flex; align-items: center; justify-content: center; gap: 0.5em;"

                                        text(requiredStars)
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
                    div(classes = "discord-message") {
                        div(classes = "discord-message-sidebar") {
                            img(src = lorittaUser.effectiveAvatarUrl, classes = "discord-message-avatar")
                        }

                        div(classes = "discord-message-content") {
                            h2(classes = "discord-message-header") {
                                span(classes = "discord-message-username") {
                                    style = "color: rgb(233, 30, 99);"
                                    text("Loritta Morenitta \uD83D\uDE18")
                                }

                                span(classes = "discord-message-bot-tag") {
                                    text("APP")
                                }

                                span(classes = "discord-message-timestamp") {
                                    text("Today at 09:07")
                                }
                            }

                            div {
                                b {
                                    text("$requiredStars")
                                }

                                text(" - ")

                                span(classes = "discord-mention") {
                                    text("#${starboardChannelName}")
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
    }

    div {
        i {
            text(i18nContext.get(DashboardI18nKeysData.Starboard.Storytime.TheMessageIsRecorded))
        }
    }
}