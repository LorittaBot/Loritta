package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social

import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.GenderCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.getOrCreateUserProfile

class GenderExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val gender = string("gender", GenderCommand.I18N_PREFIX.Options.Gender) {
            choice(GenderCommand.I18N_PREFIX.Female, "female")
            choice(GenderCommand.I18N_PREFIX.Male, "male")
            choice(GenderCommand.I18N_PREFIX.Unknown, "unknown")
        }
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val userSettings = context.loritta.pudding.users.getOrCreateUserProfile(context.user)
            .getProfileSettings()

        val gender = Gender.valueOf(args[options.gender].uppercase())

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
