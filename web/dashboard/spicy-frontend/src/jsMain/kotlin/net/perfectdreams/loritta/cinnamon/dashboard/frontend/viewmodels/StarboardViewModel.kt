package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.config.GuildStarboardConfig
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse

class StarboardViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope,
    private val guildViewModel: GuildViewModel
) : ViewModel(m, scope) {
    val _configResource = mutableStateOf<Resource<DashGuildScopedResponse.GetGuildStarboardConfigResponse>>(Resource.Loading())
    val configResource by _configResource

    init {
        println("Initialized ${this::class.simpleName}")
        fetchConfig()
    }

    private fun fetchConfig() {
        fetchConfigAndUpdate(m, this, guildViewModel, _configResource, DashGuildScopedRequest.GetGuildStarboardConfigRequest) {
            it.guild
        }
    }

    companion object {
        fun toMutableConfig(config: GuildStarboardConfig) = MutableGuildStarboardConfig(config)
        fun toDataConfig(config: MutableGuildStarboardConfig) = GuildStarboardConfig(
            config.enabled,
            config.starboardChannelId,
            config.requiredStars
        )

        class MutableGuildStarboardConfig(config: GuildStarboardConfig) {
            var _enabled = mutableStateOf(config.enabled)
            var enabled by _enabled
            var _starboardChannelId = mutableStateOf(config.starboardChannelId)
            var starboardChannelId by _starboardChannelId
            var _requiredStars = mutableStateOf(config.requiredStars)
            var requiredStars by _requiredStars
        }
    }
}