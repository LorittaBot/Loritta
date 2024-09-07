package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.twitch

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EmptySection.emptySection
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.tsukiScript
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.website.views.htmxGetAsHref
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.config.GuildTwitchConfig
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession

class GuildTwitchView(
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
    private val twitchConfig: GuildTwitchConfig
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
        fun FlowContent.createTwitchAccountCards(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            guild: Guild,
            trackedTwitchAccounts: List<GuildTwitchConfig.TrackedTwitchAccountWithTwitchUserAndTrackingState>
        ) {
            div(classes = "cards-with-header") {
                div(classes = "card-header") {
                    div(classes = "card-header-info") {
                        div(classes = "card-header-title") {
                            text("Canais que você está seguindo")
                        }

                        div(classes = "card-header-description") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Twitch.Channels(trackedTwitchAccounts.size)))
                        }
                    }

                    button(classes = "discord-button primary") {
                        openEmbeddedModalOnClick(
                            "Qual canal você deseja adicionar?",
                            true,
                            {
                                div(classes = "qm vertical-list") {
                                    button(classes = "discord-button primary") {
                                        openEmbeddedModalOnClick(
                                            "Autorizar Conta na Twitch",
                                            true,
                                            {
                                                div {
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
                                                                    window.open(`https://id.twitch.tv/oauth2/authorize?client_id=${loritta.config.loritta.twitch.clientId}&redirect_uri=${loritta.config.loritta.twitch.redirectUri}&response_type=code`);
                                                                        
                                                                    const listener = (event) => {
                                                                        const userId = event.data;
                                                                        
                                                                        // Switch / Twitch!
                                                                        console.log("User ID:", userId);
                                                                        
                                                                        // First let's attempt a redirect with htmx!
                                                                        // This is ofc a bit hacky, but hey, it works!
                                                                        htmx.ajax(
                                                                            "GET",
                                                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/add",
                                                                            {
                                                                                source: selectFirst("#switch-twitch-redirect"),
                                                                                values: {
                                                                                    twitchUserId: userId,
                                                                                    createPremiumTrack: false
                                                                                }
                                                                            }
                                                                        );
                                                                            
                                                                        // Then let's close the open modal
                                                                        htmx.trigger(document, "closeSpicyModal", {})                                                                        
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

                                        text("Quero adicionar o meu canal")
                                    }

                                    button(classes = "discord-button primary") {
                                        openEmbeddedModalOnClick(
                                            "Adicionar canal de outra pessoa",
                                            true,
                                            {
                                                div {
                                                    input(InputType.text) {
                                                        name = "login"
                                                        placeholder = "lorittamorenitta"
                                                    }
                                                }
                                            },
                                            listOf(
                                                {
                                                    this.defaultModalCloseButton(i18nContext)
                                                },
                                                {
                                                    attributes["hx-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/add-login"
                                                    attributes["hx-push-url"] = "true"
                                                    attributes["hx-disabled-elt"] = "this"
                                                    attributes["hx-include"] = "[name='login']"
                                                    // show:top - Scroll to the top
                                                    // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                                    // swap:0ms - We don't want the swap animation because it is a full page swap
                                                    attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                                                    attributes["hx-select"] = "#right-sidebar-contents"
                                                    attributes["hx-target"] = "#right-sidebar-contents"
                                                    disabled = true
                                                    this.classes += "discord-button primary"

                                                    htmxDiscordLikeLoadingButtonSetup(
                                                        i18nContext,
                                                    ) {
                                                        this.text("Continuar")
                                                    }

                                                    //language=JavaScript
                                                    tsukiScript(code = """
                                                        var input = selectFirst("[name='channelLink']")
                                                        var button = self
                                                        input.on("input", e => {
                                                            button.disabled = input.handle.value.trim() === '';
                                                        })
                                                     """.trimIndent())
                                                }
                                            )
                                        )

                                        text("Quero adicionar o canal de outra pessoa")
                                    }
                                }
                            },
                            listOf {
                                this.defaultModalCloseButton(i18nContext)
                            }
                        )

                        text("Adicionar Canal")
                    }
                }

                div(classes = "cards") {
                    if (trackedTwitchAccounts.isNotEmpty()) {
                        // TODO: Add sorting
                        val sortedTrackedTwitchAccounts = trackedTwitchAccounts

                        for (trackedTwitchAccount in sortedTrackedTwitchAccounts) {
                            div(classes = "card card-with-avatar-content-buttons") {
                                img(src = trackedTwitchAccount.twitchUser?.profileImageUrl) {
                                    style = "width: 64px; height: 64px; border-radius: 100%;"
                                }

                                div {
                                    style = "flex-grow: 1; display: flex; flex-direction: column; word-break: break-word;"

                                    div {
                                        text("${trackedTwitchAccount.twitchUser?.displayName} (${trackedTwitchAccount.twitchUser?.login})")
                                    }

                                    div {
                                        style = "font-size: 0.8em; display: flex; flex-direction: row; gap: 0.25em; align-items: center;"

                                        when (trackedTwitchAccount.trackingState) {
                                            TwitchAccountTrackState.AUTHORIZED -> {
                                                div(classes = "state-tip success") {
                                                    i(classes = "fa-solid fa-check")
                                                }
                                                text("Notificações ativadas — Canal autorizado pelo dono")
                                            }
                                            TwitchAccountTrackState.ALWAYS_TRACK_USER -> {
                                                div(classes = "state-tip success") {
                                                    i(classes = "fa-solid fa-check")
                                                }
                                                text("Notificações ativadas — Canal famoso")
                                            }
                                            TwitchAccountTrackState.PREMIUM_TRACK_USER -> {
                                                div(classes = "state-tip success") {
                                                    i(classes = "fa-solid fa-check")
                                                }
                                                text("Notificações ativadas — Canal usando Acompanhamento Premium")
                                            }
                                            TwitchAccountTrackState.UNAUTHORIZED -> {
                                                div(classes = "state-tip warn") {
                                                    i(classes = "fa-solid fa-xmark")
                                                }
                                                text("Notificações desativadas — Canal não autorizado")
                                            }
                                        }
                                    }
                                }

                                div {
                                    style = "display: flex; gap: 0.5em;"
                                    div {
                                        button(classes = "discord-button danger") {
                                            openEmbeddedModalOnClick(
                                                "Você tem certeza?",
                                                true,
                                                {
                                                    div {
                                                        text("Você quer deletar meeeesmo?")
                                                    }
                                                },
                                                listOf(
                                                    {
                                                        this.defaultModalCloseButton(i18nContext)
                                                    },
                                                    {
                                                        attributes["hx-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/tracks/${trackedTwitchAccount.trackedInfo.id}"
                                                        attributes["hx-disabled-elt"] = "this"
                                                        // show:top - Scroll to the top
                                                        // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                                        // swap:0ms - We don't want the swap animation because it is a full page swap
                                                        attributes["hx-swap"] = "innerHTML settle:0ms swap:0ms"
                                                        attributes["hx-target"] = "#tracked-twitch-accounts-wrapper"

                                                        this.classes += "danger"

                                                        htmxDiscordLikeLoadingButtonSetup(
                                                            i18nContext
                                                        ) {
                                                            this.text("Excluir")
                                                        }
                                                    }
                                                )
                                            )
                                            text("Excluir")
                                        }
                                    }
                                    div {
                                        a(classes = "discord-button primary", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/tracks/${trackedTwitchAccount.trackedInfo.id}") {
                                            htmxGetAsHref()
                                            attributes["hx-push-url"] = "true"
                                            attributes["hx-disabled-elt"] = "this"
                                            // show:top - Scroll to the top
                                            // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                            // swap:0ms - We don't want the swap animation because it is a full page swap
                                            attributes["hx-swap"] = "outerHTML show:top settle:0ms swap:0ms"
                                            attributes["hx-select"] = "#right-sidebar-contents"
                                            attributes["hx-target"] = "#right-sidebar-contents"

                                            htmxDiscordLikeLoadingButtonSetup(
                                                i18nContext
                                            ) {
                                                this.text("Editar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        emptySection(i18nContext)
                    }
                }
            }
        }

        fun FlowContent.createPremiumTwitchAccountCards(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            guild: Guild,
            trackedTwitchAccounts: List<GuildTwitchConfig.PremiumTrackTwitchAccountWithTwitchUser>
        ) {
            div(classes = "cards-with-header") {
                div(classes = "card-header") {
                    div(classes = "card-header-info") {
                        div(classes = "card-header-title") {
                            text("Canais com Acompanhamento Premium")
                        }

                        div(classes = "card-header-description") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Twitch.Channels(trackedTwitchAccounts.size)))
                        }
                    }
                }

                div(classes = "cards") {
                    if (trackedTwitchAccounts.isNotEmpty()) {
                        // TODO: Add sorting
                        val sortedTrackedTwitchAccounts = trackedTwitchAccounts

                        for (trackedTwitchAccount in sortedTrackedTwitchAccounts) {
                            div(classes = "card card-with-avatar-content-buttons") {
                                img(src = trackedTwitchAccount.twitchUser?.profileImageUrl) {
                                    style = "width: 64px; height: 64px; border-radius: 100%;"
                                }

                                div {
                                    style =
                                        "flex-grow: 1; display: flex; flex-direction: column; word-break: break-word;"

                                    div {
                                        text("${trackedTwitchAccount.twitchUser?.displayName} (${trackedTwitchAccount.twitchUser?.login})")
                                    }
                                }

                                div {
                                    style = "display: flex; gap: 0.5em;"
                                    div {
                                        button(classes = "discord-button danger") {
                                            openEmbeddedModalOnClick(
                                                "Você tem certeza?",
                                                true,
                                                {
                                                    div {
                                                        text("Você quer deletar meeeesmo?")
                                                    }
                                                },
                                                listOf(
                                                    {
                                                        this.defaultModalCloseButton(i18nContext)
                                                    },
                                                    {
                                                        attributes["hx-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/twitch/premium-tracks/${trackedTwitchAccount.trackedInfo.id}"
                                                        attributes["hx-disabled-elt"] = "this"
                                                        // show:top - Scroll to the top
                                                        // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                                        // swap:0ms - We don't want the swap animation because it is a full page swap
                                                        attributes["hx-swap"] = "innerHTML settle:0ms swap:0ms"
                                                        attributes["hx-target"] = "#premium-tracked-twitch-accounts-wrapper"

                                                        this.classes += "danger"

                                                        htmxDiscordLikeLoadingButtonSetup(
                                                            i18nContext
                                                        ) {
                                                            this.text("Excluir")
                                                        }
                                                    }
                                                )
                                            )
                                            text("Excluir")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        emptySection(i18nContext)
                    }
                }
            }
        }
    }

    override fun DIV.generateRightSidebarContents() {
        div {
            div {
                id = "form-stuff-wrapper"

                div(classes = "hero-wrapper") {
                    div(classes = "hero-text") {
                        h1 {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Twitch.Title))
                        }

                        p {
                            text("Anuncie para seus membros quando você entra ao vivo na Twitch! Assim, seus fãs não irão perder as suas lives.")
                        }
                    }
                }

                hr {}

                div {
                    id = "tracked-twitch-accounts-wrapper"

                    createTwitchAccountCards(lorittaWebsite.loritta, i18nContext, guild, twitchConfig.trackedTwitchAccounts)
                }

                hr {}

                div(classes = "hero-wrapper") {
                    div(classes = "hero-text") {
                        h2 {
                            text("Acompanhamentos Premium")
                        }

                        p {
                            text("Servidores premium podem seguir contas que não foram autorizadas na Loritta. Aqui, você encontrará todas as contas com o recurso de acompanhamento premium ativado!")
                        }
                    }
                }

                div {
                    id = "premium-tracked-twitch-accounts-wrapper"

                    createPremiumTwitchAccountCards(lorittaWebsite.loritta, i18nContext, guild, twitchConfig.premiumTrackTwitchAccounts)
                }
            }
        }
    }
}