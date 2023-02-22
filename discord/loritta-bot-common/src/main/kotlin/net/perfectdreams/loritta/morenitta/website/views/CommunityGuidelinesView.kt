package net.perfectdreams.loritta.morenitta.website.views

import kotlinx.html.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot

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
                div(classes = "media-figure") {
                    img(src = "https://assets.perfectdreams.media/loritta/emotes/lori-hi.png") {}
                }
                div(classes = "media-body") {
                    div {
                        style = "text-align: left;"

                        div {
                            style = "text-align: center;"
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

                        div {
                            style = "text-align: center;"
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

                div(classes = "media-figure") {
                    img(src = "https://assets.perfectdreams.media/loritta/loritta-police-sortros.png") {}
                }
            }
        }
        div(classes = "odd-wrapper wobbly-bg") {
            div(classes = "media") {
                div(classes = "media-figure") {
                    img(src = "https://assets.perfectdreams.media/loritta/emotes/lori-sob.png") {}
                }

                div(classes = "media-body") {
                    div {
                        style = "text-align: left;"

                        div {
                            style = "text-align: center;"
                            h2 {
                                + i18nContext.get(I18nKeysData.Website.Guidelines.IfYouAreBanned.Title)
                            }
                        }

                        p {
                            buildAsHtml(
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

    private fun buildAsHtml(originalString: String, onControlChar: (String) -> (Unit), onStringBuild: (String) -> (Unit)) {
        var isControl = false

        val genericStringBuilder = StringBuilder()
        val controlStringBuilder = StringBuilder()

        for (ch in originalString) {
            if (isControl) {
                if (ch == '}') {
                    onControlChar.invoke(controlStringBuilder.toString())
                    isControl = false
                    continue
                }

                controlStringBuilder.append(ch)
                continue
            }

            if (ch == '{') {
                onStringBuild.invoke(genericStringBuilder.toString())
                genericStringBuilder.clear()
                isControl = true
                continue
            }

            genericStringBuilder.append(ch)
        }

        onStringBuild.invoke(genericStringBuilder.toString())
    }
}