package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenAndRemoveCodeBackticks
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AboutMeCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class AboutMeExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(AboutMeExecutor::class) {
        object Options : CommandOptions() {
            val text = string("text", AboutMeCommand.I18N_PREFIX.Options.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val text = args[Options.text].shortenAndRemoveCodeBackticks(1000, "")
        val profileSettings = context.loritta.services.users.getOrCreateProfileSettings(UserId(context.user.id.value))

        if (profileSettings.aboutMe != text)
            profileSettings.setAboutMe(text)

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(AboutMeCommand.I18N_PREFIX.SuccessfullyChanged(text)),
                Emotes.Tada
            )
        }
    }
}