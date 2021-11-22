package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class AfkOffExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(AfkOffExecutor::class)

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val profile = context.loritta.services.users.getUserProfile(UserId(context.user.id.value))

        if (profile?.isAfk == true)
            profile.disableAfk()

        context.sendMessage {
            styled(
                context.i18nContext.get(
                    AfkCommand.I18N_PREFIX.Off.AfkModeDeactivated
                ),
                Emotes.BabyChick
            )
        }
    }
}