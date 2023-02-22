package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils

class CommunityGuidelinesView(
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
    override fun getTitle() = locale["website.guidelines.communityGuidelines"]

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            div(classes = "media") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: center;"

                        div {
                            h1 {
                                + i18nContext.get(I18nKeysData.Website.Guidelines.Intro.Title)
                            }
                        }

                        for (str in i18nContext.get(I18nKeysData.Website.Guidelines.Intro.Description)) {
                            p {
                                + str
                            }
                        }
                    }
                }
            }
        }
        div(classes = "even-wrapper wobbly-bg") {
            div(classes = "media") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: left;"

                        div(classes = "title-with-emoji") {
                            img(src = "https://assets.perfectdreams.media/loritta/emotes/lori-ban-hammer.png", classes = "emoji-title")

                            h2 {
                                + i18nContext.get(I18nKeysData.Website.Guidelines.WhatYouCantDo.Title)
                            }
                        }

                        ul {
                            for (str in i18nContext.get(I18nKeysData.Website.Guidelines.WhatYouCantDo.Entries)) {
                                li {
                                    + str
                                }
                            }
                        }
                    }
                }
            }
        }
        div(classes = "odd-wrapper wobbly-bg") {
            div(classes = "media") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: center;"

                        div(classes = "title-with-emoji") {
                            img(src = "https://assets.perfectdreams.media/loritta/emotes/lori-sob.png", classes = "emoji-title")

                            h2 {
                                + i18nContext.get(I18nKeysData.Website.Guidelines.IfYouAreBanned.Title)
                            }
                        }

                        p {
                            WebsiteUtils.buildAsHtml(
                                i18nContext.language.textBundle.strings[I18nKeys.Website.Guidelines.IfYouAreBanned.Description.key]!!,
                                {
                                    a(href = "/extras/faq-loritta/loritta-ban-appeal") {
                                        + i18nContext.get(I18nKeysData.Website.Guidelines.IfYouAreBanned.ClickHere)
                                    }
                                },
                                {
                                    + it
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}