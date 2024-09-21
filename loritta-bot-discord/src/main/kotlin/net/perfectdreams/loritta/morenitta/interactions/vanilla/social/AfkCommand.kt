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
            enableLegacyMessageSupport = true

            alternativeLegacyAbsoluteCommandPaths.add("awayfromthekeyboard")

            executor = ToggleExecutor()

            subcommand(
                name = OnExecutor.I18N_PREFIX.Label,
                description = OnExecutor.I18N_PREFIX.Description,
                uniqueId = UUID.fromString("0633bb0a-bcdc-4274-bcd9-00d82d5ecf36")
            ) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("ligar")
                    add("ativar")
                }

                executor = OnExecutor()
            }
            subcommand(
                name = OffExecutor.I18N_PREFIX.Label,
                description = OffExecutor.I18N_PREFIX.Description,
                uniqueId = UUID.fromString("92b44d1c-b479-463f-91fd-016018757f87")
            ) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("desligar")
                    add("desativar")
                }

                executor = OffExecutor()
            }
        }

    class OnExecutor: LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options: ApplicationCommandOptions() {
            val reason =
                optionalString(
                    "reason",
                    I18N_PREFIX.Options.Reason
                )
        }

        override val options: Options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val reason = args[options.reason]?.shortenAndStripCodeBackticks(300)?.stripNewLines()
            enable(context, reason)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? = emptyMap()

        companion object {
            val I18N_PREFIX = AfkCommand.I18N_PREFIX.On
        }
    }

    class OffExecutor: LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            disable(context)
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? = emptyMap()

        companion object {
            val I18N_PREFIX = AfkCommand.I18N_PREFIX.Off
        }
    }

    class ToggleExecutor: LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options: ApplicationCommandOptions() {
            val reason =
                optionalString(
                    "reason",
                    OnExecutor.I18N_PREFIX.Options.Reason
                )
        }

        override val options: Options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            if (context is LegacyMessageCommandContext)
                enable(context, args[options.reason])
            else {
                if (context.lorittaUser.profile.isAfk)
                    disable(context)
                else
                    enable(context, args[options.reason])
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? = emptyMap()
    }

    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Afk

        suspend fun enable(
            context: UnleashedContext,
            reason: String?
        ) {
            val profile = context.loritta.pudding.users.getOrCreateUserProfile(UserId(context.user.idLong))
            if (!profile.isAfk || profile.afkReason != reason) {
                profile.enableAfk(reason)
            }

            context.reply(ephemeral = true) {
                styled(
                    context.i18nContext.get(
                        OnExecutor.I18N_PREFIX.AfkModeActivated
                    ),
                    Emotes.LoriSleeping
                )
            }
        }

        suspend fun disable(context: UnleashedContext) {
            val profile = context.loritta.pudding.users.getUserProfile(UserId(context.user.idLong))
            if (profile?.isAfk == true)
                profile.disableAfk()

            context.reply(ephemeral = true) {
                styled(
                    context.i18nContext.get(
                        OffExecutor.I18N_PREFIX.AfkModeDeactivated
                    ),
                    Emotes.LoriZap
                )
            }
        }
    }
}