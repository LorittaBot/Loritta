package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import kotlinx.datetime.Instant
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.timeDifference
import org.jetbrains.compose.web.dom.Text

@Composable
fun RelativeTimeStamp(i18nContext: I18nContext, instant: Instant) {
    val timestamp = timeDifference(i18nContext, instant)

    Text(timestamp)
}