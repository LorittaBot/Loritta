package net.perfectdreams.loritta.morenitta.interactions.vanilla.social

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenAndStripCodeBackticks
import net.perfectdreams.loritta.common.utils.text.TextUtils.stripNewLines
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

class AfkCommand: SlashCommandDeclarationWrapper {
    override fun command(): SlashCommandDeclarationBuilder =
        slashCommand(
            name = I18N_PREFIX.Label,
            description = TodoFixThisData,
            category = CommandCategory.SOCIAL,
            uniqueId = UUID.fromString("bcf80930-44b9-4a60-814e-0a9549e939ed")
        ) {
            subcommand(I18N_PREFIX.On.Label, I18N_PREFIX.On.Description, UUID.fromString("0633bb0a-bcdc-4274-bcd9-00d82d5ecf36")) {
                executor = AfkOnExecutor()
            }
            subcommand(I18N_PREFIX.Off.Label, I18N_PREFIX.Off.Description, UUID.fromString("92b44d1c-b479-463f-91fd-016018757f87")) {
                executor = AfkOffExecutor()
            }
        }

    class AfkOnExecutor: LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options: ApplicationCommandOptions() {
            val reason =
                optionalString(
                    "reason",
                    I18N_PREFIX.On.Options.Reason
                )
        }

        override val options: Options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val profile = context.loritta.pudding.users.getOrCreateUserProfile(UserId(context.user.idLong))
            val reason = args[options.reason]?.shortenAndStripCodeBackticks(300)?.stripNewLines()

            if (!profile.isAfk || profile.afkReason != reason)
                profile.enableAfk(reason)

            context.reply(ephemeral = true) {
                styled(
                    context.i18nContext.get(
                        I18N_PREFIX.On.AfkModeActivated
                    ),
                    Emotes.LoriSleeping
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? = emptyMap()
    }

    class AfkOffExecutor: LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val profile = context.loritta.pudding.users.getUserProfile(UserId(context.user.idLong))
            if (profile?.isAfk == true)
                profile.disableAfk()

            context.reply(ephemeral = true) {
                styled(
                    context.i18nContext.get(
                        AfkCommand.I18N_PREFIX.Off.AfkModeDeactivated
                    ),
                    Emotes.LoriZap
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? = emptyMap()
    }

    private companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Afk
    }
}