package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.FillContentErrorSection
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

@Composable
inline fun <reified T> ConfigChecker(
    response: LorittaDashboardRPCResponse,
    block: @Composable (T) -> (Unit)
) {
    when (response) {
        is LorittaDashboardRPCResponse.UnknownGuildError -> FillContentErrorSection("A Loritta não está neste servidor!")
        is LorittaDashboardRPCResponse.MissingPermissionError -> FillContentErrorSection("Você não tem permissão para configurar este servidor!")
        is LorittaDashboardRPCResponse.UnknownMemberError -> FillContentErrorSection("Servidor existe... mas você não está no servidor!")
        is T -> {
            block(response)
        }
        else -> error("Unhandled response in the ConfigChecker: ${response::class.simpleName}")
    }

}