package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class AfkOffExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val profile = context.loritta.services.users.getUserProfile(UserId(context.user.id.value))

        if (profile?.isAfk == true)
            profile.disableAfk()

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(
                    AfkCommand.I18N_PREFIX.Off.AfkModeDeactivated
                ),
                Emotes.BabyChick
            )
        }
    }
}