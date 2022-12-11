package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay

import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.toMessageFormat
import dev.kord.core.entity.User
import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.SlashContextHighLevelEditableMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.loriEmoji
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.ShortenedToLongSonhosAutocompleteExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils.appendUserHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.i18n.I18nKeysData
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class PayExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = user("user", SonhosCommand.PAY_I18N_PREFIX.Options.User.Text)
        val quantity = string("quantity", SonhosCommand.PAY_I18N_PREFIX.Options.Quantity.Text) {
            autocomplete(ShortenedToLongSonhosAutocompleteExecutor(loritta))
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

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        val receiver = args[options.user]
        val howMuch = args[options.quantity].toLongOrNull()
        val ttlDuration = args[options.ttlDuration]?.let { Duration.parse(it) } ?: 15.minutes
        val isLoritta = receiver.id == loritta.config.loritta.discord.applicationId

        checkIfSelfAccountIsOldEnough(context)
        checkIfOtherAccountIsOldEnough(context, receiver)
        // checkIfSelfAccountGotDailyRecently(context)

        // Too small
        if (howMuch == null || howMuch == 0L)
            context.failEphemerally(
                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TryingToTransferZeroSonhos),
                Emotes.LoriHmpf
            )

        if (0L > howMuch)
            context.failEphemerally(
                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TryingToTransferLessThanZeroSonhos),
                Emotes.LoriHmpf
            )

        if (context.user.id == receiver.id)
            context.failEphemerally(
                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.CantTransferToSelf),
                Emotes.Error
            )
        
        if (UserUtils.handleIfUserIsBanned(loritta, context, receiver))
            return

        // All prelimary checks have passed, let's defer!
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val userProfile = loritta.pudding.users.getUserProfile(UserId(context.user.id))

        if (userProfile == null || howMuch > userProfile.money) {
            context.fail {
                styled(
                    context.i18nContext.get(SonhosUtils.insufficientSonhos(userProfile, howMuch)),
                    Emotes.LoriSob
                )

                appendUserHaventGotDailyTodayOrUpsellSonhosBundles(
                    loritta,
                    context.i18nContext,
                    UserId(context.user.id),
                    "pay",
                    "transfer-not-enough-sonhos"
                )
            }
        }

        // TODO: Loritta is grateful easter egg
        // Easter Eggs
        val quirkyMessage = when {
            howMuch >= 500_000 -> context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.RandomQuirkyRichMessages).random()
            // tellUserLorittaIsGrateful -> context.locale.getList("commands.command.pay.randomLorittaIsGratefulMessages").random()
            else -> null
        }

        // We WANT to store on the database due to two things:
        // 1. We want to control the interaction TTL
        // 2. We want to block duplicate transactions by buttom spamming (with this, we can block this on transaction level)
        val nowPlusTimeToLive = Clock.System.now() + ttlDuration

        val (interactionDataId, data, encodedData) = context.loritta.encodeDataForComponentOnDatabase(
            TransferSonhosData(
                receiver.id,
                context.user.id,
                howMuch
            ),
            ttl = ttlDuration
        )

        val message = context.sendMessage {
            styled(
                buildString {
                    append(context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.YouAreGoingToTransfer(howMuch, mentionUser(receiver))))
                    if (quirkyMessage != null) {
                        append(" ")
                        append(quirkyMessage)
                    }
                },
                Emotes.LoriRich
            )

            styled(
                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.ConfirmTheTransaction(mentionUser(receiver), nowPlusTimeToLive.toMessageFormat(DiscordTimestampStyle.LongDateTime), nowPlusTimeToLive.toMessageFormat(DiscordTimestampStyle.RelativeTime))),
                Emotes.LoriZap
            )

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.AcceptTransfer),
                    TransferSonhosButtonExecutor,
                    encodedData
                ) {
                    loriEmoji = Emotes.Handshake
                }

                interactiveButton(
                    ButtonStyle.Danger,
                    context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.Cancel),
                    CancelSonhosTransferButtonExecutor,
                    context.loritta.encodeDataForComponentOrStoreInDatabase(
                        CancelSonhosTransferData(
                            context.user.id,
                            interactionDataId
                        )
                    )
                ) {
                    loriEmoji = Emotes.LoriHmpf
                }
            }
        }

        if (isLoritta) {
            // If it is Loritta, we will mimick that she is *actually* accepting the bet!
            TransferSonhosButtonExecutor.acceptSonhos(
                loritta,
                context,
                loritta.config.loritta.discord.applicationId,
                SlashContextHighLevelEditableMessage(message),
                data
            )
        }
    }

    private suspend fun checkIfSelfAccountGotDailyRecently(context: ApplicationCommandContext) {
        val now = Clock.System.now()

        // Check if the user got daily in the last 14 days before allowing a transaction
        val gotDailyRewardInTheLastXDays = context.loritta.pudding.sonhos.getUserLastDailyRewardReceived(
            UserId(context.user.id),
            now - 14.days
        ) != null

        if (!gotDailyRewardInTheLastXDays)
            context.failEphemerally(
                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.SelfAccountNeedsToGetDaily(loritta.commandMentions.daily)),
                Emotes.LoriSob
            )
    }

    private fun checkIfSelfAccountIsOldEnough(context: ApplicationCommandContext) {
        val now = Clock.System.now()
        val timestamp = context.user.id.timestamp
        val allowedAfterTimestamp = timestamp + (14.days)

        if (allowedAfterTimestamp > now) // 14 dias
            context.failEphemerally(
                context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.SelfAccountIsTooNew(allowedAfterTimestamp.toMessageFormat(DiscordTimestampStyle.LongDateTime), allowedAfterTimestamp.toMessageFormat(DiscordTimestampStyle.RelativeTime))),
                Emotes.LoriSob
            )
    }

    private fun checkIfOtherAccountIsOldEnough(context: ApplicationCommandContext, target: User) {
        val now = Clock.System.now()
        val timestamp = target.id.timestamp
        val allowedAfterTimestamp = timestamp + (7.days)

        if (timestamp + (7.days) > now) // 7 dias
            context.failEphemerally {
                styled(
                    context.i18nContext.get(
                        SonhosCommand.PAY_I18N_PREFIX.OtherAccountIsTooNew(
                            mentionUser(target),
                            allowedAfterTimestamp.toMessageFormat(DiscordTimestampStyle.LongDateTime),
                            allowedAfterTimestamp.toMessageFormat(DiscordTimestampStyle.RelativeTime)
                        )
                    ),
                    Emotes.LoriSob
                )
            }
    }
}