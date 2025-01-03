package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Payment
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import org.jetbrains.exposed.sql.and
import java.util.*

class PremiumCommand(val m: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Premium
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.UTILS, UUID.fromString("8cfac679-16ed-4de0-8e81-ed05ab93ad05")) {
        enableLegacyMessageSupport = true

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = PremiumExecutor(m)
    }

    class PremiumExecutor(val m: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(true)
            val payments = m.transaction {
                Payment.find {
                    (Payments.expiresAt greaterEq System.currentTimeMillis()) and
                            (Payments.reason eq PaymentReason.DONATION) and
                            (Payments.userId eq context.user.idLong)
                }.toList()
            }

            if (payments.isEmpty()) {
                context.reply(true) {
                    styled(context.i18nContext.get(I18N_PREFIX.YouDontHaveLorittaPremium))
                }
                return
            }

            context.reply(true) {
                for (payment in payments) {
                    if (payment.expiresAt == Long.MAX_VALUE) {
                        styled(context.i18nContext.get(I18N_PREFIX.PaymentPermanent(payment.money)))
                    } else {
                        styled(context.i18nContext.get(I18N_PREFIX.PaymentTemporary(payment.money, DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(payment.expiresAt!!))))
                    }
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            return emptyMap()
        }
    }
}