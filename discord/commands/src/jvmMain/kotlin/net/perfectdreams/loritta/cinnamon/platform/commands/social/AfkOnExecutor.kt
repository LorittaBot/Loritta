package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenAndRemoveCodeBackticks
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.stripNewLines
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AfkCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class AfkOnExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(AfkOnExecutor::class) {
        object Options : CommandOptions() {
            val reason = optionalString("reason", AfkCommand.I18N_PREFIX.On.Options.Reason)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val profile = context.loritta.services.users.getOrCreateUserProfile(UserId(context.user.id.value))
        val reason = args[Options.reason]?.shortenAndRemoveCodeBackticks(300)?.stripNewLines()

        if (!profile.isAfk || profile.afkReason != reason)
            profile.enableAfk(reason)

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(
                    AfkCommand.I18N_PREFIX.On.AfkModeActivated
                ) + " ${Emotes.Wink}",
                Emotes.Sleeping
            )
        }
    }
}
