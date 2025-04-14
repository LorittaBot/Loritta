package net.perfectdreams.loritta.website.backend.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.coroutines.runBlocking
import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.style
import kotlinx.html.ul
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.website.backend.LorittaWebsiteBackend
import net.perfectdreams.loritta.website.backend.utils.imgSrcSetFromEtherealGambi
import net.perfectdreams.loritta.website.backend.utils.innerContent

class ContactView(
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
    override fun getTitle() = i18nContext.get(I18nKeysData.Website.Contact.Title)

    override fun DIV.generateContent() {
        innerContent {
            div(classes = "odd-wrapper") {
                div(classes = "media") {
                    div(classes = "media-body") {
                        div {
                            style = "text-align: center;"

                            div {
                                style = "text-align: center;"
                                h1 {
                                    +i18nContext.get(I18nKeysData.Website.Contact.Title)
                                }
                            }

                            for (text in i18nContext.language.textBundle.lists[I18nKeys.Website.Contact.Description.key]!!) {
                                // TODO: Move this controller thing somewhere else
                                val stringBuilder = StringBuilder()
                                var isControl = false

                                p {
                                    for (ch in text) {
                                        if (ch == '{') {
                                            + i18nContext.formatter.format(stringBuilder.toString(), mapOf())
                                            stringBuilder.clear()
                                            isControl = true
                                            continue
                                        }

                                        if (isControl && ch == '}') {
                                            if (stringBuilder.toString() == "loriSunglasses") {
                                                imgSrcSetFromEtherealGambi(
                                                    LorittaWebsiteBackend,
                                                    // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                                                    runBlocking { LorittaWebsiteBackend.getOrRetrieveImageInfo("loritta/emotes/lori-sunglasses")!! },
                                                    "png",
                                                    "1.5em"
                                                ) {
                                                    classes = setOf("inline-emoji")
                                                }
                                            }
                                            if (stringBuilder.toString() == "loriZap") {
                                                imgSrcSetFromEtherealGambi(
                                                    LorittaWebsiteBackend,
                                                    // TODO: Fix this SUPER HACK:tm: - (However do we really care about this? While it does suspend, it should be 99,99% of the times in memory, right)
                                                    runBlocking { LorittaWebsiteBackend.getOrRetrieveImageInfo("loritta/emotes/lori-zap")!! },
                                                    "png",
                                                    "1.5em"
                                                ) {
                                                    classes = setOf("inline-emoji")
                                                }
                                            }

                                            stringBuilder.clear()
                                            isControl = false
                                            continue
                                        }

                                        stringBuilder.append(ch)
                                    }

                                    if (stringBuilder.isNotEmpty())
                                        + i18nContext.formatter.format(stringBuilder.toString(), mapOf())
                                }
                            }
                        }
                    }
                }
            }

            div(classes = "even-wrapper wobbly-bg") {
                div(classes = "contact-methods-wrapper") {
                    div(classes = "contact-method-wrapper") {
                        div(classes = "contact-method-content") {
                            h2 {
                                + i18nContext.get(I18nKeysData.Website.Contact.Support.Title)
                            }

                            p {
                                + i18nContext.get(I18nKeysData.Website.Contact.Support.VisitOurSupportPageIf)
                            }

                            ul {
                                style = "text-align: left;"

                                for (line in i18nContext.get(I18nKeysData.Website.Contact.Support.SupportReasons)) {
                                    li {
                                        + line
                                    }
                                }
                            }
                        }

                        div(classes = "contact-method-way") {
                            a(classes = "add-me button primary", href = "/${locale.path}/support") {
                                style = "display: block; width: fit-content; margin: auto;"
                                iconManager.discord.apply(this)
                                +" ${i18nContext.get(I18nKeysData.Website.Contact.Support.Title)}"
                            }
                        }
                    }

                    div(classes = "contact-method-wrapper") {
                        div(classes = "contact-method-content") {
                            h2 {
                                +i18nContext.get(I18nKeysData.Website.Contact.BusinessContact.Title)
                            }

                            p {
                                + i18nContext.get(I18nKeysData.Website.Contact.BusinessContact.SendAnEmailIf)
                            }

                            ul {
                                style = "text-align: left;"

                                for (line in i18nContext.get(I18nKeysData.Website.Contact.BusinessContact.BusinessContactReasons)) {
                                    li {
                                        + line
                                    }
                                }
                            }

                            p {
                                + i18nContext.get(I18nKeysData.Website.Contact.BusinessContact.BusinessContactTip)
                            }
                        }

                        div(classes = "contact-method-way") {
                            a(href = "mailto:howdy@loritta.website") {
                                + "howdy@loritta.website"
                            }
                        }
                    }
                }
            }
        }
    }
}