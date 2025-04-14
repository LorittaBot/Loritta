package net.perfectdreams.loritta.website.backend.utils

import kotlinx.coroutines.runBlocking
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend

object DiscordInviteWrapper {
    fun FlowContent.lorittaSupportServerInvite(LorittaWebsiteBackend: LorittaWebsiteBackend, i18nContext: I18nContext) = discordInvite(
        i18nContext,
        "/v3/assets/img/server-icons/loritta-support-64.gif",
        "Loritta's Support \uD83C\uDF07\uD83C\uDF03",
        {
            imgSrcSetFromEtherealGambi(
                LorittaWebsiteBackend,
                // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                runBlocking { LorittaWebsiteBackend.getOrRetrieveImageInfo("loritta/emotes/lori-sunglasses")!! },
                "png",
                "1.5em"
            ) {
                classes = setOf("inline-emoji")
            }

            +" ${i18nContext.get(I18nKeysData.Website.DiscordInvite.LorittaSupportServerDetails)}"
        },
        "https://discord.gg/loritta"
    )

    fun FlowContent.lorittaCommunityServerInvite(LorittaWebsiteBackend: LorittaWebsiteBackend, i18nContext: I18nContext) = discordInvite(
        i18nContext,
        "/v3/assets/img/server-icons/loritta-community-64.gif",
        "Apartamento da Loritta \uD83C\uDF07\uD83C\uDF03",
        {
            imgSrcSetFromEtherealGambi(
                LorittaWebsiteBackend,
                // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                runBlocking { LorittaWebsiteBackend.getOrRetrieveImageInfo("loritta/emotes/lori-kiss")!! },
                "png",
                "1.5em"
            ) {
                classes = setOf("inline-emoji")
            }

            +" ${i18nContext.get(I18nKeysData.Website.DiscordInvite.LorittaCommunityServerDetails)}"
        },
        "https://discord.gg/loritta"
    )

    fun FlowContent.sparklyPowerServerInvite(LorittaWebsiteBackend: LorittaWebsiteBackend, i18nContext: I18nContext) = discordInvite(
        i18nContext,
        "${LorittaWebsiteBackend.etherealGambiClient.baseUrl}/sparklypower/sparklypower-server-icon-64.gif",
        "SparklyPower \uD83D\uDC8E\uD83C\uDFAE",
        {
            imgSrcSetFromEtherealGambi(
                LorittaWebsiteBackend,
                // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                runBlocking { LorittaWebsiteBackend.getOrRetrieveImageInfo("sparklypower/emotes/pantufa-hangloose")!! },
                "png",
                "1.5em"
            ) {
                classes = setOf("inline-emoji")
                style += "transform: scaleX(-1);"
            }

            +" ${i18nContext.get(I18nKeysData.Website.DiscordInvite.SparklyPowerServerDetails)}"
        },
        "https://discord.gg/sparklypower"
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