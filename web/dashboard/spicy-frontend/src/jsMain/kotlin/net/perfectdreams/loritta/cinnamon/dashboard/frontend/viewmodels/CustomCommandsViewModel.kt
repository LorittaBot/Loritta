package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.CoroutineScope
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.config.GuildCustomCommandsConfig
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse

class CustomCommandsViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope,
    private val guildViewModel: GuildViewModel
) : ViewModel(m, scope) {
    val _configResource = mutableStateOf<Resource<DashGuildScopedResponse.GetGuildCustomCommandsConfigResponse>>(Resource.Loading())
    val configResource by _configResource

    init {
        println("Initialized ${this::class.simpleName}")
        fetchConfig()
    }

    fun fetchConfig() {
        fetchConfigAndUpdate(m, this, guildViewModel, _configResource, DashGuildScopedRequest.GetGuildCustomCommandsConfigRequest) {
            it.guild
        }
    }

    companion object {
        fun toMutableConfig(config: GuildCustomCommandsConfig) = MutableGuildCustomCommandsConfig(config)
        fun toDataConfig(config: MutableGuildCustomCommandsConfig) = GuildCustomCommandsConfig(config.commands)
    }

    class MutableGuildCustomCommandsConfig(config: GuildCustomCommandsConfig) {
        val commands = config.commands.toMutableStateList()
    }
}