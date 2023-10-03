package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.twitch

import androidx.compose.runtime.*
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrappers
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalSpicyInfo
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Toast
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.AddNewGuildTwitchChannelViewModel
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.placeholders.TwitchStreamOnlineMessagePlaceholders
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.TwitchUser
import net.perfectdreams.loritta.serializable.config.TrackedTwitchAccount
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import kotlin.random.Random

private val LOUD_TWITCH_IDS = setOf(
    569325723L, // loud_coringa,
    569327531L, // loud_thurzin
    572866502L, // loud_voltan
    569320237L, // loud_babi
    490164805L, // loud_lzinn
    123998916L, // loud_mii
    161888550L, // loud_thaiga
    660006987L, // loud_jordan
    108544855L, // loud_caiox
    660003898L, // loud_dacruz
    569324171L, // loud_mob
    569316091L, // loud_crusher
)

@Composable
fun TwitchChannelEditor(
    m: LorittaDashboardFrontend,
    i18nContext: I18nContext,
    guild: DiscordGuild,
    userInfo: GetUserIdentificationResponse,
    selfUser: DiscordUser,
    twitchUser: TwitchUser,
    trackedTwitchAccount: TrackedTwitchAccount,
    accountTrackState: TwitchAccountTrackState,
    createPremiumTrack: Boolean,
    plan: ServerPremiumPlans,
    premiumTracksCount: Long,
    alwaysDirty: Boolean,
    reloadData: () -> (Unit),
    postSave: (Boolean, Long) -> (Unit)
) {
    val spicyInfo = LocalSpicyInfo.current
    val isNewTrackedAccount = trackedTwitchAccount.id == -1L

    var mutableTrackedTwitchAccount by remember { mutableStateOf(AddNewGuildTwitchChannelViewModel.toMutableConfig(trackedTwitchAccount)) }

    // The initial config state
    var startConfigState by remember { mutableStateOf(AddNewGuildTwitchChannelViewModel.toDataConfig(mutableTrackedTwitchAccount)) }

    HeroBanner {
        HeroImage {
            Img(src = twitchUser.profileImageUrl) {
                // Easter Egg - Aspas/Aspaszin "Gira Aspas"
                if (twitchUser.id == 269503217L)
                    classes("gira-aspas")

                attr("style", "border-radius: 100%;")
            }
        }

        HeroText {
            H1 {
                Text(twitchUser.displayName)

                // Easter Egg - saadhak
                if (twitchUser.id == 133926538L) {
                    TagPrimary(attrs = {
                        attr("style", "margin-left: 0.5em;")
                    }) {
                        Text("Pai")
                    }
                }

                // Easter Egg - felps
                if (twitchUser.id == 30672329L) {
                    TagPrimary(attrs = {
                        attr("style", "margin-left: 0.5em;")
                    }) {
                        Text("'-'")
                    }
                }

                // Easter Egg - forever
                if (twitchUser.id == 477552485L) {
                    TagPrimary(attrs = {
                        attr("style", "margin-left: 0.5em;")
                    }) {
                        Text("Cadê Forever Mapa?")
                    }
                }


                // Easter Egg - cazum8videos
                if (twitchUser.id == 62772720L) {
                    TagPrimary(attrs = {
                        attr("style", "margin-left: 0.5em;")
                    }) {
                        Text("Pudim")
                    }
                }

                // Easter Egg - LOUD people
                if (twitchUser.id in LOUD_TWITCH_IDS) {
                    TagPrimary(attrs = {
                        attr("style", "margin-left: 0.5em;")
                    }) {
                        Text("Faz o L")
                    }
                }

                // Easter Egg - cellbit
                if (twitchUser.id == 28579002L) {
                    TagPrimary(attrs = {
                        attr("style", "margin-left: 0.5em;")
                    }) {
                        Text("A Saga da Casinha")
                    }
                }

                // Easter Egg - MrPowerGamerBRyay
                if (twitchUser.id == 903850572L) {
                    TagPrimary(attrs = {
                        attr("style", "margin-left: 0.5em;")
                    }) {
                        Text("Criador da Loritta")
                    }
                }

                // Easter Egg - lorittamorenitta
                if (twitchUser.id == 934129273L) {
                    TagPrimary(attrs = {
                        attr("style", "margin-left: 0.5em;")
                    }) {
                        Text("Deusa Suprema")
                    }
                }
            }

            when (accountTrackState) {
                TwitchAccountTrackState.AUTHORIZED -> {
                    Div(attrs = {
                        classes("alert", "alert-success")
                    }) {
                        Text("O canal foi autorizada pelo dono, então você receberá notificações quando o canal entrar ao vivo!")
                    }
                }
                TwitchAccountTrackState.ALWAYS_TRACK_USER -> {
                    Div(attrs = {
                        classes("alert", "alert-success")
                    }) {
                        Text("O canal não está autorizado, mas ela está na minha lista especial de \"pessoas tão incríveis que não preciso pedir autorização\". Você receberá notificações quando o canal entrar ao vivo.")
                    }
                }
                TwitchAccountTrackState.PREMIUM_TRACK_USER -> {
                    Div(attrs = {
                        classes("alert", "alert-success")
                    }) {
                        Text("O canal não está autorizado, mas você colocou ele na lista de acompanhamentos premium! Você receberá notificações quando o canal entrar ao vivo.")
                    }
                }
                TwitchAccountTrackState.UNAUTHORIZED -> {
                    Div(attrs = {
                        classes("alert", "alert-danger")
                    }) {
                        Text("O canal não está autorizado! Você só receberá notificações quando o canal for autorizada na Loritta.")
                        HorizontalList {
                            DiscordButton(
                                DiscordButtonType.PRIMARY,
                                attrs = {
                                    onClick {
                                        m.globalState.openCloseOnlyModal(
                                            "Autorizar Canal na Twitch",
                                            true,
                                        ) { modal ->
                                            // This is a hack!!! We use this to know when the modal has been closed
                                            Span(attrs = {
                                                window.open("https://id.twitch.tv/oauth2/authorize?client_id=${spicyInfo.twitchClientId}&redirect_uri=${spicyInfo.twitchRedirectUri}&response_type=code")

                                                val listener = object : EventListener {
                                                    override fun handleEvent(event: Event) {
                                                        val userId = event.asDynamic().data.toString().toLong()

                                                        // Close the modal...
                                                        modal.close()

                                                        // Check if the user ID matches!
                                                        if (userId == mutableTrackedTwitchAccount.userId) {
                                                            // It matches, so the user has authorized the account! We will reload the current page...
                                                            reloadData.invoke()
                                                        } else {
                                                            // Does NOT match...
                                                            m.globalState.openCloseOnlyModal(
                                                                "Canal Incorreto",
                                                                true
                                                            ) {
                                                                Text("O canal que você está configurando não é o mesmo canal que você autorizou! Verifique se você está conectado na conta correta na Twitch!")
                                                            }
                                                        }
                                                    }
                                                }

                                                window.addEventListener(
                                                    "message",
                                                    listener,
                                                    false
                                                )

                                                ref {
                                                    onDispose {
                                                        window.removeEventListener("message", listener)
                                                    }
                                                }
                                            })

                                            Text("Siga as instruções para autorizar a sua conta")
                                        }
                                    }
                                }
                            ) {
                                Text("Autorizar Canal")
                            }

                            DiscordButton(
                                DiscordButtonType.PRIMARY,
                                attrs = {
                                    if (plan.maxUnauthorizedTwitchChannels > premiumTracksCount) {
                                        onClick {
                                            GlobalScope.launch {
                                                m.globalState.showToast(
                                                    Toast.Type.INFO,
                                                    "Criando acompanhamento premium..."
                                                )

                                                m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.EnablePremiumTrackForTwitchChannelResponse>(
                                                    guild.id,
                                                    DashGuildScopedRequest.EnablePremiumTrackForTwitchChannelRequest(
                                                        trackedTwitchAccount.twitchUserId
                                                    ),
                                                    onSuccess = {
                                                        m.globalState.showToast(
                                                            Toast.Type.SUCCESS,
                                                            "Acompanhamento premium criado!"
                                                        )
                                                        m.soundEffects.configSaved.play(1.0)
                                                        reloadData.invoke()
                                                    },
                                                    onError = {
                                                        m.soundEffects.configError.play(1.0)
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        disabledWithSoundEffect(m)
                                    }
                                }
                            ) {
                                Text("Seguir com Acompanhamento Premium")
                            }
                        }
                    }
                }
            }
        }
    }

    Hr {}

    // Easter Egg - XAROLA
    if (trackedTwitchAccount.twitchUserId == 181743137L) {
        XarolaRatinhoVALORANTVoiceChat(m, trackedTwitchAccount.twitchUserId)
    }

    FieldWrappers {
        FieldWrapper {
            FieldLabel("Canal onde será enviado as mensagens")

            DiscordChannelSelectMenu(
                m,
                i18nContext,
                guild.channels,
                mutableTrackedTwitchAccount.channelId
            ) {
                mutableTrackedTwitchAccount.channelId = it.id
            }
        }

        DiscordMessageEditor(
            m,
            i18nContext,
            null,
            TwitchStreamOnlineMessagePlaceholders,
            {
                when (it) {
                    TwitchStreamOnlineMessagePlaceholders.GuildIconUrlPlaceholder -> guild.getIconUrl(512) ?: "" // TODO: Fix this!
                    TwitchStreamOnlineMessagePlaceholders.GuildNamePlaceholder -> guild.name
                    TwitchStreamOnlineMessagePlaceholders.GuildSizePlaceholder -> "100" // TODO: Fix this!
                    TwitchStreamOnlineMessagePlaceholders.StreamGamePlaceholder -> "Just Chatting"
                    TwitchStreamOnlineMessagePlaceholders.StreamTitlePlaceholder -> "Configurando a Loritta!"
                    TwitchStreamOnlineMessagePlaceholders.StreamUrlPlaceholder -> "https://twitch.tv/${twitchUser.login}"
                }
            },
            DashGuildScopedRequest.SendMessageRequest.AdditionalPlaceholdersInfo.TwitchStreamOnlinePlaceholderInfo(twitchUser.login),
            guild,
            when (mutableTrackedTwitchAccount.channelId) {
                -1L -> TargetChannelResult.ChannelNotSelected
                else -> TargetChannelResult.GuildMessageChannelTarget(mutableTrackedTwitchAccount.channelId)
            },
            userInfo,
            selfUser,
            listOf(),
            listOf(),
            mutableTrackedTwitchAccount.message
        ) {
            mutableTrackedTwitchAccount.message = it
        }
    }

    Hr {}

    var isSaving by remember { mutableStateOf(false) }

    SaveBar(
        m,
        i18nContext,
        alwaysDirty || startConfigState != AddNewGuildTwitchChannelViewModel.toDataConfig(mutableTrackedTwitchAccount),
        isSaving,
        onReset = {
            mutableTrackedTwitchAccount = AddNewGuildTwitchChannelViewModel.toMutableConfig(startConfigState)
        },
        onSave = {
            GlobalScope.launch {
                isSaving = true

                m.globalState.showToast(Toast.Type.INFO, "Salvando configuração...")
                // val config = WelcomerViewModel.toDataConfig(mutableWelcomerConfig)
                m.makeGuildScopedRPCRequestWithGenericHandling<DashGuildScopedResponse.UpsertGuildTwitchChannelResponse>(
                    guild.id,
                    DashGuildScopedRequest.UpsertGuildTwitchChannelRequest(
                        if (isNewTrackedAccount) null else mutableTrackedTwitchAccount.id,
                        mutableTrackedTwitchAccount.userId,
                        mutableTrackedTwitchAccount.channelId,
                        mutableTrackedTwitchAccount.message,
                        createPremiumTrack
                    ),
                    onSuccess = {
                        when (it) {
                            is DashGuildScopedResponse.UpsertGuildTwitchChannelResponse.Success -> {
                                m.globalState.showToast(Toast.Type.SUCCESS, "Configuração salva!")
                                m.soundEffects.configSaved.play(1.0)
                                isSaving = false
                                m.globalState.activeSaveBar = false

                                startConfigState = TrackedTwitchAccount(
                                    mutableTrackedTwitchAccount.id,
                                    mutableTrackedTwitchAccount.userId,
                                    mutableTrackedTwitchAccount.channelId,
                                    mutableTrackedTwitchAccount.message
                                )

                                postSave.invoke(isNewTrackedAccount, it.trackedId)
                            }
                            DashGuildScopedResponse.UpsertGuildTwitchChannelResponse.TooManyPremiumTracks -> {
                                m.globalState.showToast(Toast.Type.WARN, "Você chegou no limite de acompanhamentos premium!")
                                m.soundEffects.configError.play(1.0)
                                isSaving = false
                            }
                        }
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

@Composable
fun XarolaRatinhoVALORANTVoiceChat(m: LorittaDashboardFrontend, twitchUserId: Long) {
    var hasFinishedPlayingAudio by remember { mutableStateOf(false) }

    if (!hasFinishedPlayingAudio) {
        Div(attrs = {
            attr(
                "style", "position: fixed;\n" +
                        "  z-index: 1000;\n" +
                        "  left: 1em;\n" +
                        "  top: 65%;\n"
            )
        }) {
            LaunchedEffect(twitchUserId) {
                m.soundEffects.xarolaRatinho.play(
                    0.1,
                    onEnd = {
                        hasFinishedPlayingAudio = true
                    }
                )
            }

            Div(attrs = {
                classes("xarola-ratinho")
            }) {
                Div(attrs = {
                    classes("xarola-ratinho-icon-wrapper")
                }) {
                    Img(src = "https://stuff.loritta.website/valorant/yoru-icon.png")

                    if (!hasFinishedPlayingAudio) {
                        Div(attrs = {
                            classes("xarola-ratinho-bars")
                        }) {
                            repeat(10) {
                                Div(attrs = {
                                    classes("xarola-ratinho-bar")
                                    attr("style", "animation-delay: ${Random(it).nextDouble(-5.0, 0.0)}s;")
                                })
                            }
                        }
                    }
                }

                Div(attrs = {
                    classes("xarola-ratinho-text")
                }) {
                    Div(attrs = {
                        classes("xarola-ratinho-username")
                    }) {
                        Text("XAROLA")
                    }

                    Div {
                        Text("em Surgimento no Lado Atacante - Equipe")
                    }
                }
            }
        }
    }
}