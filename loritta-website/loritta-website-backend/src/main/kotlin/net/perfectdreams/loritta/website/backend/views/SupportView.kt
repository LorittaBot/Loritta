package net.perfectdreams.loritta.website.backend.views

import kotlinx.html.*
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.DiscordInviteWrapper.lorittaCommunityServerInvite
import net.perfectdreams.loritta.website.backend.utils.imgSrcSetFromEtherealGambi
import net.perfectdreams.loritta.website.backend.utils.innerContent

class SupportView(
    LorittaWebsiteBackend: LorittaWebsiteBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : NavbarView(
    LorittaWebsiteBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override fun getTitle() = locale["website.support.title"]

    override fun DIV.generateContent() {
        innerContent {
            div(classes = "odd-wrapper") {
                div(classes = "media") {
                    div(classes = "media-figure") {
                        imgSrcSetFromEtherealGambi(
                            LorittaWebsiteBackend,
                            LorittaWebsiteBackend.images.lorittaSupport,
                            "png",
                            "(max-width: 800px) 50vw, 15vw"
                        )
                    }

                    div(classes = "media-body") {
                        div {
                            style = "text-align: left;"

                            div {
                                style = "text-align: center;"
                                h1 {
                                    +locale["website.support.title"]
                                }
                            }

                            for (str in locale.getList("website.support.description")) {
                                p {
                                    +str
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
                            lorittaCommunityServerInvite(LorittaWebsiteBackend, i18nContext)
                        }
                    }
                }
            }
        }
    }
}