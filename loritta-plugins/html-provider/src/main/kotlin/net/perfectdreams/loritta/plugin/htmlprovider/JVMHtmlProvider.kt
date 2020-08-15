package net.perfectdreams.loritta.plugin.htmlprovider

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.sweetmorenitta.views.HomeView
import net.perfectdreams.loritta.sweetmorenitta.views.SupportView
import net.perfectdreams.loritta.website.utils.LorittaHtmlProvider
import net.perfectdreams.loritta.website.utils.RouteKey
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

class JVMHtmlProvider : LorittaHtmlProvider {
    override fun render(page: String, arguments: List<Any?>): String {
        if (page == RouteKey.SUPPORT) {
            return SupportView(
                    arguments[1] as BaseLocale,
                    arguments[0] as String
            ).generateHtml()
        }

        if (page == RouteKey.HOME) {
            return HomeView(
                    arguments[1] as BaseLocale,
                    arguments[0] as String
            ).generateHtml()
        }

        throw RuntimeException("Can't process page \"$page\"")
    }
}