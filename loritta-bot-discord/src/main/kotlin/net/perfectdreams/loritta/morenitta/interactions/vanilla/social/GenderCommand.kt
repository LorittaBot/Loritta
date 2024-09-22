package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationBuilder
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.serializable.UserId
import java.util.UUID

class GenderCommand: SlashCommandDeclarationWrapper {
    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = I18N_PREFIX.Description,
            category = CommandCategory.SOCIAL,
            uniqueId = UUID.fromString("ee0cdf11-6e19-4264-a4af-51413454c34e")
        ) {
            executor = GenderExecutor()
        }

    class GenderExecutor: LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val gender = string("gender", I18N_PREFIX.Options.Gender) {
                choice(I18N_PREFIX.Female, "female")
                choice(I18N_PREFIX.Male, "male")
                choice(I18N_PREFIX.Unknown, "unknown")
            }
        }

        override val options: Options = Options()

        override suspend fun execute(
            context: UnleashedContext,
            args: SlashCommandArguments
        ) {
            if (context is LegacyMessageCommandContext) {
                context.reply(ephemeral = true, "This command can only be used as a slash command!")
                return
            }

            val userSettings = context.loritta.pudding.users.getOrCreateUserProfile(UserId(context.user.idLong))
                .getProfileSettings()

            val gender = Gender.valueOf(args[options.gender].uppercase())

            if (userSettings.gender != gender)
                userSettings.setGender(gender)

            context.reply(ephemeral = true) {
                styled(
                    context.i18nContext.get(I18N_PREFIX.SuccessfullyChanged),
                    Emotes.LORI_PAT
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            context.reply(ephemeral = true, "This command can only be used as as slash command!")
            val choice = args.singleOrNull() ?: GENDER_NOT_SPECIFIED
            return mapOf(options.gender to choice)
        }
    }

    private companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Gender

        const val GENDER_NOT_SPECIFIED = "none"
    }
}