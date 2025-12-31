package net.perfectdreams.loritta.morenitta.rpc.commands

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries.Lotteries
import net.perfectdreams.loritta.cinnamon.pudding.tables.lotteries.LotteryTickets
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.rpc.LorittaRPC
import net.perfectdreams.loritta.morenitta.rpc.payloads.BuyLotteryTicketResponse
import net.perfectdreams.loritta.morenitta.rpc.payloads.ViewLotteryStatusRequest
import net.perfectdreams.loritta.morenitta.rpc.payloads.ViewLotteryStatusResponse
import net.perfectdreams.loritta.morenitta.utils.Constants
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.OffsetDateTime

class ViewLotteryStatusRPCCommand(val loritta: LorittaBot) : LorittaRPCCommand(LorittaRPC.ViewLotteryStats) {
    override suspend fun onRequest(call: ApplicationCall) {
        val request = Json.decodeFromString<ViewLotteryStatusRequest>(call.receiveText())

        val result = loritta.lottery.mutex.withLock {
            loritta.transaction {
                val now = OffsetDateTime.now(Constants.LORITTA_TIMEZONE)

                // This lottery may be active or not
                val activeLottery = Lotteries.selectAll()
                    .where {
                        Lotteries.startedAt lessEq now
                    }
                    .orderBy(Lotteries.endsAt, SortOrder.DESC)
                    .firstOrNull()

                if (activeLottery == null)
                    return@transaction ViewLotteryStatusResponse.ThereIsntActiveLottery

                val howManyTicketsYouBought = LotteryTickets.selectAll()
                    .where {
                        LotteryTickets.userId eq request.userId and (LotteryTickets.lottery eq activeLottery[Lotteries.id])
                    }
                    .count()

                val totalTickets = LotteryTickets.selectAll()
                    .where {
                       LotteryTickets.lottery eq activeLottery[Lotteries.id]
                    }
                    .count()

                val usersParticipating = LotteryTickets.select(LotteryTickets.userId)
                    .where {
                        LotteryTickets.lottery eq activeLottery[Lotteries.id]
                    }
                    .groupBy(LotteryTickets.userId)
                    .count()

                return@transaction ViewLotteryStatusResponse.Success(
                    totalTickets,
                    activeLottery[Lotteries.ticketPrice],
                    activeLottery[Lotteries.houseSponsorship],
                    howManyTicketsYouBought,
                    usersParticipating,
                    activeLottery[Lotteries.tableTotalNumbers],
                    activeLottery[Lotteries.endsAt].toInstant().toKotlinInstant(),
                    if (activeLottery[Lotteries.endedAt] != null) {
                        ViewLotteryStatusResponse.Success.LotteryResults(
                            activeLottery[Lotteries.winningNumbers]!!,
                            activeLottery[Lotteries.hits]
                        )
                    } else null
                )
            }
        }

        call.respondRPCResponse(result)
    }
}