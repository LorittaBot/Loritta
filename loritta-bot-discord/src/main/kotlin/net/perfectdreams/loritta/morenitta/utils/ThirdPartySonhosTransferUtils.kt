package net.perfectdreams.loritta.morenitta.utils

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.components.button.Button
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.utils.TimeFormat
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosCommand
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import java.time.Instant

object ThirdPartySonhosTransferUtils {
    const val THIRD_PARTY_SONHOS_TRANSFER_ACCEPT_COMPONENT_PREFIX = "3p_sonhos_transfer_accept"

    /**
     * @param i18nContext             the i18nContext that will be used in this message
     * @param senderId                who wil send the sonhos
     * @param receiverId              who will receive the sonhos
     * @param howMuch                 how much sonhos are being transferred
     * @param nowPlusTimeToLive       the TTL of the transaction
     * @param sonhosTransferRequestId the Database Sonhos Transfer Request ID
     * @param acceptedQuantity        how many users have already accepted the transfer
     */
    fun createSonhosTransferMessageThirdPerson(
        i18nContext: I18nContext,
        senderId: UserSnowflake,
        receiverId: UserSnowflake,
        howMuch: Long,
        tax: Long,
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
                    if (tax != 0L) {
                        append(
                            i18nContext.get(
                                SonhosCommand.PAY_I18N_PREFIX.ThirdPartyIsGoingToTransfer(
                                    sonhos = howMuch,
                                    sender = senderId.asMention,
                                    receiver = receiverId.asMention,
                                    sonhosTaxQuantity = tax,
                                )
                            )
                        )
                    } else {
                        append(
                            i18nContext.get(
                                SonhosCommand.PAY_I18N_PREFIX.ThirdPartyIsGoingToTransferNoTax(
                                    sonhos = howMuch,
                                    sender = senderId.asMention,
                                    receiver = receiverId.asMention,
                                )
                            )
                        )
                    }

                    if (quirkyMessage != null) {
                        append(" ")
                        append(quirkyMessage)
                    }
                },
                Emotes.LoriRich
            )

            styled(
                i18nContext.get(
                    SonhosCommand.PAY_I18N_PREFIX.ThirdPersonConfirmTheTransaction(
                        senderId.asMention,
                        receiverId.asMention,
                        TimeFormat.DATE_TIME_LONG.format(nowPlusTimeToLive),
                        TimeFormat.RELATIVE.format(nowPlusTimeToLive)
                    )
                ),
                Emotes.LoriZap
            )

            // Because we support expiration dates, we need to do this differently because we must persist the pay between restarts!!
            actionRow(
                Button.of(
                    ButtonStyle.PRIMARY,
                    "$THIRD_PARTY_SONHOS_TRANSFER_ACCEPT_COMPONENT_PREFIX:${sonhosTransferRequestId}",
                    i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.AcceptTransfer(acceptedQuantity)),
                    Emotes.Handshake.toJDA()
                )
            )
        }
    }
}