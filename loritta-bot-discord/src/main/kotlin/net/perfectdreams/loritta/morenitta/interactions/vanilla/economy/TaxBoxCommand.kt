package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.interactions.components.Thumbnail
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.TaxBoxes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.TaxBoxConfigs
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.common.utils.ServerPremiumPlan
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.serializable.StoredTaxBoxWithdrawTransaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

class TaxBoxCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Taxbox
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("b92ab741-bc30-4236-95ea-507465244528")) {
        enableLegacyMessageSupport = true
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD)
        this.defaultMemberPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)

        executor = TaxBoxExecutor(loritta)
    }

    class TaxBoxExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val guild = context.guildOrNull ?: return

            context.deferChannelMessage(false)

            val guildId = guild.idLong
            val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

            val data = loritta.transaction {
                val donationKeys = DonationKeys.selectAll()
                    .where {
                        DonationKeys.activeIn eq guildId and (DonationKeys.expiresAt greaterEq now.toInstant().toEpochMilli())
                    }
                    .toList()

                val value = donationKeys.sumOf { it[DonationKeys.value] }
                val plan = ServerPremiumPlan.getPlanFromValue(value)

                val taxBoxConfig = TaxBoxConfigs.selectAll()
                    .where { TaxBoxConfigs.id eq guildId }
                    .firstOrNull()

                val taxBox = TaxBoxes.selectAll()
                    .where { TaxBoxes.id eq guildId }
                    .firstOrNull()

                val balance = taxBox?.get(TaxBoxes.sonhos) ?: 0L
                val isEnabled = taxBoxConfig?.get(TaxBoxConfigs.enabled) ?: false

                TaxBoxData(plan, isEnabled, balance)
            }

            if (!data.plan.taxBox) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.NotPremium),
                        Constants.ERROR
                    )
                }
                return
            }

            if (!data.isEnabled) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.NotEnabled),
                        Constants.ERROR
                    )
                }
                return
            }

            context.reply(false) {
                this.useComponentsV2 = true

                container {
                    this.accentColorRaw = LorittaColors.LorittaAqua.rgb

                    this.section(Thumbnail("https://stuff.loritta.website/loritta-sonhos-glasses-yafyr.gif")) {
                        this.text(
                            buildString {
                                appendLine("## \uD83D\uDCE6 ${context.i18nContext.get(I18N_PREFIX.Title)}")

                                if (data.balance > 0) {
                                    appendLine(context.i18nContext.get(I18N_PREFIX.Balance(data.balance)))
                                } else {
                                    appendLine(context.i18nContext.get(I18N_PREFIX.Empty))
                                }

                                appendLine()

                                appendLine("-# " + context.i18nContext.get(I18N_PREFIX.Explanation(loritta.commandMentions.emojiFightStart, loritta.commandMentions.coinflipBet, loritta.commandMentions.minesPlay, loritta.commandMentions.blackjackPlay)))
                            }
                        )
                    }
                }

                actionRow(
                    loritta.interactivityManager
                        .buttonForUser(
                            context.user,
                            context.alwaysEphemeral,
                            ButtonStyle.PRIMARY,
                            context.i18nContext.get(I18N_PREFIX.WithdrawButton),
                            {
                                loriEmoji = SonhosUtils.getSonhosEmojiOfQuantity(data.balance)
                                disabled = 0L >= data.balance
                            }
                        ) { withdrawContext ->
                            withdrawContext.deferChannelMessage(false)

                            val withdrawResult = loritta.transaction {
                                val profile = Profile.findById(withdrawContext.user.idLong) ?: return@transaction WithdrawResult.Empty // Shouldn't happen but just in case

                                // Re-validate premium
                                val donationKeys = DonationKeys.selectAll()
                                    .where {
                                        DonationKeys.activeIn eq guildId and (DonationKeys.expiresAt greaterEq OffsetDateTime.now(Constants.LORITTA_TIMEZONE).toInstant().toEpochMilli())
                                    }
                                    .toList()

                                val value = donationKeys.sumOf { it[DonationKeys.value] }
                                val plan = ServerPremiumPlan.getPlanFromValue(value)

                                if (!plan.taxBox) {
                                    return@transaction WithdrawResult.NotPremium
                                }

                                // Re-validate enabled
                                val taxBoxConfig = TaxBoxConfigs.selectAll()
                                    .where { TaxBoxConfigs.id eq guildId and (TaxBoxConfigs.enabled eq true) }
                                    .firstOrNull()
                                    ?: return@transaction WithdrawResult.NotEnabled

                                // Get current balance
                                val taxBox = TaxBoxes.selectAll()
                                    .where { TaxBoxes.id eq guildId }
                                    .firstOrNull()

                                val currentBalance = taxBox?.get(TaxBoxes.sonhos) ?: 0L

                                if (0L >= currentBalance)
                                    return@transaction WithdrawResult.Empty

                                // Zero out the tax box
                                TaxBoxes.update({ TaxBoxes.id eq guildId }) {
                                    it[TaxBoxes.sonhos] = 0L
                                }

                                profile.addSonhosNested(
                                    currentBalance,
                                    refreshBeforeAction = false,
                                    refreshOnSuccess = false
                                )

                                // Log the transaction
                                SimpleSonhosTransactionsLogUtils.insert(
                                    withdrawContext.user.idLong,
                                    Instant.now(),
                                    TransactionType.TAX_BOX_WITHDRAW,
                                    currentBalance,
                                    StoredTaxBoxWithdrawTransaction(guildId = guildId)
                                )

                                WithdrawResult.Success(currentBalance)
                            }

                            when (withdrawResult) {
                                is WithdrawResult.NotPremium -> {
                                    withdrawContext.reply(true) {
                                        styled(
                                            withdrawContext.i18nContext.get(I18N_PREFIX.NotPremium),
                                            Constants.ERROR
                                        )
                                    }
                                }
                                is WithdrawResult.NotEnabled -> {
                                    withdrawContext.reply(true) {
                                        styled(
                                            withdrawContext.i18nContext.get(I18N_PREFIX.NotEnabled),
                                            Constants.ERROR
                                        )
                                    }
                                }
                                is WithdrawResult.Empty -> {
                                    withdrawContext.reply(true) {
                                        styled(
                                            withdrawContext.i18nContext.get(I18N_PREFIX.WithdrawEmpty),
                                            Constants.ERROR
                                        )
                                    }
                                }
                                is WithdrawResult.Success -> {
                                    withdrawContext.reply(false) {
                                        styled(
                                            withdrawContext.i18nContext.get(I18N_PREFIX.WithdrawSuccess(withdrawResult.amount)),
                                            "\uD83D\uDCE6"
                                        )
                                    }
                                }
                            }
                        }
                )
            }
        }

        override suspend fun convertToInteractionsArguments(context: LegacyMessageCommandContext, args: List<String>): Map<OptionReference<*>, Any?>? = LorittaLegacyMessageCommandExecutor.NO_ARGS
    }

    private data class TaxBoxData(
        val plan: ServerPremiumPlan,
        val isEnabled: Boolean,
        val balance: Long
    )

    private sealed class WithdrawResult {
        data object NotPremium : WithdrawResult()
        data object NotEnabled : WithdrawResult()
        data object Empty : WithdrawResult()
        data class Success(val amount: Long) : WithdrawResult()
    }
}
