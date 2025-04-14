package net.perfectdreams.loritta.website.frontend

import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.harmony.web.addClass
import net.perfectdreams.harmony.web.removeClass
import net.perfectdreams.loritta.website.frontend.utils.extensions.select
import net.perfectdreams.loritta.website.frontend.utils.extensions.selectAll
import org.w3c.dom.HTMLSpanElement
import web.dom.Element
import web.dom.document

object WebsiteThemeUtils {
    /**
     * Sets the current active theme and stores the user's preference in a cookie
     */
    fun changeWebsiteThemeTo(newTheme: WebsiteTheme) {
        val body = document.body!!
        val themeChangerButton = document.select<Element?>("#theme-changer-button")

        WebsiteTheme.values().forEach {
            body.removeClass(it.bodyClass)
        }

        body.addClass(newTheme.bodyClass)

        CookieUtils.createCookie("userTheme", newTheme.name)

        // Switch icons
        val iconsInTheThemeSwitcherButton = themeChangerButton?.selectAll<HTMLSpanElement>("span")

        if (iconsInTheThemeSwitcherButton != null) {
            println("Icons: ${iconsInTheThemeSwitcherButton.size}")
            val activeIcons = iconsInTheThemeSwitcherButton.filter { it.style.display == "" }
            val inactiveIcons = iconsInTheThemeSwitcherButton.filter { it.style.display != "" }

            activeIcons.forEach { it.style.display = "none" }
            inactiveIcons.forEach { it.style.display = "" }
        }

        // Old website
        if (newTheme == WebsiteTheme.DARK_THEME)
            CookieUtils.createCookie("darkTheme", "true")
        else
            CookieUtils.eraseCookie("darkTheme")
    }
}