package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.RightSidebar

@Composable
fun UserRightSidebar(
    m: LorittaDashboardFrontend,
    block: @Composable () -> (Unit)
) {
    RightSidebar {
        block.invoke()

        // TODO: Re-enable this after we figure out how this legalese stuff works
        // Hr {}

        // LegalFooter()
    }
}