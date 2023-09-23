package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.customcommands

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.AScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.TextWithIconWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash.ResourceChecker
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.AddNewGuildCustomCommandScreen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.LocalUserIdentification
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPath
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.CustomCommandsViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.GuildViewModel
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.viewmodels.viewModel
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.serializable.config.GuildCustomCommand
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text

@Composable
fun AddNewGuildCustomCommand(
    m: LorittaDashboardFrontend,
    screen: AddNewGuildCustomCommandScreen,
    i18nContext: I18nContext,
    guildViewModel: GuildViewModel
) {
    val userInfo = LocalUserIdentification.current
    val configViewModel = viewModel { CustomCommandsViewModel(m, it, guildViewModel) }

    val defaultGuildCustomCommand = GuildCustomCommand(
        -1, // Unknown ID / Insert new command
        "loritta",
        CustomCommandCodeType.SIMPLE_TEXT,
        "A Loritta é muito fofa!",
    )

    ResourceChecker(
        i18nContext,
        guildViewModel.guildInfoResource,
        configViewModel.configResource
    ) { guild, customCommandsResponse ->
        AScreen(
            m,
            ScreenPathWithArguments(ScreenPath.ConfigureGuildCustomCommandsPath, mapOf("guildId" to screen.guildId.toString()), mapOf())
        ) {
            DiscordButton(
                DiscordButtonType.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT,
                attrs = {
                    onClick {
                        m.routingManager.switchBasedOnPath(
                            i18nContext,
                            ScreenPathWithArguments(
                                ScreenPath.ConfigureGuildCustomCommandsPath,
                                mapOf("guildId" to screen.guildId.toString()),
                                mapOf()
                            ).build(),
                            false
                        )
                    }
                }
            ) {
                TextWithIconWrapper(SVGIconManager.chevronLeft, {}) {
                    Text("Voltar para a lista de comandos personalizados")
                }
            }
        }

        Hr {}

        CustomCommandEditor(
            m,
            i18nContext,
            guild,
            userInfo,
            customCommandsResponse.selfUser,
            defaultGuildCustomCommand,
            true
        ) { isNewCommand, commandId ->
            // To be honest we don't *really* need to check if it is a new command because all commands from this route WILL be a new command
            if (isNewCommand) {
                // After creating the command, switch to the edit command page
                m.routingManager.switchBasedOnPath(
                    i18nContext,
                    ScreenPathWithArguments(
                        ScreenPath.EditGuildCustomCommandPath,
                        mapOf("guildId" to screen.guildId.toString(), "commandId" to commandId.toString()),
                        mapOf()
                    ).build(),
                    false
                )
            }
        }
    }
}