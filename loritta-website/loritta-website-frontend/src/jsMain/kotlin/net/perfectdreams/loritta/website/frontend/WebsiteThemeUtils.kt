package net.perfectdreams.loritta.website.frontend

import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.loritta.website.frontend.utils.extensions.select
import net.perfectdreams.loritta.website.frontend.utils.extensions.selectAll
import org.w3c.dom.Element
import org.w3c.dom.HTMLSpanElement

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