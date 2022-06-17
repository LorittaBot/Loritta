package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.GlobalState
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

@Composable
fun CloseModalButton(globalState: GlobalState, text: StringI18nData = I18nKeysData.Website.Dashboard.Modal.Close) {
    DiscordButton(
        DiscordButtonType.SECONDARY,
        attrs = {
            onClick {
                globalState.activeModal = null
            }
        }
    ) {
        LocalizedText(globalState, text)
    }
}