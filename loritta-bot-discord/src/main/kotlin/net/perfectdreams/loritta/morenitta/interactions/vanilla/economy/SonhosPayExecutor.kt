package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.Dailies
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransferRequests
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.AccountUtils
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.NumberUtils
import net.perfectdreams.loritta.morenitta.utils.VacationModeUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class SonhosPayExecutor(private val loritta: LorittaBot) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    companion object {
        const val SONHOS_TRANSFER_ACCEPT_COMPONENT_PREFIX = "sonhos_transfer_accept"
        val TIME_TO_LIVE_RANGE = 1.minutes.inWholeMilliseconds..7.days.inWholeMilliseconds

        // These are "generic" checks that are used for the transfer sonhos route, to avoid duplicate code that will cause headaches if not used in both calls
        fun checkIfAccountIsOldEnoughToSendSonhos(targetUserId: UserSnowflake) = checkIfAccountIsOldEnough(targetUserId, 14.days)
        fun checkIfAccountIsOldEnoughToReceiveSonhos(targetUserId: UserSnowflake) = checkIfAccountIsOldEnough(targetUserId, 7.days)

        fun checkIfAccountIsOldEnough(targetUserId: UserSnowflake, duration: Duration): OtherAccountOldEnoughResult {
            val now = Clock.System.now()
            val timestamp = targetUserId.timeCreated.toInstant().toKotlinInstant()
            val allowedAfterTimestamp = timestamp + duration

            if (allowedAfterTimestamp > now)
                return OtherAccountOldEnoughResult.NotOldEnough(allowedAfterTimestamp)

            return OtherAccountOldEnoughResult.Success
        }

        suspend fun checkIfAccountGotDailyAtLeastOnce(loritta: LorittaBot, targetUserId: UserSnowflake): AccountGotDailyAtLeastOnceResult {
            val gotDailyAtLeastOnce = loritta.transaction {
                Dailies.selectAll()
                    .where {
                        Dailies.receivedById eq targetUserId.idLong
                    }
                    .count() != 0L
            }

            if (!gotDailyAtLeastOnce)
                return AccountGotDailyAtLeastOnceResult.HaventGotDailyOnce

            return AccountGotDailyAtLeastOnceResult.Success
        }

        /**
         * @param i18nContext             the i18nContext that will be used in this message
         * @param receiverId              who will receive the sonhos
         * @param howMuch                 how much sonhos are being transferred
         * @param nowPlusTimeToLive       the TTL of the transaction
         * @param sonhosTransferRequestId the Database Sonhos Transfer Request ID
         * @param acceptedQuantity        how many users have already accepted the transfer
         */
        fun createSonhosTransferMessage(
            loritta: LorittaBot,
            i18nContext: I18nContext,
            receiverId: UserSnowflake,
            howMuch: Long,
            nowPlusTimeToLive: Instant,
            sonhosTransferRequestId: Long,
            acceptedQuantity: Int
        ): InlineMessage<MessageCreateData>.() -> (Unit) {
            // TODO: Loritta is grateful easter egg
            // Easter Eggs
            val quirkyMessage = when {
                howMuch >= 500_000 -> i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.RandomQuirkyRichMessages).random()
                // tellUserLorittaIsGrateful -> context.locale.getList("commands.command.pay.randomLorittaIsGratefulMessages").random()
                else -> null
            }

            return {
                // Allow mentioning the receiver
                mentions {
                    user(receiverId)
                }

                styled(
                    buildString {
                        append(
                            i18nContext.get(
                                SonhosCommand.PAY_I18N_PREFIX.YouAreGoingToTransfer(
                                    howMuch,
                                    receiverId.asMention
                                )
                            )
                        )
                        if (quirkyMessage != null) {
                            append(" ")
                            append(quirkyMessage)
                        }
                    },
                    Emotes.LoriRich
                )

                styled(
                    i18nContext.get(
                        SonhosCommand.PAY_I18N_PREFIX.ConfirmTheTransaction(
                            receiverId.asMention,
                            TimeFormat.DATE_TIME_LONG.format(nowPlusTimeToLive),
                            TimeFormat.RELATIVE.format(nowPlusTimeToLive)
                        )
                    ),
                    Emotes.LoriZap
                )

                styled(
                    i18nContext.get(
                        SonhosCommand.PAY_I18N_PREFIX.SellDisallowedWarning(
                            loritta.config.loritta.website.url + "guidelines"
                        )
                    ),
                    Emotes.LoriZap
                )

                // Because we support expiration dates, we need to do this differently because we must persist the pay between restarts!!
                actionRow(
                    Button.of(
                        ButtonStyle.PRIMARY,
                        "$SONHOS_TRANSFER_ACCEPT_COMPONENT_PREFIX:${sonhosTransferRequestId}",
                        i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.AcceptTransfer(acceptedQuantity)),
                        Emotes.Handshake.toJDA()
                    )
                )
            }
        }

        sealed class OtherAccountOldEnoughResult {
            data object Success : OtherAccountOldEnoughResult()
            data class NotOldEnough(val allowedAfterTimestamp: kotlinx.datetime.Instant) : OtherAccountOldEnoughResult()
        }

        sealed class AccountGotDailyAtLeastOnceResult {
            data object Success : AccountGotDailyAtLeastOnceResult()
            data object HaventGotDailyOnce : AccountGotDailyAtLeastOnceResult()
        }
    }

    inner class Options : ApplicationCommandOptions() {
        val user = string("user", SonhosCommand.PAY_I18N_PREFIX.Options.User.Text)
        val quantity = string("quantity", SonhosCommand.PAY_I18N_PREFIX.Options.Quantity.Text) {
            // autocomplete(ShortenedToLongSonhosAutocompleteExecutor(loritta))
        }
        val autoAccept = optionalBoolean("auto_accept", SonhosCommand.PAY_I18N_PREFIX.Options.AutoAccept.Text) {
        }
        val ttlDuration = optionalString("expires_after", SonhosCommand.PAY_I18N_PREFIX.Options.ExpiresAfter.Text) {
            choice(I18nKeysData.Time.Minutes(1), "1m")
            choice(I18nKeysData.Time.Minutes(5), "5m")
            choice(I18nKeysData.Time.Minutes(15), "15m")
            choice(I18nKeysData.Time.Hours(1), "1h")
            choice(I18nKeysData.Time.Hours(6), "6h")
            choice(I18nKeysData.Time.Hours(12), "12h")
            choice(I18nKeysData.Time.Hours(24), "24h")
            choice(I18nKeysData.Time.Days(3), "3d")
            choice(I18nKeysData.Time.Days(7), "7d")
        }
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        if (SonhosUtils.checkIfEconomyIsDisabled(context))
            return

        context.deferChannelMessage(false)

        val userProfile = context.lorittaUser.profile

        val users = checkAndRetrieveAllValidUsersFromString(context, args[options.user])
        val howMuch = NumberUtils.convertShortenedNumberOrUserSonhosSpecificToLong(args[options.quantity], userProfile.money)
        val autoAccept = args[options.autoAccept] ?: false
        val ttlDuration = args[options.ttlDuration]?.let { Duration.parse(it) } ?: 15.minutes

        if (users.isEmpty()) {
            context.reply(false) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.NoValidUserFound),
                    Emotes.LoriSob
                )
            }
            return
        }

        // Too small
        if (howMuch == null || howMuch == 0L) {
            context.reply(false) {
                styled(
                    context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TryingToTransferZeroSonhos),
                    Emotes.LoriHmpf
                )
            }
            return
        }

        if (0L > howMuch) {
            context.reply(false) {
                styled(
                    context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TryingToTransferLessThanZeroSonhos),
                    Emotes.LoriHmpf
                )
            }
            return
        }

        if ((howMuch * users.size) > userProfile.money) {
            context.reply(false) {
                styled(
                    context.i18nContext.get(SonhosUtils.insufficientSonhos(userProfile.money, howMuch)),
                    Emotes.LoriSob
                )

                appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                    loritta,
                    context.i18nContext,
                    net.perfectdreams.loritta.serializable.UserId(context.user.idLong),
                    "pay",
                    "transfer-not-enough-sonhos"
                )
            }
            return
        }

        checkIfSelfAccountIsOldEnough(context)
        if (VacationModeUtils.checkIfWeAreOnVacation(context, false))
            return

        val now = Instant.now()
        for (receiver in users) {
            val isLoritta = receiver.idLong == context.jda.selfUser.idLong

            checkIfOtherAccountIsOldEnough(context, receiver)
            checkIfSelfAccountGotDailyAtLeastOnce(context)
            if (!isLoritta)
                checkIfOtherAccountGotDailyAtLeastOnce(context, receiver)

            if (context.user.idLong == receiver.idLong) {
                context.reply(false) {
                    styled(
                        context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.CantTransferToSelf),
                        Emotes.Error
                    )
                }
                continue
            }

            if (AccountUtils.checkAndSendMessageIfUserIsBanned(loritta, context, receiver))
                continue
            if (VacationModeUtils.checkIfUserIsOnVacation(context, receiver, false))
                continue

            // All preliminary checks have passed! Let's create a sonhos transfer request

            // We WANT to store on the database due to two things:
            // 1. We want to control the interaction TTL
            // 2. We want to block duplicate transactions by buttom spamming (with this, we can block this on transaction level)
            val nowPlusTimeToLive = now.plusMillis(ttlDuration.inWholeMilliseconds)

            var acceptedQuantity = 0
            if (autoAccept)
                acceptedQuantity++
            if (isLoritta)
                acceptedQuantity++

            val sonhosTransferRequestId = loritta.transaction {
                SonhosTransferRequests.insertAndGetId {
                    it[giver] = context.user.idLong
                    it[SonhosTransferRequests.receiver] = receiver.idLong
                    it[quantity] = howMuch
                    it[requestedAt] = now
                    it[expiresAt] = nowPlusTimeToLive

                    // TODO: This causes a bug: If the user is auto accepting + Loritta automatically accepts the button, the button will be 2/2 but the transaction will not automatically go thru
                    if (autoAccept)
                        it[giverAcceptedAt] = now

                    // If it is Loritta, we will mimick that she is *actually* accepting the transfer!
                    if (isLoritta)
                        it[receiverAcceptedAt] = now
                }
            }

            context.reply(false) {
                val message = createSonhosTransferMessage(
                    context.loritta,
                    context.i18nContext,
                    receiver,
                    howMuch,
                    nowPlusTimeToLive,
                    sonhosTransferRequestId.value,
                    acceptedQuantity
                )

                message.invoke(this)
            }
        }
    }

    private fun checkIfSelfAccountIsOldEnough(context: UnleashedContext) {
        when (val result = checkIfAccountIsOldEnoughToSendSonhos(context.user)) {
            is OtherAccountOldEnoughResult.NotOldEnough -> {
                context.fail(false) {
                    styled(
                        context.i18nContext.get(
                            SonhosCommand.PAY_I18N_PREFIX.SelfAccountIsTooNew(
                                TimeFormat.DATE_TIME_LONG.format(result.allowedAfterTimestamp.toJavaInstant()),
                                TimeFormat.RELATIVE.format(result.allowedAfterTimestamp.toJavaInstant())
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            }
            OtherAccountOldEnoughResult.Success -> {}
        }
    }

    private suspend fun checkIfSelfAccountGotDailyAtLeastOnce(context: UnleashedContext) {
        val result = checkIfAccountGotDailyAtLeastOnce(context.loritta, UserSnowflake.fromId(context.user.idLong))

        when (result) {
            AccountGotDailyAtLeastOnceResult.HaventGotDailyOnce -> {
                context.fail(false) {
                    styled(
                        context.i18nContext.get(
                            SonhosCommand.PAY_I18N_PREFIX.SelfAccountNeedsToGetDaily(loritta.commandMentions.daily)
                        ),
                        Emotes.LoriSob
                    )
                }
            }
            AccountGotDailyAtLeastOnceResult.Success -> {}
        }
    }

    private suspend fun checkIfOtherAccountGotDailyAtLeastOnce(context: UnleashedContext, target: User) {
        val result = checkIfAccountGotDailyAtLeastOnce(context.loritta, target)

        when (result) {
            AccountGotDailyAtLeastOnceResult.HaventGotDailyOnce -> {
                context.fail(false) {
                    styled(
                        context.i18nContext.get(
                            SonhosCommand.PAY_I18N_PREFIX.OtherAccountNeedsToGetDaily(
                                target.asMention,
                                loritta.commandMentions.daily
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            }
            AccountGotDailyAtLeastOnceResult.Success -> {}
        }
    }

    private fun checkIfOtherAccountIsOldEnough(context: UnleashedContext, target: User) {
        when (val result = checkIfAccountIsOldEnoughToReceiveSonhos(target)) {
            is OtherAccountOldEnoughResult.NotOldEnough -> {
                context.fail(false) {
                    styled(
                        context.i18nContext.get(
                            SonhosCommand.PAY_I18N_PREFIX.OtherAccountIsTooNew(
                                target.asMention,
                                TimeFormat.DATE_TIME_LONG.format(result.allowedAfterTimestamp.toJavaInstant()),
                                TimeFormat.RELATIVE.format(result.allowedAfterTimestamp.toJavaInstant())
                            )
                        ),
                        Emotes.LoriSob
                    )
                }
            }
            OtherAccountOldEnoughResult.Success -> {}
        }
    }

    private suspend fun checkAndRetrieveAllValidUsersFromString(context: UnleashedContext, usersAsString: String): List<User> {
        val split = usersAsString.split(" ")
        var matchedCount = 0

        val validUsers = mutableListOf<User>()
        for (input in split) {
            // We don't want to query via other means, this would cause issues with Loritta detecting users as messages
            val shouldUseExtensiveMatching = validUsers.isEmpty()

            val matchedUser = DiscordUtils.extractUserFromString(
                context.loritta,
                input,
                context.mentions.users,
                context.guildOrNull,
                extractUserViaEffectiveName = shouldUseExtensiveMatching,
                extractUserViaUsername = shouldUseExtensiveMatching
            )

            if (matchedUser != null) {
                matchedCount++
                validUsers.add(matchedUser)
            }
            else break
        }

        return validUsers
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        if (2 > args.size) {
            context.explain()
            return null
        }

        val users = args.dropLast(1)
        val sonhos = args.last()

        return mapOf(
            options.user to users.joinToString(" "),
            options.quantity to sonhos
        )
    }
}
