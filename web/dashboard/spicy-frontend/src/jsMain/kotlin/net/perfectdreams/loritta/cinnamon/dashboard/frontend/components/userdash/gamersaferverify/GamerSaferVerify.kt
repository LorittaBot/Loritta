package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.gamersaferverify

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ConfigChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.ConfigureGuildGamerSaferVerifyScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GamerSaferVerifyViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse

@Composable
fun GamerSaferVerify(
    m: LorittaDashboardFrontend,
    screen: ConfigureGuildGamerSaferVerifyScreen,
    i18nContext: I18nContext,
    guildViewModel: GuildViewModel
) {
    val configViewModel = viewModel { GamerSaferVerifyViewModel(m, it, guildViewModel) }

    ResourceChecker(i18nContext, guildViewModel.guildInfoResource) { guildResponse ->
        ConfigChecker<LorittaDashboardRPCResponse.GetGuildInfoResponse.Success>(guildResponse) { guildSuccessResponse ->
            GamerSaferVerifyOverview(
                m,
                screen,
                i18nContext,
                guildSuccessResponse.guild
            )
        }
    }
}