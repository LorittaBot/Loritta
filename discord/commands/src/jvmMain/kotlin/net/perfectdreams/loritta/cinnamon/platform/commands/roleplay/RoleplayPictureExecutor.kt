package net.perfectdreams.loritta.cinnamon.platform.commands.roleplay

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.roleplay.retribute.RetributeRoleplayData
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.AnagramCommand
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient

abstract class RoleplayPictureExecutor(
    private val client: RandomRoleplayPicturesClient,
    private val attributes: RoleplayActionAttributes
) : SlashCommandExecutor() {
    companion object {
        object Options : ApplicationCommandOptions() {
            val user = user("user", AnagramCommand.I18N_PREFIX.Options.Text)
                .register()
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage()

        val receiver = args[Options.user]

        context.sendMessage(
            RoleplayUtils.roleplayStuff(
                context.loritta,
                context.i18nContext,
                RetributeRoleplayData(
                    context.user.id,
                    context.user.id,
                    receiver.id
                ),
                client,
                attributes
            )
        )
    }
}