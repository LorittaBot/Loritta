package net.perfectdreams.spicymorenitta

import net.perfectdreams.spicymorenitta.utils.onDOMReady
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.removeClass

object WebsiteThemeUtils {
	fun changeWebsiteThemeTo(newTheme: WebsiteTheme, afterPageLoad: Boolean) {
		val callback: () -> (Unit) = {
			val body = document.body!!
			val themeChangerButton = document.select<Element?>("#theme-changer-button")

			WebsiteTheme.values().forEach {
				body.removeClass(it.bodyClass)
			}

			body.addClass(newTheme.bodyClass)

			CookiesUtils.createCookie("userTheme", newTheme.name)

			themeChangerButton?.select<HTMLElement>("i")?.setAttribute("class", newTheme.icon)

			// Old website
			if (newTheme == WebsiteThemeUtils.WebsiteTheme.DARK_THEME)
				CookiesUtils.createCookie("darkTheme", "true")
			else
				CookiesUtils.eraseCookie("darkTheme")
		}

		if (afterPageLoad)
			document.onDOMReady {
				callback.invoke()
			}
		else
			callback.invoke()
	}

	enum class WebsiteTheme(val bodyClass: String, val icon: String) {
		DEFAULT("light", "fas fa-moon"),
		DARK_THEME("dark", "fas fa-sun")
	}
}