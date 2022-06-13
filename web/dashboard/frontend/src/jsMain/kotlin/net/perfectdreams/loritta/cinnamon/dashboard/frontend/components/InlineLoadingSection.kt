package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun InlineLoadingSection(i18nContext: I18nContext) {
    Span(attrs = { classes("inline-loading-section") }) {
        Img("https://cdn.discordapp.com/emojis/957368372025262120.gif?size=160&quality=lossless")

        Div {
            Text("Carregando...")
        }
    }
}