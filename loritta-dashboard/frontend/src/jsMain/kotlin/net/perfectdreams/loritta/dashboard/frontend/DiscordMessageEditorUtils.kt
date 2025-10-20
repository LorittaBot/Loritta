package net.perfectdreams.loritta.dashboard.frontend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object DiscordMessageEditorUtils {
    var messageEditorRenderDirection by mutableStateOf(RenderDirection.HORIZONTAL)

    enum class RenderDirection {
        VERTICAL,
        HORIZONTAL
    }
}