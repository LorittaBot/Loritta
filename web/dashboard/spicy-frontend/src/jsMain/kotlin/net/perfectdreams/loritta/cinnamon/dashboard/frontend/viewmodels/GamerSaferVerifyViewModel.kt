package net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.serializable.GamerSaferVerificationUserAndRole
import net.perfectdreams.loritta.serializable.config.GuildGamerSaferConfig
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

class GamerSaferVerifyViewModel(
    m: LorittaDashboardFrontend,
    scope: CoroutineScope,
    private val guildViewModel: GuildViewModel
) : ViewModel(m, scope) {
    private val _configResource = mutableStateOf<Resource<LorittaDashboardRPCResponse.GetGuildGamerSaferConfigResponse>>(Resource.Loading())
    val configResource by _configResource

    init {
        println("Initialized GamerSaferVerifyViewModel")
        fetchConfig()
    }

    private fun fetchConfig() {
        scope.launch {
            m.makeRPCRequestAndUpdateState<LorittaDashboardRPCResponse.GetGuildGamerSaferConfigResponse>(
                _configResource,
                LorittaDashboardRPCRequest.GetGuildGamerSaferConfigRequest(guildViewModel.guildId)
            )
        }
    }

    class MutableGuildGamerSaferConfig(
        val verificationRoleId: MutableState<Long?>,
        val verificationRoles: SnapshotStateList<GamerSaferVerificationUserAndRole>
    ) {
        companion object {
            fun from(config: GuildGamerSaferConfig) = MutableGuildGamerSaferConfig(
                mutableStateOf(config.verificationRoleId),
                config.verificationRoles.toMutableStateList()
            )

            fun to(config: MutableGuildGamerSaferConfig) = GuildGamerSaferConfig(
                config.verificationRoleId.value,
                config.verificationRoles.toList()
            )
        }
    }
}