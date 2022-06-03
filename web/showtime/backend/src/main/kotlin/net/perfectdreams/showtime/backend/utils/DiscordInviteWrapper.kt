package net.perfectdreams.showtime.backend.utils

import kotlinx.coroutines.runBlocking
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.img
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.showtime.backend.ShowtimeBackend

object DiscordInviteWrapper {
    fun FlowContent.lorittaSupportServerInvite(showtimeBackend: ShowtimeBackend, i18nContext: I18nContext) = discordInvite(
        i18nContext,
        "/v3/assets/img/server-icons/loritta-support-64.gif",
        "Loritta's Support \uD83C\uDF07\uD83C\uDF03",
        {
            imgSrcSetFromEtherealGambi(
                showtimeBackend,
                // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                runBlocking { showtimeBackend.getOrRetrieveImageInfo("loritta/emotes/lori-sunglasses")!! },
                "png",
                "1.5em"
            ) {
                classes = setOf("inline-emoji")
            }

            +" ${i18nContext.get(I18nKeysData.Website.DiscordInvite.LorittaSupportServerDetails)}"
        },
        "https://discord.gg/loritta"
    )

    fun FlowContent.lorittaCommunityServerInvite(showtimeBackend: ShowtimeBackend, i18nContext: I18nContext) = discordInvite(
        i18nContext,
        "/v3/assets/img/server-icons/loritta-community-64.gif",
        "Apartamento da Loritta \uD83C\uDF07\uD83C\uDF03",
        {
            imgSrcSetFromEtherealGambi(
                showtimeBackend,
                // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                runBlocking { showtimeBackend.getOrRetrieveImageInfo("loritta/emotes/lori-kiss")!! },
                "png",
                "1.5em"
            ) {
                classes = setOf("inline-emoji")
            }

            +" ${i18nContext.get(I18nKeysData.Website.DiscordInvite.LorittaCommunityServerDetails)}"
        },
        "https://discord.gg/lori"
    )

    fun FlowContent.discordInvite(i18nContext: I18nContext, icon: String, name: String, description: DIV.() -> (Unit), invite: String) {
        div(classes = "discord-invite-wrapper") {
            div(classes = "discord-invite-title") {
                + i18nContext.get(I18nKeysData.Website.DiscordInvite.Title)
            }

            div(classes = "discord-server-details") {
                div(classes = "discord-server-icon") {
                    img(src = icon) {}
                }

                div(classes = "discord-server-info") {
                    div(classes = "discord-server-name") {
                        +name
                    }
                    div(classes = "discord-server-description") {
                        description()
                    }
                }

                a(classes = "discord-server-button", href = invite, target = "_blank") {
                    +i18nContext.get(I18nKeysData.Website.DiscordInvite.Join)
                }
            }
        }
    }
}