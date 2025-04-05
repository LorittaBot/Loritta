package net.perfectdreams.spicymorenitta.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.spicymorenitta.modals.Modal
import org.jetbrains.compose.web.dom.Text

@Composable
fun CloseModalButton(modal: Modal, text: StringI18nData = I18nKeysData.Website.Dashboard.Modal.Close) {
    DiscordButton(
        DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
        attrs = {
            onClick {
                modal.close()
            }
        }
    ) {
        Text("Fechar") // TODO - htmx-adventures: Fix this!
    }
}