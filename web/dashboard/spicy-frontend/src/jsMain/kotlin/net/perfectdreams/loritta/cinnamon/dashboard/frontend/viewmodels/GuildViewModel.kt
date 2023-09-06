package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.DiscordGuild

class GuildViewModel(m: LorittaDashboardFrontend, scope: CoroutineScope, val guildId: Long) : ViewModel(m, scope) {
    val _guildInfoResource = mutableStateOf<Resource<DiscordGuild>>(Resource.Loading())
    val guildInfoResource by _guildInfoResource

    init {
        println("Initialized GuildViewModel")
    }
}