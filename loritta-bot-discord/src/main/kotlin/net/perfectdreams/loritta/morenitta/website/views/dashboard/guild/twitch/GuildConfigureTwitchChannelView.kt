package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch

import kotlinx.html.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
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
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.encodeURIComponent
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.tsukiScript
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.website.views.htmxGetAsHref
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.serializable.messageeditor.TestMessageTargetChannelQuery
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.switchtwitch.data.TwitchUser
import kotlin.random.Random

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
    private val accountTrackState: TwitchAccountTrackState,
    private val trackSettings: TwitchTrackSettings,
    private val serverPremiumPlan: ServerPremiumPlans,
    private val premiumTracksCount: Long
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
                    this.text("Voltar para a lista de canais da Twitch")
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
                                div(classes = "qm horizontal-list") {
                                    button(classes = "discord-button primary") {
                                        type = ButtonType.button
                                        openEmbeddedModalOnClick(
                                            "Autorizar Conta na Twitch",
                                            true,
                                            {
                                                div {
                                                    attributes["spicy-twitch-user-id"] = twitchUser.id.toString()
                                                    attributes["spicy-incorrect-twitch-channel"] = encodeURIComponent(
                                                        Json.encodeToString<EmbeddedSpicyModal>(
                                                            EmbeddedSpicyModalUtils.createSpicyModal(
                                                                "Canal Incorreto",
                                                                true,
                                                                {
                                                                    div {
                                                                        text("O canal que você está configurando não é o mesmo canal que você autorizou! Verifique se você está conectado na conta correta na Twitch!")
                                                                    }
                                                                },
                                                                EmbeddedSpicyModalUtils.modalButtonListOnlyCloseModalButton(i18nContext)
                                                            )
                                                        )
                                                    )

                                                    text("Siga as instruções para autorizar a sua conta")

                                                    // Hacky!
                                                    // This is the same code as the "appendEntry" stuff
                                                    form {
                                                        id = "switch-twitch-redirect"
                                                        attributes["hx-select"] = "#right-sidebar-contents"
                                                        attributes["hx-target"] = "#right-sidebar-contents"
                                                        attributes["hx-indicator"] = "#right-sidebar-wrapper"
                                                        attributes["hx-push-url"] = "true"
                                                        // Fix bug when a user is rapidly clicking on multiple entries while they are loading, causing a htmx:swapError
                                                        // Example: Click on entry1, then before it finishes loading, click on entry2, htmx will crash!
                                                        // We use "replace" because we always want to honor the LAST click made by the user
                                                        // attributes["hx-sync"] = "#left-sidebar:replace"
                                                        // show:top - Scroll to the top
                                                        // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                                        // swap:0ms - We don't want the swap animation because it is a full page swap
                                                        attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                                                    }

                                                    // language=JavaScript
                                                    tsukiScript(
                                                        code = """
                                                                    window.open(`https://id.twitch.tv/oauth2/authorize?client_id=${lorittaWebsite.loritta.config.loritta.twitch.clientId}&redirect_uri=${lorittaWebsite.loritta.config.loritta.twitch.redirectUri}&response_type=code`);
                                                                        
                                                                    const listener = (event) => {
                                                                        const userId = event.data;
                                                                        
                                                                        // Switch / Twitch!
                                                                        console.log("User ID:", userId);
                                                                        
                                                                        if (userId === self.getAttribute("spicy-twitch-user-id")) { // oooh, hacky!
                                                                            // First let's attempt a redirect with htmx!
                                                                            // This is ofc a bit hacky, but hey, it works!
                                                                            htmx.ajax(
                                                                                "GET",
                                                                                window.location.href,
                                                                                {
                                                                                    source: selectFirst("#switch-twitch-redirect")
                                                                                }
                                                                            );
                                                                                
                                                                            // Then let's close the open modal
                                                                            htmx.trigger(document, "closeSpicyModal", {})     
                                                                        } else {
                                                                            window['spicy-morenitta'].openEmbeddedModal(self, 'spicy-incorrect-twitch-channel')
                                                                        }
                                                                    };
                                                                        
                                                                    self.whenRemovedFromDOM(() => {
                                                                        console.log("whenRemovedFromDOM!!!")
                                                                        window.removeEventListener("message", listener, false);
                                                                    })
                                                                        
                                                                    window.addEventListener("message", listener, false);
                                                                """.trimIndent()
                                                    )
                                                }
                                            },
                                            EmbeddedSpicyModalUtils.modalButtonListOnlyCloseModalButton(i18nContext)
                                        )

                                        text("Autorizar Canal")
                                    }

                                    button(classes = "discord-button primary") {
                                        type = ButtonType.button
                                        if (serverPremiumPlan.maxUnauthorizedTwitchChannels > premiumTracksCount) {
                                            attributes["hx-put"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/premium-tracks"
                                            attributes["hx-select"] = "#right-sidebar-contents"
                                            attributes["hx-target"] = "#right-sidebar-contents"
                                            attributes["hx-disabled-elt"] = "this"
                                            // show:top - Scroll to the top
                                            // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                            // swap:0ms - We don't want the swap animation because it is a full page swap
                                            attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                                            attributes["hx-select"] = "#right-sidebar-contents"
                                            attributes["hx-target"] = "#right-sidebar-contents"
                                            attributes["hx-vals"] = buildJsonObject {
                                                put("twitchUserId", twitchUser.id.toString())
                                                if (trackId != null)
                                                    put("trackId", trackId.toString())
                                            }.toString()
                                        } else {
                                            disabled = true
                                        }

                                        htmxDiscordLikeLoadingButtonSetup(
                                            i18nContext,
                                        ) {
                                            text("Seguir com Acompanhamento Premium")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            hr {}

            // Easter Egg - XAROLA
            if (twitchUser.id == 181743137L) {
                xarolaRatinhoVALORANTVoiceChat()
            }

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
                                    trackSettings.channelId,
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
                            trackSettings.message
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

    private fun DIV.xarolaRatinhoVALORANTVoiceChat() {
        div {
            style = "position: fixed;\n" +
                    "  z-index: 1000;\n" +
                    "  left: 1em;\n" +
                    "  top: 65%;\n"
            // language=JavaScript
            tsukiScript(code = """
               window['spicy-morenitta'].playSoundEffect(
                    "xarola-ratinho",
                    () => {
                       self.remove()
                    }
               )
            """.trimIndent())

            div(classes = "xarola-ratinho") {
                div(classes = "xarola-ratinho-icon-wrapper") {
                    img(src = "https://stuff.loritta.website/valorant/yoru-icon.png")

                    div(classes = "xarola-ratinho-bars") {
                        repeat(10) {
                            div(classes = "xarola-ratinho-bar") {
                                style = "animation-delay: ${Random(it).nextDouble(-5.0, 0.0)}s;"
                            }
                        }
                    }
                }

                div(classes = "xarola-ratinho-text") {
                    div(classes = "xarola-ratinho-username") {
                        text("XAROLA")
                    }

                    div {
                        text("em Surgimento no Lado Atacante - Equipe")
                    }
                }
            }
        }
    }

    data class TwitchTrackSettings(
        val channelId: Long?,
        val message: String
    )
}