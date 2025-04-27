package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class GiveawaySetupCommand(loritta: LorittaBot): DiscordAbstractCommandBase(loritta, listOf("giveaway setup", "sorteio setup", "giveaway criar", "sorteio criar", "giveaway create", "sorteio create"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command"
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Giveaway.Setup
        val logger = KotlinLogging.logger { }
    }

    override fun command() = create {
        userRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)

        canUseInPrivateChannel = false

        localizedDescription("$LOCALE_PREFIX.giveaway.description")

        executesDiscord {
            // This is an empty command because this command is still used on the "GiveawayCommand" command
            // This command is NOT registered on the command map because it has been replaced by the new interactions version
        }
    }
}
