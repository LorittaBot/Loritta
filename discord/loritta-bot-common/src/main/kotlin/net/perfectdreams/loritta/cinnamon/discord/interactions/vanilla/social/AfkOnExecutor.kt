package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social

import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenAndStripCodeBackticks
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripNewLines
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.serializable.UserId

class AfkOnExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val reason = optionalString("reason", AfkCommand.I18N_PREFIX.On.Options.Reason)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val profile = context.loritta.pudding.users.getOrCreateUserProfile(UserId(context.user.id.value))
        val reason = args[options.reason]?.shortenAndStripCodeBackticks(300)?.stripNewLines()

        if (!profile.isAfk || profile.afkReason != reason)
            profile.enableAfk(reason)

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(
                    AfkCommand.I18N_PREFIX.On.AfkModeActivated
                ) + " ${Emotes.Wink}",
                Emotes.LoriSleeping
            )
        }
    }
}
