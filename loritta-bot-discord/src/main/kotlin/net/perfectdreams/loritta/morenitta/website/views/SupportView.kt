package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.sweetmorenitta.utils.imgSrcSet

class SupportView(
    loritta: LorittaBot,
    i18nContext: I18nContext,
    locale: BaseLocale,
    path: String
) : NavbarView(
    loritta,
    i18nContext,
    locale,
    path
) {
    override fun getTitle() = locale["website.support.title"]

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            div(classes = "media") {
                div(classes = "media-figure") {
                    imgSrcSet(
                        "${versionPrefix}/assets/img/support/",
                        "lori_support.png",
                        "(max-width: 800px) 50vw, 15vw",
                        1168,
                        168,
                        100
                    )
                }

                div(classes = "media-body") {
                    div {
                        style = "text-align: left;"

                        div {
                            style = "text-align: center;"
                            h1 {
                                + locale["website.support.title"]
                            }
                        }

                        for (str in locale.getList("website.support.description")) {
                            p {
                                + str
                            }
                        }
                    }
                }
            }
        }
        div(classes = "even-wrapper wobbly-bg") {
            div(classes = "support-invites-wrapper") {
                div(classes = "support-invite-wrapper") {
                    div(classes = "support-invite-content") {
                        h2 {
                            text(i18nContext.get(I18nKeysData.Website.Support.SupportAndCommunity))
                        }

                        i18nContext.get(I18nKeysData.Website.Support.CommunityDescription).forEach {
                            p {
                                text(it)
                            }
                        }
                    }

                    div(classes = "discord-support-invite-wrapper") {
                        lorittaCommunityServerInvite(i18nContext)
                    }
                }
            }
        }
    }

    fun FlowContent.lorittaCommunityServerInvite(i18nContext: I18nContext) = discordInvite(
        i18nContext,
        "https://stuff.loritta.website/official-server-icons/loritta-community-256.gif",
        "Apartamento da Loritta \uD83C\uDF07\uD83C\uDF03",
        {
            img(src = "https://assets.perfectdreams.media/loritta/emotes/lori-kiss.png") {
                classes = setOf("inline-emoji")
            }

            +" ${i18nContext.get(I18nKeysData.Website.DiscordInvite.LorittaCommunityServerDetails)}"
        },
        "https://discord.gg/loritta"
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