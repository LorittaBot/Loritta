package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.starboard

import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrappers
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ConfigureGuildStarboardScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.DiscordUtils
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Toast
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.StarboardViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.common.utils.Color
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.serializable.config.GuildStarboardConfig
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import org.jetbrains.compose.web.attributes.step
import org.jetbrains.compose.web.dom.*

@Composable
fun GuildStarboard(
    m: LorittaDashboardFrontend,
    screen: ConfigureGuildStarboardScreen,
    i18nContext: I18nContext,
    guildViewModel: GuildViewModel
) {
    val userInfo = LocalUserIdentification.current
    val configViewModel = viewModel { StarboardViewModel(m, it, guildViewModel) }

    println("Guild Info Resource: ${guildViewModel.guildInfoResource::class}")
    println("Config Resource: ${configViewModel.configResource::class}")

    ResourceChecker(
        i18nContext,
        guildViewModel.guildInfoResource,
        configViewModel.configResource
    ) { guild, starboardResponse ->
        val starboardConfig = starboardResponse.starboardConfig ?: GuildStarboardConfig(
            false,
            null,
            1
        )

        var mutableStarboardConfig by remember { mutableStateOf(StarboardViewModel.toMutableConfig(starboardConfig)) }

        // The initial config state
        var startConfigState by remember { mutableStateOf(StarboardViewModel.toDataConfig(mutableStarboardConfig)) }

        HeroBanner {
            HeroImage {
                EtherealGambiImg(src = "https://stuff.loritta.website/loritta-star-hug-yafyr.png", sizes = "350px") {
                    classes("starboard-web-animation")
                }
            }

            HeroText {
                H1 {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.Title))
                }

                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.Description)) {
                    P {
                        Text(line)
                    }
                }
            }
        }

        Hr {}

        DiscordToggle("starboard-enabled", "Ativar módulo?", null, mutableStarboardConfig._enabled)

        Hr {}

        Div(attrs = {
            if (mutableStarboardConfig.enabled) {
                attr("style", "")
            } else {
                attr("style", "filter: blur(4px); pointer-events: none; user-select: none;")
            }
        }) {
            FieldWrappers {
                FieldWrapper {
                    FieldLabel(i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.StarboardChannel))

                    DiscordChannelSelectMenu(
                        m,
                        i18nContext,
                        guild.channels,
                        mutableStarboardConfig.starboardChannelId,
                    ) {
                        mutableStarboardConfig.starboardChannelId = it.id
                    }
                }

                FieldWrapper {
                    FieldLabel(i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.MinimumReactCount))

                    NumberInput(mutableStarboardConfig.requiredStars, min = 1) {
                        step(1)

                        onInput {
                            mutableStarboardConfig.requiredStars = it.value?.toInt() ?: 1
                        }
                    }
                }
            }

            Hr {}

            Div {
            Div {
                I {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.Storytime.MeanwhileInAChannel))
                }

                Div(attrs = { classes("message-preview-section") }) {
                    Div(attrs = { classes("message-preview-wrapper") }) {
                        Div(attrs = { classes("message-preview") }) {
                            DiscordMessageStyle {
                                DiscordMessageBlock(
                                    i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.Storytime.ArthTheRat),
                                    "https://stuff.loritta.website/dj-arth.png",
                                    false
                                ) {
                                    DiscordMessageAccessories {
                                        DiscordMessageAttachments(listOf("https://stuff.loritta.website/commands/terminator_anime.png"))

                                        DiscordMessageReactions {
                                            DiscordMessageReaction {
                                                UIIcon(SVGIconManager.coloredStar) {
                                                    attr("style", "width: 1em; height: 1em;")
                                                }

                                                Text(" ")
                                                Text(mutableStarboardConfig.requiredStars.toString())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val starboardChannel = guild.channels.firstOrNull { it.id == mutableStarboardConfig.starboardChannelId }
                I {
                    Text(
                        i18nContext.get(
                            I18nKeysData.Website.Dashboard.Starboard.Storytime.TheMessageHasXStars(
                                mutableStarboardConfig.requiredStars
                            )
                        )
                    )
                    Text(" ")

                    if (starboardChannel != null) {
                        TextReplaceControls(
                            i18nContext,
                            I18nKeys.Website.Dashboard.Starboard.Storytime.AndNowOnTheChannel,
                            appendAsFormattedText(i18nContext, mapOf()),
                        ) {
                            when (it) {
                                "channel" -> {
                                    ComposableFunctionResult {
                                        InlineDiscordMention("#${starboardChannel.name}")
                                    }
                                }

                                else -> AppendControlAsIsResult
                            }
                        }
                    } else {
                        Text(i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.Storytime.AndNowOnTheNullChannel))
                    }
                }

                Div(attrs = { classes("message-preview-section") }) {
                    Div(attrs = { classes("message-preview-wrapper") }) {
                        Div(attrs = { classes("message-preview") }) {
                            DiscordMessageStyle {
                                DiscordMessageBlock(
                                    starboardResponse.selfUser.globalName ?: starboardResponse.selfUser.name,
                                    DiscordUtils.getUserAvatarUrl(
                                        starboardResponse.selfUser.id,
                                        starboardResponse.selfUser.avatarId
                                    ),
                                    true
                                ) {
                                    Div {
                                        UIIcon(SVGIconManager.coloredStar) {
                                            classes("discord-inline-emoji")
                                        }
                                        Text(" ")
                                        B {
                                            Text("${mutableStarboardConfig.requiredStars}")
                                        }
                                        Text(" - ")
                                        InlineDiscordMention("#" + i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.Storytime.MemesOfDubiousQualityChannel))
                                    }

                                    DiscordMessageAccessories {
                                        DiscordMessageEmbed(Color(255, 255, 135).rgb, null) {
                                            DiscordAuthor(
                                                null,
                                                "https://stuff.loritta.website/dj-arth.png"
                                            ) {
                                                Text("${i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.Storytime.ArthTheRat)} (351760430991147010)")
                                            }

                                            DiscordEmbedDescription {
                                                UIIcon(SVGIconManager.coloredFolder) {
                                                    classes("discord-inline-emoji")
                                                }

                                                Text(" ")

                                                B {
                                                    Text(i18nContext.get(I18nKeysData.Modules.Starboard.Files(1)))
                                                }
                                            }

                                            DiscordEmbedImage("https://stuff.loritta.website/commands/terminator_anime.png")
                                        }

                                        DiscordComponents {
                                            DiscordActionRow {
                                                DiscordLinkButton {
                                                    Text(i18nContext.get(I18nKeysData.Modules.Starboard.JumpToMessage))
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

                I {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.Starboard.Storytime.TheMessageIsRecorded))
                }
            }
        }

        Hr {}

        var isSaving by remember { mutableStateOf(false) }

        SaveBar(
            m,
            i18nContext,
            startConfigState != StarboardViewModel.toDataConfig(mutableStarboardConfig),
            isSaving,
            onReset = {
                mutableStarboardConfig = StarboardViewModel.toMutableConfig(startConfigState)
            },
            onSave = {
                GlobalScope.launch {
                    isSaving = true

                    m.globalState.showToast(Toast.Type.INFO, "Salvando configuração...")
                    val config = StarboardViewModel.toDataConfig(mutableStarboardConfig)
                    val dashResponse =
                        m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.UpdateGuildStarboardConfigResponse>(
                            guild.id,
                            DashGuildScopedRequest.UpdateGuildStarboardConfigRequest(config),
                            onSuccess = {
                                m.globalState.showToast(Toast.Type.SUCCESS, "Configuração salva!")
                                m.soundEffects.configSaved.play(1.0)
                                isSaving = false
                                startConfigState = config
                                m.globalState.activeSaveBar = false
                            },
                            onError = {
                                m.soundEffects.configError.play(1.0)
                                isSaving = false
                                m.globalState.activeSaveBar = false
                            }
                        )
                }
            }
        )
    }
}