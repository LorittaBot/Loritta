package net.perfectdreams.spicymorenitta

import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.serialization.Serializable
import net.perfectdreams.spicymorenitta.utils.onDOMContentLoaded
import net.perfectdreams.spicymorenitta.utils.select
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

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
			document.onDOMContentLoaded {
				callback.invoke()
			}
		else
			callback.invoke()
	}

	@Serializable
	enum class WebsiteTheme(val bodyClass: String, val icon: String) {
		DEFAULT("light", "fas fa-moon"),
		DARK_THEME("dark", "fas fa-sun")
	}
}