package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalI18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.AttrBuilderContext
import org.jetbrains.compose.web.dom.ContentBuilder
import org.w3c.dom.HTMLAnchorElement

/**
 * A link that works just like a anchor link, but switches to a screen when clicked
 */
@Composable
fun AScreen(
    m: LorittaDashboardFrontend,
    screenPath: ScreenPathWithArguments,
    attrs: AttrBuilderContext<HTMLAnchorElement>? = null,
    content: ContentBuilder<HTMLAnchorElement>? = null
) {
    val i18nContext = LocalI18nContext.current
    A(
        href = "/${i18nContext.get(I18nKeysData.Website.Dashboard.LocalePathId)}${screenPath.build()}",
        attrs = {
            if (attrs != null)
                attrs()

            onClick {
                it.preventDefault()

                m.routingManager.switchBasedOnPath(i18nContext, screenPath.build(), false)
            }
        }) {

        if (content != null)
            content()
    }
}