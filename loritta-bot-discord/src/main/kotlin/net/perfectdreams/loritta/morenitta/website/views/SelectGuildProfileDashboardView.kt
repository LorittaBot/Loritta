package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.components.EtherealGambiUtils.etherealGambiImg
import net.perfectdreams.loritta.morenitta.website.components.LoadingSectionComponents.fillContentLoadingSection
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class SelectGuildProfileDashboardView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String,
    legacyBaseLocale: LegacyBaseLocale,
    userIdentification: LorittaJsonWebSession.UserIdentification,
    userPremiumPlan: UserPremiumPlans
) : ProfileDashboardView(
    loritta,
    i18nContext,
    locale,
    path,
    legacyBaseLocale,
    userIdentification,
    userPremiumPlan,
    "main"
) {
    override fun getTitle() = "Painel de Controle"

    override fun DIV.generateRightSidebarContents() {
        p {
            + "Antes de começar, leia as "
            a(href = "/${locale.path}/guidelines") {
                + "diretrizes de comunidades da Loritta"
            }
            + "!"
        }

        div(classes = "htmx-fill-content-loading-section") {
            id = "user-guilds-wrapper"
            attributes["hx-trigger"] = "load"
            attributes["hx-target"] = "#user-guilds"
            attributes["hx-get"] = ""
            attributes["hx-indicator"] = "this"

            div {
                id = "user-guilds"
            }

            fillContentLoadingSection(i18nContext)
        }
    }

    fun userGuilds(userGuilds: List<TemmieDiscordAuth.Guild>, favoritedGuilds: Set<Long>): FlowContent.() -> Unit = {
        if (userGuilds.isEmpty()) {
            div {
                id = "no-server-found"

                h1 {
                    +"¯\\_(ツ)_/¯"
                }
                h2 {
                    +i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.NoServerFound)
                }

                for (line in i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.TryLoggingIn)) {
                    p {
                        +line
                    }
                }
            }
        } else {
            div(classes = "choose-your-server") {
                id = "choose-your-server"

                for (guild in userGuilds.sortedWith(compareByDescending<TemmieDiscordAuth.Guild> { it.id.toLong() in favoritedGuilds }.thenBy { it.name })) {
                    val guildId = guild.id.toLong()
                    val userPermissionLevel = LorittaWebsite.getUserPermissionLevel(guild)

                    div(classes = "discord-invite-wrapper") {
                        div(classes = "discord-server-details") {
                            div(classes = "discord-server-icon") {
                                val icon = guild.icon
                                val iconUrl = if (icon != null) {
                                    "https://cdn.discordapp.com/icons/${guild.id}/${guild.icon}"
                                } else {
                                    "/assets/img/unknown.png"
                                }
                                img(src = iconUrl) {}
                            }

                            div(classes = "discord-server-info") {
                                div(classes = "discord-server-name") {
                                    if (guild.features.contains("VERIFIED")) {
                                        unsafe {
                                            raw("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" aria-label=\"Verificado\" aria-hidden=\"false\" role=\"img\" width=\"16\" height=\"16\" viewBox=\"0 0 16 15.2\" class=\"\" style=\"position: relative;top: 0.1em;color: #29a6fe;\"><path fill=\"currentColor\" fill-rule=\"evenodd\" d=\"m16 7.6c0 .79-1.28 1.38-1.52 2.09s.44 2 0 2.59-1.84.35-2.46.8-.79 1.84-1.54 2.09-1.67-.8-2.47-.8-1.75 1-2.47.8-.92-1.64-1.54-2.09-2-.18-2.46-.8.23-1.84 0-2.59-1.54-1.3-1.54-2.09 1.28-1.38 1.52-2.09-.44-2 0-2.59 1.85-.35 2.48-.8.78-1.84 1.53-2.12 1.67.83 2.47.83 1.75-1 2.47-.8.91 1.64 1.53 2.09 2 .18 2.46.8-.23 1.84 0 2.59 1.54 1.3 1.54 2.09z\"></path><path d=\"M7.4,11.17,4,8.62,5,7.26l2,1.53L10.64,4l1.36,1Z\" fill=\"white\"></path></svg>")
                                        }
                                        + " "
                                    } else if (guild.features.contains("PARTNERED")) {
                                        unsafe {
                                            raw("<svg aria-label=\"Parceiro(a) do Discord\" class=\"flowerStar-2tNFCR\" aria-hidden=\"false\" role=\"img\" width=\"16\" height=\"16\" viewBox=\"0 0 16 15.2\" style=\"position: relative;top: 0.1em;color: #29a6fe;\"><path fill=\"currentColor\" fill-rule=\"evenodd\" d=\"m16 7.6c0 .79-1.28 1.38-1.52 2.09s.44 2 0 2.59-1.84.35-2.46.8-.79 1.84-1.54 2.09-1.67-.8-2.47-.8-1.75 1-2.47.8-.92-1.64-1.54-2.09-2-.18-2.46-.8.23-1.84 0-2.59-1.54-1.3-1.54-2.09 1.28-1.38 1.52-2.09-.44-2 0-2.59 1.85-.35 2.48-.8.78-1.84 1.53-2.12 1.67.83 2.47.83 1.75-1 2.47-.8.91 1.64 1.53 2.09 2 .18 2.46.8-.23 1.84 0 2.59 1.54 1.3 1.54 2.09z\"></path><path d=\"M10.5906 6.39993L9.19223 7.29993C8.99246 7.39993 8.89258 7.39993 8.69281 7.29993C8.59293 7.19993 8.39317 7.09993 8.29328 6.99993C7.89375 6.89993 7.5941 6.99993 7.29445 7.19993L6.79504 7.49993L4.29797 9.19993C3.69867 9.49993 2.99949 9.39993 2.69984 8.79993C2.30031 8.29993 2.50008 7.59993 2.99949 7.19993L5.99598 5.19993C6.79504 4.69993 7.79387 4.49993 8.69281 4.69993C9.49188 4.89993 10.0912 5.29993 10.5906 5.89993C10.7904 6.09993 10.6905 6.29993 10.5906 6.39993Z\" fill=\"white\"></path><path d=\"M13.4871 7.79985C13.4871 8.19985 13.2874 8.59985 12.9877 8.79985L9.89135 10.7999C9.29206 11.1999 8.69276 11.3999 7.99358 11.3999C7.69393 11.3999 7.49417 11.3999 7.19452 11.2999C6.39545 11.0999 5.79616 10.6999 5.29674 10.0999C5.19686 9.89985 5.29674 9.69985 5.39663 9.59985L6.79499 8.69985C6.89487 8.59985 7.09463 8.59985 7.19452 8.69985C7.39428 8.79985 7.59405 8.89985 7.69393 8.99985C8.09346 8.99985 8.39311 8.99985 8.69276 8.79985L9.39194 8.39985L11.3896 6.99985L11.6892 6.79985C12.1887 6.49985 12.9877 6.59985 13.2874 7.09985C13.4871 7.39985 13.4871 7.59985 13.4871 7.79985Z\" fill=\"white\"></path></svg>")
                                        }
                                        + " "
                                    }
                                    +guild.name
                                }
                                div(classes = "discord-server-description") {
                                    + when (userPermissionLevel) {
                                        LorittaWebsite.UserPermissionLevel.OWNER -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Owner)
                                        LorittaWebsite.UserPermissionLevel.ADMINISTRATOR -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Administrator)
                                        LorittaWebsite.UserPermissionLevel.MANAGER -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Manager)
                                        LorittaWebsite.UserPermissionLevel.MEMBER -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Member)
                                    }
                                }
                            }

                            div {
                                style = "margin-left: auto;"

                                div {
                                    style = "display: flex; gap: 8px; align-items: center;"

                                    favoriteGuild(i18nContext, guildId, guildId in favoritedGuilds)

                                    a(href = "/${locale.path}/guild/${guild.id}/configure") {
                                        button(classes = "discord-button primary") {
                                            type = ButtonType.button
                                            text(i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.ManageServer))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        hr {}

        div {
            style = "display: flex; justify-content: center;"

            etherealGambiImg(src = "https://stuff.loritta.website/loritta-deitada-gabi.png", sizes = "(max-width: 600px) 100vw, 600px") {
                style = "max-width: 600px; width: 100%;"
            }
        }
    }

    companion object {
        fun FlowContent.favoriteGuild(i18nContext: I18nContext, guildId: Long, alreadyFavorited: Boolean) {
            button(classes = "favorite-guild-for-user-list-button") {
                classes += if (alreadyFavorited) {
                    "guild-already-favorited"
                } else {
                    "guild-not-favorited"
                }

                attributes["hx-indicator"] = "this"
                attributes["hx-swap"] = "outerHTML settle:100ms"
                attributes["hx-target"] = "this"
                attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/favorite-guild"
                attributes["hx-vals"] = buildJsonObject {
                    put("guildId", guildId.toString())
                    put("favorited", !alreadyFavorited)
                }.toString()

                if (alreadyFavorited) {
                    attributes["aria-label"] = i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.RemoveServerFromFavorites)
                    i(classes = "fa-solid fa-star")
                } else {
                    attributes["aria-label"] = i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.AddServerToFavorites)
                    i(classes = "fa-regular fa-star")
                }
            }
        }
    }
}