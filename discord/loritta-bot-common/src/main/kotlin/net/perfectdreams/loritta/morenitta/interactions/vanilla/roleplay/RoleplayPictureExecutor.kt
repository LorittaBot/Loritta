package net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay

import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions

abstract class RoleplayPictureExecutor(private val attributes: RoleplayActionAttributes) : LorittaSlashCommandExecutor() {
    inner class Options : ApplicationCommandOptions() {
        val user = user("user", attributes.userI18nDescription)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val receiver = args[options.user]

        RoleplayCommand.executeCompat(
            CommandContextCompat.InteractionsCommandContextCompat(context),
            attributes,
            receiver.user
        )
    }
}