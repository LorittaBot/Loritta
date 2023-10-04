package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.twitch

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.AScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.TextWithIconWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.AddNewGuildTwitchChannelScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.AddNewGuildTwitchChannelViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.serializable.config.TrackedTwitchAccount
import net.perfectdreams.loritta.serializable.config.TwitchAccountTrackState
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text

@Composable
fun AddNewGuildTwitchChannel(
    m: LorittaDashboardFrontend,
    screen: AddNewGuildTwitchChannelScreen,
    i18nContext: I18nContext,
    guildViewModel: GuildViewModel
) {
    val userInfo = LocalUserIdentification.current
    val configViewModel = viewModel { AddNewGuildTwitchChannelViewModel(m, it, guildViewModel, screen.userId.toLong()) }

    val defaultTrackedTwitchAccount = TrackedTwitchAccount(
        -1, // Unknown ID / Insert new tracked account
        screen.userId,
        -1,
        "Estou ao vivo jogando {stream.game}! **{stream.title}** {stream.url}",
    )

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
            defaultTrackedTwitchAccount,
            if (screen.createPremiumTrack) TwitchAccountTrackState.PREMIUM_TRACK_USER else twitchResponse.trackingState,
            screen.createPremiumTrack,
            ServerPremiumPlans.getPlanFromValue(twitchResponse.activatedPremiumKeysValue),
            twitchResponse.premiumTracksCount,
            true,
            {
                configViewModel.fetchConfig()
            }
        ) { isNewCommand, trackedId ->
            // To be honest we don't *really* need to check if it is a new tracked account because all tracked accounts from this route WILL be a new tracked account
            if (isNewCommand) {
                // After creating the command, switch to the edit command page
                m.routingManager.switchBasedOnPath(
                    i18nContext,
                    ScreenPathWithArguments(
                        ScreenPath.EditGuildTwitchChannelPath,
                        mapOf("guildId" to screen.guildId.toString(), "trackedId" to trackedId.toString()),
                        mapOf()
                    ).build(),
                    false
                )
            }
        }

        Hr {}
    }
}