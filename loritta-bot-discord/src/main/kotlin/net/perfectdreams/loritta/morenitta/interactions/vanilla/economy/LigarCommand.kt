package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.LorittaBot.Companion.RANDOM
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import net.perfectdreams.loritta.serializable.StoredBomDiaECiaCallCalledTransaction
import net.perfectdreams.loritta.serializable.StoredBomDiaECiaCallWonTransaction
import org.jetbrains.exposed.sql.insert
import java.time.Instant
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class LigarCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    companion object {
        private val I18N_PREFIX = I18nKeysData.Commands.Command.Ligar
        val coroutineExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        private val logger = KotlinLogging.logger {}
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.ECONOMY, UUID.fromString("a1a03b03-b23d-43b8-93f5-0c714307220e")) {
        enableLegacyMessageSupport = true

        examples = I18N_PREFIX.Examples

        executor = LigarExecutor(loritta)
    }

    inner class LigarExecutor(val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val phoneAndText = string("phone_and_text", I18N_PREFIX.Options.PhoneAndText.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val phoneAndText = args[options.phoneAndText]
            val parts = phoneAndText.split(" ", limit = 2)
            val phoneNumber = parts.getOrNull(0)?.replace("-", "")
            val text = (parts.getOrNull(1) ?: "").lowercase()

            if (phoneNumber == "40028922") {
                val profile = context.lorittaUser.profile

                if (75 > profile.money) {
                    context.reply(false) {
                        styled(
                            context.i18nContext.get(I18N_PREFIX.InsufficientFunds),
                            Constants.ERROR
                        )
                        styled(
                            context.i18nContext.get(
                                GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                    "https://loritta.website/", // Hardcoded, woo
                                    "call",
                                    "yudi-is-sad-cuz-cant-call-him"
                                )
                            ),
                            Emotes.LoriRich
                        )
                    }
                    return
                }

                loritta.newSuspendedTransaction {
                    profile.takeSonhosAndAddToTransactionLogNested(
                        75,
                        SonhosPaymentReason.BOM_DIA_E_CIA
                    )

                    // Cinnamon transaction system
                    SimpleSonhosTransactionsLogUtils.insert(
                        profile.userId,
                        Instant.now(),
                        TransactionType.BOM_DIA_E_CIA,
                        75,
                        StoredBomDiaECiaCallCalledTransaction
                    )
                }

                GlobalScope.launch(coroutineExecutor) {
                    if (loritta.bomDiaECia.available) {
                        if (text.contains("\u200B") || text.contains("\u200C") || text.contains("\u200D")) {
                            context.reply(false) {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.Cheating),
                                    "<:yudi:446394608256024597>"
                                )
                            }
                            loritta.bomDiaECia.triedToCall.add(context.user.idLong)
                            return@launch
                        }

                        if (text != loritta.bomDiaECia.currentText) {
                            context.reply(false) {
                                styled(
                                    context.i18nContext.get(I18N_PREFIX.WrongText),
                                    "<:yudi:446394608256024597>"
                                )
                            }
                            loritta.bomDiaECia.triedToCall.add(context.user.idLong)
                            return@launch
                        }

                        loritta.bomDiaECia.available = false

                        val randomPrize = RANDOM.nextInt(5_000, 10_001)
                            .toLong()
                        val guild = context.guild
                        val user = context.user
                        val prizeAsBigDecimal = randomPrize.toBigDecimal()
                        val wonMillis = System.currentTimeMillis()

                        loritta.newSuspendedTransaction {
                            profile.addSonhosNested(randomPrize)

                            BomDiaECiaWinners.insert {
                                it[guildId] = guild.idLong
                                it[userId] = user.idLong
                                it[wonAt] = wonMillis
                                it[prize] = prizeAsBigDecimal
                            }

                            PaymentUtils.addToTransactionLogNested(
                                randomPrize,
                                SonhosPaymentReason.BOM_DIA_E_CIA,
                                receivedBy = context.user.idLong,
                                givenAtMillis = wonMillis
                            )

                            // Cinnamon transaction system
                            SimpleSonhosTransactionsLogUtils.insert(
                                profile.userId,
                                Instant.now(),
                                TransactionType.BOM_DIA_E_CIA,
                                randomPrize,
                                StoredBomDiaECiaCallWonTransaction
                            )
                        }

                        val wordsTyped = text.split(" ").size
                        val timeDiff = wonMillis - loritta.bomDiaECia.lastBomDiaECia
                        val wordsPerMinute = ((60 * wordsTyped) / (timeDiff / 1000)).toDouble()
                        val wpmAsInt = wordsPerMinute.roundToInt()

                        logger.info("${context.user.id} ganhou ${randomPrize} no Bom Dia & Cia!")
                        logger.info("Demorou ${timeDiff}ms a acertar o Bom Dia & Cia, num total aproximado de ${wpmAsInt} palavras por minuto!")

                        context.reply(false) {
                            styled(
                                context.i18nContext.get(
                                    I18N_PREFIX.YouWon(prizeAmount = randomPrize)
                                ),
                                "<:yudi:446394608256024597>"
                            )

                            styled(
                                context.i18nContext.get(
                                    I18N_PREFIX.YouWonWpm(
                                        wordsPerMinute = wpmAsInt,
                                        loriYayEmoji = "<a:lori_yay_wobbly:638040459721310238>"
                                    )
                                ),
                                Emotes.LoriLurk
                            )
                        }

                        loritta.bomDiaECia.announceWinner(context.channel as GuildMessageChannelUnion, context.guild, context.user)
                    } else {
                        context.reply(false) {
                            styled(
                                context.i18nContext.get(
                                    I18N_PREFIX.NotAvailable(
                                        botMention = context.jda.selfUser.asMention
                                    )
                                ),
                                "<:yudi:446394608256024597>"
                            )
                        }
                        if (30000 > System.currentTimeMillis() - loritta.bomDiaECia.lastBomDiaECia)
                            loritta.bomDiaECia.triedToCall.add(context.user.idLong)
                    }
                }
            } else {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(I18N_PREFIX.UnknownNumber),
                        "\uD83D\uDCF4"
                    )
                }
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.phoneAndText to args.joinToString(" ")
            )
        }
    }
}
