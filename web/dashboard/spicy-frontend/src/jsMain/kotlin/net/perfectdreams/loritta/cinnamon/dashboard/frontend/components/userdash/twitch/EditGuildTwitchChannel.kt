package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.twitch

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.AScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.TextWithIconWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.EditGuildTwitchChannelScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.EditGuildTwitchChannelViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text

@Composable
fun EditGuildTwitchChannel(
    m: LorittaDashboardFrontend,
    screen: EditGuildTwitchChannelScreen,
    i18nContext: I18nContext,
    guildViewModel: GuildViewModel
) {
    val userInfo = LocalUserIdentification.current
    val configViewModel = viewModel { EditGuildTwitchChannelViewModel(m, it, guildViewModel, screen.trackedId.toLong()) }

    ResourceChecker(
        i18nContext,
        guildViewModel.guildInfoResource,
        configViewModel.configResource
    ) { guild, twitchResponse ->
        AScreen(
            m,
            ScreenPathWithArguments(ScreenPath.ConfigureGuildTwitchPath, mapOf("guildId" to screen.guildId.toString()), mapOf())
        ) {
            DiscordButton(
                DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT
            ) {
                TextWithIconWrapper(SVGIconManager.chevronLeft, {}) {
                    Text("Voltar para a lista de canais da Twitch")
                }
            }
        }

        Hr {}

        TwitchChannelEditor(
            m,
            i18nContext,
            guild,
            userInfo,
            twitchResponse.selfUser,
            twitchResponse.twitchUser!!, // TODO: Add proper checks
            twitchResponse.trackedTwitchAccount,
            twitchResponse.trackingState,
            false,
            ServerPremiumPlans.getPlanFromValue(twitchResponse.activatedPremiumKeysValue),
            twitchResponse.premiumTracksCount,
            false,
            {
                configViewModel.fetchConfig()
            }
        ) { _, _ ->
            // We are BING CHILLING here, we don't really need to do anything on post save on an edit
        }

        Hr {}
    }
}