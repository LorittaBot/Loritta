package net.perfectdreams.loritta.sweetmorenitta.views

import com.mrpowergamerbr.loritta.dao.Profile
import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV
import kotlinx.html.code
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.style
import org.jetbrains.exposed.sql.ResultRow

class UserBannedView(locale: BaseLocale, path: String, val profile: Profile, val bannedState: ResultRow) : NavbarView(locale, path) {
    override fun getTitle() = "¯\\_(ツ)_/¯"

    override fun DIV.generateContent() {
        div(classes = "odd-wrapper") {
            style = "text-align: center;"

            div(classes = "media single-column") {
                div(classes = "media-body") {
                    div {
                        style = "text-align: center;"

                        img(src = "https://loritta.website/assets/img/fanarts/l6.png") {
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
                                + (bannedState[net.perfectdreams.loritta.tables.BannedUsers.reason] ?: "¯\\_(ツ)_/¯")
                            }
                        }
                    }
                }
            }
        }
    }
}