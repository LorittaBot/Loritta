package net.perfectdreams.loritta.commands.vanilla.economy

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneId

class TransactionsCommand : LorittaCommand(arrayOf("transactions", "transações"), category = CommandCategory.ECONOMY) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.economy.transactions.description"]
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        val user = context.getUserAt(0) ?: context.userHandle

        val transactions = transaction(Databases.loritta) {
            SonhosTransaction.select {
                SonhosTransaction.givenBy eq user.idLong or (SonhosTransaction.receivedBy eq user.idLong)
            }.orderBy(SonhosTransaction.givenAt, SortOrder.DESC)
                    .limit(20)
                    .toMutableList()
        }

        val description = buildString {
            for (transaction in transactions) {
                val receivedSonhos = transaction[SonhosTransaction.receivedBy] == user.idLong

                val givenAtTime = Instant.ofEpochMilli(transaction[SonhosTransaction.givenAt])
                        .atZone(ZoneId.systemDefault())

                val day = givenAtTime.dayOfMonth.toString().padStart(2, '0')
                val month = givenAtTime.monthValue.toString().padStart(2, '0')
                val year = givenAtTime.year

                val hour = givenAtTime.hour.toString().padStart(2, '0')
                val minute = givenAtTime.minute.toString().padStart(2, '0')

                this.append("`[$day/$month/$year $hour:$minute]` ")

                val emoji = if (receivedSonhos)
                    "\uD83D\uDCB5"
                else
                    "\uD83D\uDCB8"

                this.append(emoji)

                this.append(" ")

                if (transaction[SonhosTransaction.reason] == SonhosPaymentReason.PAYMENT) {
                    val receivedByUser = if (receivedSonhos) {
                        lorittaShards.retrieveUserById(transaction[SonhosTransaction.givenBy])
                    } else {
                        lorittaShards.retrieveUserById(transaction[SonhosTransaction.receivedBy])
                    }

                    val name = (receivedByUser?.name + "#" + receivedByUser?.discriminator)

                    if (receivedSonhos) {
                        this.append(locale["commands.economy.transactions.receivedMoneySonhos", transaction[SonhosTransaction.quantity], "`$name`"])
                    } else {
                        this.append(locale["commands.economy.transactions.sentMoneySonhos", transaction[SonhosTransaction.quantity], "`$name`"])
                    }
                } else if (transaction[SonhosTransaction.reason] == SonhosPaymentReason.PAYMENT_TAX) {
                    this.append(locale["commands.economy.transactions.sentMoneySonhosTax", transaction[SonhosTransaction.quantity]])
                } else {
                    val type = transaction[SonhosTransaction.reason].name
                            .toLowerCase()
                            .replace("_", " ")
                            .split(" ")
                            .map {
                                it.capitalize()
                            }
                            .joinToString("")
                            .toCharArray().apply {
                                this[0] = this[0].toLowerCase()
                            }
                            .joinToString("")

                    val genericTypeName = locale["commands.economy.transactions.types.${type}"]

                    if (receivedSonhos)
                        this.append(locale["commands.economy.transactions.genericReceived", transaction[SonhosTransaction.quantity], genericTypeName])
                    else
                        this.append(locale["commands.economy.transactions.genericSent", transaction[SonhosTransaction.quantity], genericTypeName])
                }
                this.append("\n")
            }
        }

        val embed = EmbedBuilder()
                .setTitle(
                        "${Emotes.LORI_RICH} " +
                                if (user != context.userHandle)
                                    locale["commands.economy.transactions.otherUserTransactions", user.asTag]
                                else
                                    locale["commands.economy.transactions.title"]
                )
                .setColor(Constants.LORITTA_AQUA)
                .setDescription(description)

        context.sendMessage(context.getAsMention(true), embed.build())
    }
}