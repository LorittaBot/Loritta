package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.morenitta.LorittaBot

class AfkOffExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val profile = context.loritta.pudding.users.getUserProfile(UserId(context.user.id.value))

        if (profile?.isAfk == true)
            profile.disableAfk()

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(
                    AfkCommand.I18N_PREFIX.Off.AfkModeDeactivated
                ),
                Emotes.LoriZap
            )
        }
    }
}