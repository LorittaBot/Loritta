package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Text

@Composable
fun EmptySection(i18nContext: I18nContext) {
    Div(attrs = { classes("empty-section") }) {
        Img(src = "https://assets.perfectdreams.media/loritta/emotes/lori-sob.png") {
            attr("width", "192")
            attr("height", "192")
        }

        Div(attrs = { classes("philosophical-text") }) {
            Text(i18nContext.get(I18nKeysData.Commands.Command.Transactions.NoTransactionsFunnyMessages).random())
        }
    }
}