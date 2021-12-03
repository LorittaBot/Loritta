package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.GenderCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class GenderExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(GenderExecutor::class) {
        object Options: CommandOptions() {
            val gender = string("gender", GenderCommand.I18N_PREFIX.Options.Gender)
                .choice("female", GenderCommand.I18N_PREFIX.Female)
                .choice("male", GenderCommand.I18N_PREFIX.Male)
                .choice("unknown", GenderCommand.I18N_PREFIX.Unknown)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val userSettings = context.loritta.services.users.getOrCreateProfileSettings(UserId(context.user.id.value))
        val gender = Gender.valueOf(args[Options.gender].uppercase())

        if (userSettings.gender != gender)
            userSettings.setGender(gender)

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(GenderCommand.I18N_PREFIX.SuccessfullyChanged),
                Emotes.Tada
            )
        }
    }
}
