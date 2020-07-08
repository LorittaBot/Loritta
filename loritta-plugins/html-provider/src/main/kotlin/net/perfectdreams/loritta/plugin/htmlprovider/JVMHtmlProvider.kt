package net.perfectdreams.loritta.plugin.htmlprovider

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.sweetmorenitta.views.SupportView
import net.perfectdreams.loritta.website.utils.LorittaHtmlProvider
import java.lang.RuntimeException

class JVMHtmlProvider : LorittaHtmlProvider {
    override fun render(page: String, arguments: List<Any?>): String {
        if (page == "support")
            return SupportView(arguments.first { it is BaseLocale } as BaseLocale).generateHtml()

        throw RuntimeException("Can't process page \"$page\"")
    }
}