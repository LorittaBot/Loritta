package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import kotlinx.coroutines.CoroutineScope
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend

class GamerSaferVerifyViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope,
    private val guildViewModel: GuildViewModel
) : ViewModel(m, scope) {
    init {
        println("Initialized GamerSaferVerifyViewModel")
    }
}