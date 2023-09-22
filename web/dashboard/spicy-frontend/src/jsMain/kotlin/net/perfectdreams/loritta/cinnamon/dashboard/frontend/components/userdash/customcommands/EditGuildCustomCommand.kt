package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.customcommands

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.ButtonWithIconWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.EditGuildCustomCommandScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.EditCustomCommandViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text

@Composable
fun EditGuildCustomCommand(
    m: LorittaDashboardFrontend,
    screen: EditGuildCustomCommandScreen,
    i18nContext: I18nContext,
    guildViewModel: GuildViewModel
) {
    val userInfo = LocalUserIdentification.current
    val configViewModel = viewModel { EditCustomCommandViewModel(m, it, guildViewModel, screen.commandId) }

    ResourceChecker(
        i18nContext,
        guildViewModel.guildInfoResource,
        configViewModel.configResource
    ) { guild, customCommandsResponse ->
        DiscordButton(
            DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
            attrs = {
                onClick {
                    m.routingManager.switchBasedOnPath(i18nContext, ScreenPathWithArguments(ScreenPath.ConfigureGuildCustomCommandsPath, mapOf("guildId" to screen.guildId.toString()), mapOf()).build(), false)
                }
            }
        ) {
            ButtonWithIconWrapper(SVGIconManager.chevronLeft, {}) {
                Text("Voltar para a lista de comandos personalizados")
            }
        }

        Hr {}

        CustomCommandEditor(
            m,
            i18nContext,
            guild,
            userInfo,
            customCommandsResponse.selfUser,
            customCommandsResponse.customCommand,
            false
        ) { _, _ ->
            // We are BING CHILLING here, we don't really need to do anything on post save on an edit
        }
    }
}