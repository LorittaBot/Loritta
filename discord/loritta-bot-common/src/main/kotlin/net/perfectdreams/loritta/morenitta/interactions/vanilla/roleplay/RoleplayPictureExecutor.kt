package net.perfectdreams.loritta.morenitta.interactions.vanilla.roleplay

import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember

abstract class RoleplayPictureExecutor(private val attributes: RoleplayActionAttributes) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    inner class Options : ApplicationCommandOptions() {
        val user = user("user", attributes.userI18nDescription)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        val receiver = args[options.user]

        RoleplayCommand.executeCompat(
            context,
            attributes,
            receiver.user
        )
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        val mentionedUser = context.getUser(0) ?: run {
            context.explain()
            return null
        }

        return mapOf(options.user to UserAndMember(mentionedUser, null))
    }
}