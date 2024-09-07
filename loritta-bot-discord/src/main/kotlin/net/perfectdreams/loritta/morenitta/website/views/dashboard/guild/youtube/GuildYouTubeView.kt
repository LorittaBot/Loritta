package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.youtube

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedYouTubeAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EmptySection.emptySection
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.youtube.YouTubeChannel
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.tsukiScript
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.website.views.htmxGetAsHref
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import org.jetbrains.exposed.sql.ResultRow

class GuildYouTubeView(
    loritta: LorittaWebsite,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans,
    colorTheme: ColorTheme,
    guild: Guild,
    private val serverPremiumPlan: ServerPremiumPlans,
    private val trackedYouTubeAccounts: List<ResultRow>,
    private val youtubeChannels: List<YouTubeChannel>
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
    "youtube"
) {
    companion object {
        fun FlowContent.createYouTubeAccountCards(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            guild: Guild,
            serverPremiumPlan: ServerPremiumPlans,
            trackedYouTubeAccounts: List<ResultRow>,
            youtubeChannels: List<YouTubeChannel>
        ) {
            div(classes = "cards-with-header") {
                div(classes = "card-header") {
                    div(classes = "card-header-info") {
                        div(classes = "card-header-title") {
                            text("Canais que você está seguindo")
                        }

                        div(classes = "card-header-description") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Youtube.Channels(trackedYouTubeAccounts.size)))
                        }
                    }

                    button(classes = "discord-button primary") {
                        if (trackedYouTubeAccounts.size >= ServerPremiumPlans.Complete.maxYouTubeChannels) {
                            // Okay we are on the limit!
                            disabled = true
                        } else if (trackedYouTubeAccounts.size >= serverPremiumPlan.maxYouTubeChannels) {
                            openEmbeddedModalOnClick(
                                "Você encontrou uma função premium!",
                                true,
                                {
                                    div {
                                        p {
                                            text("Faça upgrade para poder adicionar mais canais!")
                                        }

                                        /* p {
                                            text("Você encontrou uma função premium minha! Legal, né?")
                                        }
                                        p {
                                            text("Para ter esta função e muito mais, veja a minha lista de vantagens que você pode ganhar doando!")
                                        } */
                                    }
                                },
                                EmbeddedSpicyModalUtils.modalButtonListOnlyCloseModalButton(i18nContext)
                            )
                        } else {
                            openEmbeddedModalOnClick(
                                "Adicionar Canal",
                                true,
                                {
                                    div {
                                        input(InputType.text) {
                                            name = "channelLink"
                                            placeholder = "https://www.youtube.com/@Loritta"
                                        }
                                    }
                                },
                                listOf(
                                    {
                                        this.defaultModalCloseButton(i18nContext)
                                    },
                                    {
                                        attributes["hx-get"] =
                                            "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/youtube/add"
                                        attributes["hx-push-url"] = "true"
                                        attributes["hx-disabled-elt"] = "this"
                                        attributes["hx-include"] = "[name='channelLink']"
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
                        }

                        text("Adicionar Canal")
                    }
                }

                div(classes = "cards") {
                    if (trackedYouTubeAccounts.isNotEmpty()) {
                        // TODO: Add sorting
                        val sortedTrackedTwitchAccounts = trackedYouTubeAccounts

                        for (trackedTwitchAccount in sortedTrackedTwitchAccounts) {
                            val channelInfo = youtubeChannels.firstOrNull { it.channelId == trackedTwitchAccount[TrackedYouTubeAccounts.youTubeChannelId] }

                            div(classes = "card card-with-avatar-content-buttons") {
                                img(src = channelInfo?.avatarUrl) {
                                    style = "width: 64px; height: 64px; border-radius: 100%;"
                                }

                                div {
                                    style = "flex-grow: 1; display: flex; flex-direction: column; word-break: break-word;"

                                    div {
                                        if (channelInfo != null) {
                                            text(channelInfo.name)
                                        } else {
                                            text("???")
                                        }
                                        text(" ")
                                        text("(")
                                        text(trackedTwitchAccount[TrackedYouTubeAccounts.youTubeChannelId])
                                        text(")")
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
                                                        attributes["hx-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/youtube/tracks/${trackedTwitchAccount[TrackedYouTubeAccounts.id].value}"
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
                                        a(classes = "discord-button primary", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/youtube/tracks/${trackedTwitchAccount[TrackedYouTubeAccounts.id].value}") {
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
    }

    override fun DIV.generateRightSidebarContents() {
        div {
            div {
                id = "form-stuff-wrapper"

                div(classes = "hero-wrapper") {
                    div(classes = "hero-text") {
                        h1 {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Youtube.Title))
                        }

                        p {
                            text("Anuncie para seus membros quando você posta um novo vídeo no YouTube! Assim, seus fãs não irão perder seus novos vídeos.")
                        }
                    }
                }

                hr {}

                div {
                    id = "tracked-twitch-accounts-wrapper"

                    createYouTubeAccountCards(lorittaWebsite.loritta, i18nContext, guild, serverPremiumPlan, trackedYouTubeAccounts, youtubeChannels)
                }
            }
        }
    }
}