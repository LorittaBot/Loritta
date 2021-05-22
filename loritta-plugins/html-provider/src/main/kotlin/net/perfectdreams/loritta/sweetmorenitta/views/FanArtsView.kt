package net.perfectdreams.loritta.sweetmorenitta.views

import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.html.DIV

class FanArtsView(
    locale: BaseLocale,
    path: String
) : NavbarView(
        locale,
        path
) {
    override fun getTitle() = "Fan Arts"

    override fun DIV.generateContent() {
        // Generated in the frontend
    }
}