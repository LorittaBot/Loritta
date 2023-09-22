package net.perfectdreams.loritta.morenitta.website.views

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.ResultRow

class UserBannedView(loritta: LorittaBot, i18nContext: I18nContext, locale: BaseLocale, path: String, val profile: Profile, val bannedState: ResultRow) : NavbarView(loritta, i18nContext, locale, path) {
    override fun getTitle() = "¯\\_(ツ)_/¯"

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            style = "text-align: center;"

            div(classes = "media single-column") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: center;"

                        img(src = "https://stuff.loritta.website/loritta-stop-heathecliff.png") {
                            width = "175"
                        }

                        h1 {
                            + locale["website.userBanned.title"]
                        }

                        for (str in locale.getList("website.userBanned.description")) {
                            p {
                                + str
                            }
                        }
                        p {
                            code {
                                + (bannedState[net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers.reason] ?: "¯\\_(ツ)_/¯")
                            }
                        }
                    }
                }
            }
        }
    }
}