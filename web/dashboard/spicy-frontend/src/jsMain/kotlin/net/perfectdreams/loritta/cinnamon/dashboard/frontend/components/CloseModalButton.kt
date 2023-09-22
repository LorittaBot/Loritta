package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.GlobalState
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Modal
import net.perfectdreams.loritta.i18n.I18nKeysData

@Composable
fun CloseModalButton(globalState: GlobalState, modal: Modal, text: StringI18nData = I18nKeysData.Website.Dashboard.Modal.Close) {
    DiscordButton(
        DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
        attrs = {
            onClick {
                modal.close()
            }
        }
    ) {
        LocalizedText(globalState, text)
    }
}