package net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.bluesky

import kotlinx.html.*
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TrackedBlueskyAccounts
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EmptySection.emptySection
import net.perfectdreams.loritta.morenitta.website.routes.dashboard.configure.bluesky.BlueskyProfile
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.defaultModalCloseButton
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.views.dashboard.guild.GuildDashboardView
import net.perfectdreams.loritta.morenitta.website.views.htmxDiscordLikeLoadingButtonSetup
import net.perfectdreams.loritta.morenitta.website.views.htmxGetAsHref
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import org.jetbrains.exposed.sql.ResultRow

class GuildBlueskyView(
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
    private val trackedBlueskyAccounts: List<ResultRow>,
    private val blueskyProfiles: List<BlueskyProfile>
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
        val MAX_TRACKED_BLUESKY_ACCOUNTS = 25

        fun FlowContent.createBlueskyAccountCards(
            i18nContext: I18nContext,
            guild: Guild,
            trackedBlueskyAccounts: List<ResultRow>,
            blueskyProfiles: List<BlueskyProfile>
        ) {
            div(classes = "cards-with-header") {
                div(classes = "card-header") {
                    div(classes = "card-header-info") {
                        div(classes = "card-header-title") {
                            text("Contas que você está seguindo")
                        }

                        div(classes = "card-header-description") {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Bluesky.Accounts(trackedBlueskyAccounts.size)))
                        }
                    }

                    button(classes = "discord-button primary") {
                        if (MAX_TRACKED_BLUESKY_ACCOUNTS > trackedBlueskyAccounts.size) {
                            openEmbeddedModalOnClick(
                                "Adicionar Conta",
                                true,
                                {
                                    div {
                                        input(InputType.text) {
                                            name = "handle"
                                            placeholder = "@loritta.website"
                                        }
                                    }
                                },
                                listOf(
                                    {
                                        this.defaultModalCloseButton(i18nContext)
                                    },
                                    {
                                        attributes["hx-get"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/bluesky/add"
                                        attributes["hx-push-url"] = "true"
                                        attributes["hx-disabled-elt"] = "this"
                                        attributes["hx-include"] = "[name='handle']"
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

                                        script {
                                            unsafe {
                                                raw(
                                                    """
                                                        var input = selectFirst("[name='handle']")
                                                        var button = me()
                                                        input.on("input", e => {
                                                            if (input.value.trim() === '') {
                                                                button.disabled = true;
                                                            } else {
                                                                button.disabled = false;
                                                            }
                                                        })
                                                    """.trimIndent()
                                                )
                                            }
                                        }
                                    }
                                )
                            )
                        }

                        text("Adicionar Conta")
                    }
                }

                div(classes = "cards") {
                    if (trackedBlueskyAccounts.isNotEmpty()) {
                        val sortedTrackedBlueskyAccounts = trackedBlueskyAccounts.sortedByDescending {
                            it[TrackedBlueskyAccounts.addedAt] ?: it[TrackedBlueskyAccounts.editedAt]
                        }

                        for (trackedBlueskyAccount in sortedTrackedBlueskyAccounts) {
                            val profileInfo = blueskyProfiles.firstOrNull {
                                it.did == trackedBlueskyAccount[TrackedBlueskyAccounts.repo]
                            }

                            div(classes = "card card-with-avatar-content-buttons") {
                                img(src = profileInfo?.avatar) {
                                    style = "width: 64px; height: 64px; border-radius: 100%;"
                                }

                                div {
                                    style = "flex-grow: 1; display: flex; flex-direction: column; word-break: break-word;"

                                    div {
                                        if (profileInfo != null) {
                                            text(profileInfo.effectiveName)
                                        } else {
                                            text("???")
                                        }
                                        text(" (")
                                        if (profileInfo?.handle != null) {
                                            text("@${profileInfo.handle}")
                                        } else {
                                            text("???")
                                        }
                                        text(" / ")
                                        text(trackedBlueskyAccount[TrackedBlueskyAccounts.repo])
                                        text(")")
                                    }

                                    /* div {
                                        style = "font-size: 0.8em; display: flex; flex-direction: row; gap: 0.25em; align-items: center;"
                                        text("???")
                                    } */
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
                                                        attributes["hx-delete"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/bluesky/tracks/${trackedBlueskyAccount[TrackedBlueskyAccounts.id].value}"
                                                        attributes["hx-disabled-elt"] = "this"
                                                        // show:top - Scroll to the top
                                                        // settle:0ms - We don't want the settle animation beccause it is a full page swap
                                                        // swap:0ms - We don't want the swap animation because it is a full page swap
                                                        attributes["hx-swap"] = "innerHTML settle:0ms swap:0ms"
                                                        attributes["hx-target"] = "#tracked-bluesky-accounts-wrapper"

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
                                        a(classes = "discord-button primary", href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guild/${guild.idLong}/configure/bluesky/tracks/${trackedBlueskyAccount[TrackedBlueskyAccounts.id].value}") {
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
                    img(src = "https://stuff.loritta.website/monica-ata-bluetero.jpeg", classes = "hero-image") {}

                    div(classes = "hero-text") {
                        h1 {
                            text(i18nContext.get(I18nKeysData.Website.Dashboard.Bluesky.Title))
                        }

                        p {
                            text("Anuncie para seus membros quando você posta algo no Bluesky! Assim, seus fãs não irão perder as suas opiniões filosóficas.")
                        }

                        div(classes = "alert alert-danger") {
                            div {
                                b {
                                    text("Atenção: Funcionalidade experimental!")
                                }
                            }

                            div {
                                text("As vezes tem uns bugs no painel, como a Loritta não conseguindo carregar o perfil da pessoa e outras instabilidades no Bluesky, mas não se preocupe, a Loritta *tenta* repostar todas as suas opiniões filosoficas instantaneamente!")
                            }
                        }
                    }
                }

                hr {}

                div {
                    id = "tracked-bluesky-accounts-wrapper"

                    createBlueskyAccountCards(i18nContext, guild, trackedBlueskyAccounts, blueskyProfiles)
                }
            }
        }
    }
}