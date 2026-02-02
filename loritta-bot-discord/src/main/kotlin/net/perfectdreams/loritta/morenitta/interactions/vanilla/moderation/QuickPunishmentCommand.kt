package net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import java.util.*

class QuickPunishmentCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Quickpunishment
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.MODERATION, UUID.fromString("d4c8e3a1-7f92-4b6d-a1e3-9c5f8b2d4a7e")) {
        enableLegacyMessageSupport = true

        executor = QuickPunishmentExecutor()
    }

    inner class QuickPunishmentExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val userData = context.config.getUserData(loritta, context.user.idLong)

            if (userData.quickPunishment) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Disabled),
                        Emotes.LORI_BAN_HAMMER
                    )
                    styled(
                        context.i18nContext.get(I18N_PREFIX.HowEnable),
                        Emotes.LORI_BAN_HAMMER
                    )
                }
            } else {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.Enabled),
                        Emotes.LORI_BAN_HAMMER
                    )
                    styled(
                        context.i18nContext.get(I18N_PREFIX.HowDisable),
                        Emotes.LORI_BAN_HAMMER
                    )
                }
            }

            loritta.newSuspendedTransaction {
                userData.quickPunishment = !userData.quickPunishment
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return LorittaLegacyMessageCommandExecutor.NO_ARGS
        }
    }
}
