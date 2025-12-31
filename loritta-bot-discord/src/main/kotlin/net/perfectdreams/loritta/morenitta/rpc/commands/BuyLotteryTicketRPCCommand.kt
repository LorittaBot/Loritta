package net.perfectdreams.loritta.morenitta.rpc.commands

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries.Lotteries
import net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries.LotteryTicketNumbers
import net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries.LotteryTickets
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.payloads.BuyLotteryTicketRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.BuyLotteryTicketResponse
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.serializable.StoredLotteryRewardTransaction
import net.perfectdreams.loritta.serializable.StoredLotteryTicketsTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime

class BuyLotteryTicketRPCCommand(val loritta: LorittaBot) : LorittaRPCCommand(LorittaRPC.BuyLotteryTicket) {
    override suspend fun onRequest(call: ApplicationCall) {
        val request = Json.decodeFromString<BuyLotteryTicketRequest>(call.receiveText())

        val distinctNumbers = request.numbers.distinct()

        if (distinctNumbers.size != request.numbers.size) {
            call.respondRPCResponse<BuyLotteryTicketResponse>(BuyLotteryTicketResponse.RepeatedNumbers)
            return
        }

        val result = loritta.lottery.mutex.withLock {
            loritta.transaction {
                val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

                val activeLottery = Lotteries.selectAll()
                    .where {
                        Lotteries.startedAt lessEq now and (Lotteries.endsAt greaterEq now)
                    }
                    .firstOrNull()

                if (activeLottery == null)
                    return@transaction BuyLotteryTicketResponse.ThereIsntActiveLottery

                if (request.numbers.size != activeLottery[Lotteries.numbersPerTicket])
                    return@transaction BuyLotteryTicketResponse.IncorrectNumberCount(activeLottery[Lotteries.numbersPerTicket])

                for (number in request.numbers) {
                    if (number !in 1..activeLottery[Lotteries.tableTotalNumbers])
                        return@transaction BuyLotteryTicketResponse.InvalidNumbers(activeLottery[Lotteries.tableTotalNumbers])
                }

                val ticketsWithSameNumbers = LotteryTicketNumbers
                    .innerJoin(LotteryTickets)
                    .select(LotteryTicketNumbers.ticket, LotteryTickets.lottery)
                    .where {
                        LotteryTickets.lottery eq activeLottery[Lotteries.id] and (LotteryTicketNumbers.number inList request.numbers) and (LotteryTickets.userId eq request.userId)
                    }
                    .groupBy(LotteryTicketNumbers.ticket, LotteryTickets.lottery)
                    .having { LotteryTicketNumbers.number.count() eq activeLottery[Lotteries.numbersPerTicket].toLong() }
                    .map { it[LotteryTicketNumbers.ticket].value }
                    .toList()

                if (ticketsWithSameNumbers.isNotEmpty())
                    return@transaction BuyLotteryTicketResponse.AlreadyBettedWithTheseNumbers

                when (val sonhosResult = SonhosUtils.checkIfUserHasEnoughSonhos(request.userId, activeLottery[Lotteries.ticketPrice])) {
                    is SonhosUtils.SonhosCheckResult.NotEnoughSonhos -> {
                        return@transaction BuyLotteryTicketResponse.NotEnoughSonhos(sonhosResult.balance)
                    }
                    SonhosUtils.SonhosCheckResult.Success -> {
                        // We NEED to store the numbers in separate tables, to be able to query "hey, which users have bet on specific numbers?"
                        val lotteryTicket = LotteryTickets.insert {
                            it[LotteryTickets.userId] = request.userId
                            it[LotteryTickets.boughtAt] = now
                            it[LotteryTickets.lottery] = activeLottery[Lotteries.id]
                            it[LotteryTickets.winner] = false
                        }

                        val indexedEntries = request.numbers.mapIndexed { index, i -> index to i }

                        LotteryTicketNumbers.batchInsert(indexedEntries) {
                            this[LotteryTicketNumbers.ticket] = lotteryTicket[LotteryTickets.id]
                            this[LotteryTicketNumbers.index] = it.first
                            this[LotteryTicketNumbers.number] = it.second
                        }

                        Profiles.update({ Profiles.id eq request.userId }) {
                            it[Profiles.money] = Profiles.money - activeLottery[Lotteries.ticketPrice]
                        }

                        SimpleSonhosTransactionsLogUtils.insert(
                            request.userId,
                            now.toInstant(),
                            TransactionType.LOTTERY,
                            activeLottery[Lotteries.ticketPrice],
                            StoredLotteryTicketsTransaction(
                                activeLottery[Lotteries.id].value,
                                lotteryTicket[LotteryTickets.id].value
                            )
                        )

                        return@transaction BuyLotteryTicketResponse.Success(activeLottery[Lotteries.ticketPrice])
                    }
                }
            }
        }

        call.respondRPCResponse<BuyLotteryTicketResponse>(result)
    }
}