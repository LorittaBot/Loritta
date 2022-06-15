package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import org.jetbrains.compose.web.dom.Aside

@Composable
fun AdSidebar() {
    val userInfo = LocalUserIdentification.current

    if (userInfo.displayAds) {
        // https://knowyourmeme.com/memes/that-wasnt-very-cash-money-of-you
        Aside(attrs = { id("that-wasnt-very-cash-money-of-you") }) {}
    }
}