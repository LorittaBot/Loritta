package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LoadingGifs
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span

@Composable
fun InlineLoadingSection(i18nContext: I18nContext) {
    // We need to "remember" because, if not, sometimes the GIF will change on every keystroke
    val randomGif by remember { mutableStateOf(LoadingGifs.list.random()) }

    Span(attrs = { classes("inline-loading-section") }) {
        Img(randomGif)

        Div {
            LocalizedText(i18nContext, I18nKeysData.Website.Dashboard.Loading)
        }
    }
}