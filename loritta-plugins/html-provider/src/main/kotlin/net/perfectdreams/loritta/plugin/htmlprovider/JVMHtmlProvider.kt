package net.perfectdreams.loritta.plugin.htmlprovider

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import io.ktor.request.path
import net.perfectdreams.loritta.sweetmorenitta.utils.WebRenderSettings
import net.perfectdreams.loritta.sweetmorenitta.views.HomeView
import net.perfectdreams.loritta.sweetmorenitta.views.SupportView
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.HackyWebSettings
import net.perfectdreams.loritta.website.utils.LorittaHtmlProvider
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance
import java.lang.RuntimeException

class JVMHtmlProvider : LorittaHtmlProvider {
    override fun render(page: String, arguments: List<Any?>): String {
        val hacky = arguments.firstIsInstance<HackyWebSettings>()
        val wrSettings = WebRenderSettings(
                hacky.websiteUrl,
                hacky.path,
                HtmlProviderPlugin.assetHashProvider,
                hacky.addBotUrl
        )

        if (page == "support") {
            return SupportView(
                    wrSettings,
                    arguments.first { it is BaseLocale } as BaseLocale
            ).generateHtml()
        }
        if (page == "home") {
            return HomeView(
                    wrSettings,
                    arguments.first { it is BaseLocale } as BaseLocale
            ).generateHtml()
        }

        throw RuntimeException("Can't process page \"$page\"")
    }
}