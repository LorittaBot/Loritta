package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenAndStripCodeBackticks
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.stripNewLines
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class AfkOnExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val reason = optionalString("reason", AfkCommand.I18N_PREFIX.On.Options.Reason)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val profile = context.loritta.services.users.getOrCreateUserProfile(UserId(context.user.id.value))
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
