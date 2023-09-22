package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.config.GuildCustomCommand
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse

class EditCustomCommandViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope,
    private val guildViewModel: GuildViewModel,
    private val commandId: Long
) : ViewModel(m, scope) {
    val _configResource = mutableStateOf<Resource<DashGuildScopedResponse.GetGuildCustomCommandConfigResponse>>(Resource.Loading())
    val configResource by _configResource

    init {
        println("Initialized ${this::class.simpleName}")
        fetchConfig()
    }

    private fun fetchConfig() {
        fetchConfigAndUpdate(m, this, guildViewModel, _configResource, DashGuildScopedRequest.GetGuildCustomCommandConfigRequest(commandId)) {
            it.guild
        }
    }

    companion object {
        fun toMutableConfig(config: GuildCustomCommand) = MutableGuildCustomCommand(config)
        fun toDataConfig(config: MutableGuildCustomCommand) = GuildCustomCommand(config.id, config.label, config.type, config.code)
    }

    class MutableGuildCustomCommand(config: GuildCustomCommand) {
        val id = config.id
        var label by mutableStateOf(config.label)
        var type by mutableStateOf(config.codeType)
        var code by mutableStateOf(config.code)
    }
}