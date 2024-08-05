package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand.Companion.CATEGORY_I18N_PREFIX
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand

class SonhosCommand(private val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(I18nKeysData.Commands.Command.Sonhos.Label2, CATEGORY_I18N_PREFIX.RootCommandDescription, CommandCategory.ECONOMY) {
        this.enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        subcommand(I18nKeysData.Commands.Command.Sonhosatm.Label, I18nKeysData.Commands.Command.Sonhosatm.Description) {
            this.alternativeLegacyAbsoluteCommandPaths.apply {
                add("sonhos")
                add("atm")
                add("bal")
                add("balance")
            }
            executor = SonhosAtmExecutor(loritta)
        }
    }
}