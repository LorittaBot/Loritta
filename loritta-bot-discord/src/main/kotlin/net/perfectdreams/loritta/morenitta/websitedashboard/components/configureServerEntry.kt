package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.ImgLoading
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.link
import kotlinx.html.script
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite.UserPermissionLevel
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.discord.DiscordOAuth2Guild
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.DiscordLoginUserDashboardRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.calculateGuildIconShortName

fun FlowContent.configureServerEntry(
    i18nContext: I18nContext,
    guild: DiscordOAuth2Guild,
    isFavorited: Boolean
) {
    val userPermissionLevel = LorittaDashboardWebServer.getUserPermissionLevel(guild)

    fun FlowContent.configurateServerButton() {
        discordButtonLink(ButtonStyle.PRIMARY, href = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/guilds/${guild.id}/overview") {
            style = "flex-grow: 1"

            attributes["bliss-get"] = "[href]"
            attributes["bliss-swap:200"] = SWAP_EVERYTHING_DASHBOARD
            attributes["bliss-push-url:200"] = "true"
            attributes["bliss-sync"] = "#left-sidebar"

            text(i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.ManageServer))
        }
    }

    div(classes = "discord-invite-wrapper") {
        div(classes = "discord-server-details") {
            div(classes = "discord-server-icon") {
                if (guild.icon != null) {
                    img(src = "https://cdn.discordapp.com/icons/${guild.id}/${guild.icon}") {
                        // Avoid loading all icons when the user opens the dashboard
                        loading = ImgLoading.lazy
                    }
                } else {
                    classes += "use-discord-background"
                    text(calculateGuildIconShortName(guild.name))
                }
            }

            div(classes = "discord-server-info") {
                div(classes = "discord-server-name") {
                    text(guild.name)
                }

                div(classes = "discord-server-description") {
                    text(
                        when (userPermissionLevel) {
                            UserPermissionLevel.OWNER -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Owner)
                            UserPermissionLevel.ADMINISTRATOR -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Administrator)
                            UserPermissionLevel.MANAGER -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Manager)
                            UserPermissionLevel.MEMBER -> i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.Entry.Member)
                        }
                    )
                }
            }

            div(classes = "invite-mobile-buttons") {
                style = "margin-left: auto;"

                if (isFavorited) {
                    unfavoriteGuildButton(i18nContext, guild.id, false)
                } else {
                    favoriteGuildButton(i18nContext, guild.id)
                }
            }

            div(classes = "invite-desktop-buttons") {
                style = "margin-left: auto;"

                div {
                    style = "display: flex; gap: 8px; align-items: center;"

                    if (isFavorited) {
                        unfavoriteGuildButton(i18nContext, guild.id, false)
                    } else {
                        favoriteGuildButton(i18nContext, guild.id)
                    }

                    configurateServerButton()
                }
            }
        }

        div(classes = "invite-mobile-buttons") {
            configurateServerButton()
        }
    }
}

fun FlowContent.favoriteGuildButton(i18nContext: I18nContext, guildId: Long) {
    button(classes = "favorite-guild-for-user-list-button") {

        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/favorite"
        attributes["bliss-vals-json"] = buildJsonObject {
            put("guildId", guildId)
        }.toString()
        attributes["bliss-swap:200"] = "body (innerHTML) -> this (outerHTML)"
        attributes["bliss-indicator"] = "this"

        svgIcon(SVGIcons.StarOutline)
    }
}

fun FlowContent.unfavoriteGuildButton(i18nContext: I18nContext, guildId: Long, bounceIcon: Boolean) {
    button(classes = "favorite-guild-for-user-list-button") {
        if (bounceIcon)
            classes += "guild-favorited"

        attributes["bliss-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/unfavorite"
        attributes["bliss-vals-json"] = buildJsonObject {
            put("guildId", guildId)
        }.toString()
        attributes["bliss-swap:200"] = "body (innerHTML) -> this (outerHTML)"
        attributes["bliss-indicator"] = "this"

        svgIcon(SVGIcons.Star)
    }
}