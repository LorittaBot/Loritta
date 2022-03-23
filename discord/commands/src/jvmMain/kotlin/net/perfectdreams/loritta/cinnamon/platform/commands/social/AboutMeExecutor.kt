package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenAndRemoveCodeBackticks
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.AboutMeCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

class AboutMeExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration(AboutMeExecutor::class) {
        object Options : ApplicationCommandOptions() {
            val text = string("text", AboutMeCommand.I18N_PREFIX.Options.Text)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val text = args[Options.text].shortenAndRemoveCodeBackticks(1000, "")
        val profileSettings = context.loritta.services.users.getOrCreateProfileSettings(context.user.id.value.toLong())

        if (profileSettings.aboutMe != text)
            context.loritta.services.users.setAboutMe(profileSettings.id.value.toLong(), text)

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(AboutMeCommand.I18N_PREFIX.SuccessfullyChanged(text)),
                Emotes.Tada
            )
        }
    }
}