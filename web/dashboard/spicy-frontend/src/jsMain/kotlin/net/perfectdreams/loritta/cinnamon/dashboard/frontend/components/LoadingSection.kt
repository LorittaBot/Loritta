package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LoadingGifs
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img

@Composable
fun LoadingSection(i18nContext: I18nContext) {
    Div(attrs = { classes("loading-section") }) {
        Img(LoadingGifs.list.random())

        Div {
            LocalizedText(i18nContext, I18nKeysData.Website.Dashboard.Loading)
        }
    }
}

/**
 * A [LoadingSection] that fills the entire height of the parent div, centralizing the loading section
 */
@Composable
fun FillContentLoadingSection(i18nContext: I18nContext) {
    Div(attrs = { classes("loading-section", "fill-content-loading-section") }) {
        Img(LoadingGifs.list.random())

        Div {
            LocalizedText(i18nContext, I18nKeysData.Website.Dashboard.Loading)
        }
    }
}