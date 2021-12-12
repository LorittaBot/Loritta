package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.BrokerTickerInformation
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.avg
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

class BovespaBrokerService(private val pudding: Pudding) : Service(pudding) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Gets all ticker informations in the database
     *
     * @return a list of all tickers
     */
    suspend fun getAllTickers() = pudding.transaction {
        TickerPrices.selectAll()
            .map {
                BrokerTickerInformation(
                    it[TickerPrices.ticker].value,
                    it[TickerPrices.status],
                    it[TickerPrices.value],
                    it[TickerPrices.dailyPriceVariation],
                    it[TickerPrices.lastUpdatedAt].toKotlinInstant()
                )
            }
    }

    /**
     * Gets all ticker informations in the database
     *
     * @return a list of all tickers
     */
    suspend fun getUserBoughtStocks(userId: Long) = pudding.transaction {
        val stockCount = BoughtStocks.ticker.count()
        val sumPrice = BoughtStocks.price.sum()
        val averagePrice = BoughtStocks.price.avg()

        BoughtStocks
            .slice(BoughtStocks.ticker, stockCount, sumPrice, averagePrice)
            .select {
                BoughtStocks.user eq userId
            }.groupBy(BoughtStocks.ticker)
            .map {
                BrokerUserStockAssets(
                    it[BoughtStocks.ticker].value,
                    it[stockCount],
                    it[sumPrice]!!,
                    it[averagePrice]!!
                )
            }
    }

    suspend fun buyStockAsset(userId: Long, tickerId: String, quantity: Long) = pudding.transaction {
        val tickerInformation = TickerPrices.select { TickerPrices.ticker eq tickerId }
            .limit(1)
            .first()
            .let {
                BrokerTickerInformation(
                    it[TickerPrices.ticker].value,
                    it[TickerPrices.status],
                    it[TickerPrices.value],
                    it[TickerPrices.dailyPriceVariation],
                    it[TickerPrices.lastUpdatedAt].toKotlinInstant()
                )
            }

        val valueOfStock = tickerInformation.value

        // TODO: Better exceptions
        if (tickerInformation.status != "market")
            throw OutOfSessionException(tickerInformation.status)

        val userProfile = pudding.users.getUserProfile(UserId(userId)) ?: error("User does not have a profile!")
        val currentStockCount = BoughtStocks.select {
            BoughtStocks.user eq userProfile.id.value.toLong()
        }.count()

        // TODO: Correct max stock count
        if (quantity + currentStockCount > 10)
            throw TooManyStocksException(currentStockCount)

        val money = userProfile.money
        val howMuchValue = valueOfStock * quantity
        if (howMuchValue > money)
            throw NotEnoughSonhosException()

        logger.info { "User $userId is trying to buy $quantity $tickerId for $howMuchValue" }

        val now = System.currentTimeMillis()

        // By using shouldReturnGeneratedValues, the database won't need to synchronize on each insert
        // this increases insert performance A LOT and, because we don't need the IDs, it is very useful to make
        // stocks purchases be VERY fast
        BoughtStocks.batchInsert(0 until quantity, shouldReturnGeneratedValues = false) {
            this[BoughtStocks.user] = userId
            this[BoughtStocks.ticker] = tickerId
            this[BoughtStocks.price] = valueOfStock
            this[BoughtStocks.boughtAt] = now
        }

        // TODO: Update the money in another way
        Profiles.update({ Profiles.id eq userId }) {
            with(SqlExpressionBuilder) {
                it.update(Profiles.money, Profiles.money - howMuchValue)
            }
        }

        logger.info { "User $userId bought $quantity $tickerId for $howMuchValue" }
    }

    suspend fun sellStockAsset(userId: Long, tickerId: String, quantity: Long) = pudding.transaction {
        val tickerInformation = TickerPrices.select { TickerPrices.ticker eq tickerId }
            .limit(1)
            .first()
            .let {
                BrokerTickerInformation(
                    it[TickerPrices.ticker].value,
                    it[TickerPrices.status],
                    it[TickerPrices.value],
                    it[TickerPrices.dailyPriceVariation],
                    it[TickerPrices.lastUpdatedAt].toKotlinInstant()
                )
            }

        val selfStocks = BoughtStocks.select {
            BoughtStocks.user eq userId and (BoughtStocks.ticker eq tickerId)
        }.toList()

        // Proper exceptions
        if (quantity > selfStocks.size)
            throw NotEnoughStocksException(selfStocks.size)

        val stocksThatWillBeSold = selfStocks.take(quantity.toInt())

        // TODO: Move BrokerInfo to the common module and use it here
        val howMuchWillBePaidToTheUser = tickerInformation.value * quantity

        logger.info { "User $userId is trying to sell $quantity $tickerId for $howMuchWillBePaidToTheUser" }

        val totalEarnings = howMuchWillBePaidToTheUser - stocksThatWillBeSold.sumOf { it[BoughtStocks.price] }

        // The reason we batch the stocks in multiple queries is due to this issue:
        // https://github.com/LorittaBot/Loritta/issues/2343
        // https://stackoverflow.com/questions/49274390/postgresql-and-hibernate-java-io-ioexception-tried-to-send-an-out-of-range-inte
        stocksThatWillBeSold.chunked(32767).forEachIndexed { index, chunkedStocks ->
            BoughtStocks.deleteWhere {
                BoughtStocks.id inList chunkedStocks.map { it[BoughtStocks.id] }
            }
        }

        // TODO: Update the money in another way
        Profiles.update({ Profiles.id eq userId }) {
            with(SqlExpressionBuilder) {
                it.update(Profiles.money, Profiles.money + howMuchWillBePaidToTheUser)
            }
        }

        logger.info { "User $userId sold $quantity $tickerId for $howMuchWillBePaidToTheUser" }
    }

    data class BrokerUserStockAssets(
        val ticker: String,
        val count: Long,
        val sum: Long,
        val average: BigDecimal
    )

    class NotEnoughSonhosException : RuntimeException()
    class OutOfSessionException(val currentSession: String) : RuntimeException()
    class TooManyStocksException(val currentStockCount: Long) : RuntimeException()
    class NotEnoughStocksException(val currentBoughtStocksCount: Int) : RuntimeException()
}