package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.common.utils.placeholders.TwitchStreamOnlineMessagePlaceholders
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardDiscordMessageEditor.lorittaDiscordMessageEditor
import net.perfectdreams.loritta.morenitta.website.components.DashboardSaveBar.lorittaSaveBar
import net.perfectdreams.loritta.morenitta.website.components.DiscordChannelSelectMenu.discordChannelSelectMenu
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.website.views.htmxGetAsHref
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.switchtwitch.data.TwitchUser

class GuildConfigureTwitchChannelView(
    loritta: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    selectedType: String,
    private val trackId: Long?,
    private val createPremiumTrack: Boolean,
    private val twitchUser: TwitchUser,
    private val accountTrackState: TwitchAccountTrackState
) : GuildDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    colorTheme,
    guild,
    selectedType
) {
    companion object {
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
    }

    override fun DIV.generateRightSidebarContents() {
        val serializableGuild = WebsiteUtils.convertJDAGuildToSerializable(guild)
        val serializableSelfLorittaUser = WebsiteUtils.convertJDAUserToSerializable(guild.selfMember.user)

        div {
            a(classes = "discord-button no-background-theme-dependent-dark-text", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch") {
                htmxGetAsHref()
                attributes["hx-push-url"] = "true"
                attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                attributes["hx-select"] = "#right-sidebar-contents"
                attributes["hx-target"] = "#right-sidebar-contents"

                htmxDiscordLikeLoadingButtonSetup(
                    i18nContext
                ) {
                    this.text("Voltar para a lista de contas da Twitch")
                }
            }

            hr {}

            div(classes = "hero-wrapper") {
                div(classes = "hero-image") {
                    img(src = twitchUser.profileImageUrl) {
                        // Easter Egg - Aspas/Aspaszin "Gira Aspas"
                        if (twitchUser.id == 269503217L)
                            classes += "gira-aspas"

                        style = "border-radius: 100%; width: 300px; height: 300px;"
                    }
                }

                div(classes = "hero-text") {
                    h1 {
                        text(twitchUser.displayName)

                        // Easter Egg - saadhak
                        if (twitchUser.id == 133926538L) {
                            div(classes = "tag primary") {
                                style = "margin-left: 0.5em;"
                                text("Pai")
                            }
                        }

                        // Easter Egg - felps
                        if (twitchUser.id == 30672329L) {
                            div(classes = "tag primary") {
                                style = "margin-left: 0.5em;"
                                text("'-'")
                            }
                        }

                        // Easter Egg - forever
                        if (twitchUser.id == 477552485L) {
                            div(classes = "tag primary") {
                                style = "margin-left: 0.5em;"
                                text("Cadê Forever Mapa?")
                            }
                        }


                        // Easter Egg - cazum8videos
                        if (twitchUser.id == 62772720L) {
                            div(classes = "tag primary") {
                                style = "margin-left: 0.5em;"
                                text("Pudim")
                            }
                        }

                        // Easter Egg - LOUD people
                        if (twitchUser.id in LOUD_TWITCH_IDS) {
                            div(classes = "tag primary") {
                                style = "margin-left: 0.5em;"
                                text("Faz o L")
                            }
                        }

                        // Easter Egg - cellbit
                        if (twitchUser.id == 28579002L) {
                            div(classes = "tag primary") {
                                style = "margin-left: 0.5em;"
                                text("A Saga da Casinha")
                            }
                        }

                        // Easter Egg - MrPowerGamerBR
                        if (twitchUser.id == 903850572L) {
                            div(classes = "tag primary") {
                                style = "margin-left: 0.5em;"
                                text("Criador da Loritta")
                            }
                        }

                        // Easter Egg - lorittamorenitta
                        if (twitchUser.id == 934129273L) {
                            div(classes = "tag primary") {
                                style = "margin-left: 0.5em;"
                                text("Deusa Suprema")
                            }
                        }

                        // Easter Egg - XAROLA
                        if (twitchUser.id == 181743137L) {
                            div(classes = "tag primary") {
                                style = "margin-left: 0.5em;"
                                text("RATINHO")
                            }
                        }
                    }

                    when (accountTrackState) {
                        TwitchAccountTrackState.AUTHORIZED -> {
                            div(classes = "alert alert-success") {
                                text("O canal foi autorizado pelo dono, então você receberá notificações quando o canal entrar ao vivo!")
                            }
                        }
                        TwitchAccountTrackState.ALWAYS_TRACK_USER -> {
                            div(classes = "alert alert-success") {
                                text("O canal não está autorizado, mas ela está na minha lista especial de \"pessoas tão incríveis que não preciso pedir autorização\". Você receberá notificações quando o canal entrar ao vivo.")
                            }
                        }
                        TwitchAccountTrackState.PREMIUM_TRACK_USER -> {
                            div(classes = "alert alert-success") {
                                text("O canal não está autorizado, mas você colocou ele na lista de acompanhamentos premium! Você receberá notificações quando o canal entrar ao vivo.")
                            }
                        }
                        TwitchAccountTrackState.UNAUTHORIZED -> {
                            div(classes = "alert alert-danger") {
                                text("O canal não está autorizado! Você só receberá notificações quando o canal for autorizado na Loritta.")
                                /* HorizontalList {
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

                                                        val listener = object : org.w3c.dom.events.EventListener {
                                                            override fun handleEvent(event: org.w3c.dom.events.Event) {
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
                                                    kotlinx.coroutines.GlobalScope.launch {
                                                        m.globalState.showToast(
                                                            Toast.Type.INFO,
                                                            "Criando acompanhamento premium..."
                                                        )

                                                        m.makeGuildScopedRPCRequestWithGenericHandling<net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse.EnablePremiumTrackForTwitchChannelResponse>(
                                                            guild.id,
                                                            net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest.EnablePremiumTrackForTwitchChannelRequest(
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
                                    } */
                            }
                        }
                    }
                }
            }

            hr {}

            div {
                id = "module-config-wrapper"
                form {
                    id = "module-config"
                    // If the trackId is null, then the save bar should ALWAYS be dirty
                    if (trackId != null) {
                        attributes["loritta-synchronize-with-save-bar"] = "#save-bar"
                    }

                    // TODO: We technically don't need this here if the trackId is != null
                    hiddenInput {
                        name = "twitchUserId"
                        value = twitchUser.id.toString()
                    }

                    hiddenInput {
                        name = "createPremiumTrack"
                        value = createPremiumTrack.toString()
                    }

                    div(classes = "field-wrappers") {
                        div(classes = "field-wrapper") {
                            div(classes = "field-title") {
                                label {
                                    text("Canal onde será enviado as mensagens")
                                }
                            }

                            div {
                                style = "width: 100%;"

                                discordChannelSelectMenu(
                                    lorittaWebsite,
                                    i18nContext,
                                    "channelId",
                                    guild.channels.filterIsInstance<GuildMessageChannel>(),
                                    null,
                                    null
                                )
                            }
                        }

                        lorittaDiscordMessageEditor(
                            i18nContext,
                            "message",
                            listOf(),
                            PlaceholderSectionType.TWITCH_STREAM_ONLINE_MESSAGE,
                            TwitchStreamOnlineMessagePlaceholders.placeholders.flatMap { placeholder ->
                                when (placeholder) {
                                    TwitchStreamOnlineMessagePlaceholders.GuildIconUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        guild.iconUrl ?: "???"
                                    ) // TODO: Provide a proper fallback
                                    TwitchStreamOnlineMessagePlaceholders.GuildNamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        guild.name
                                    )

                                    TwitchStreamOnlineMessagePlaceholders.GuildSizePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        guild.memberCount.toString()
                                    )

                                    TwitchStreamOnlineMessagePlaceholders.StreamGamePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        "Just Chatting"
                                    )

                                    TwitchStreamOnlineMessagePlaceholders.StreamTitlePlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        "Configurando a Loritta!"
                                    )

                                    TwitchStreamOnlineMessagePlaceholders.StreamUrlPlaceholder -> DashboardDiscordMessageEditor.createMessageEditorPlaceholders(
                                        placeholder,
                                        "https://twitch.tv/${twitchUser.login}"
                                    )
                                }
                            },
                            serializableGuild,
                            serializableSelfLorittaUser,
                            TestMessageTargetChannelQuery.QuerySelector("[name='channelId']"),
                            "{stream.url}"
                        )
                    }
                }
            }

            hr {}

            lorittaSaveBar(
                i18nContext,
                trackId == null,
                {}
            ) {
                if (trackId != null) {
                    attributes["hx-patch"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/tracks/$trackId"
                } else {
                    attributes["hx-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/tracks"
                }
            }
        }
    }

    data class BlueskyTrackSettings(
        val channelId: Long?,
        val message: String
    )
}