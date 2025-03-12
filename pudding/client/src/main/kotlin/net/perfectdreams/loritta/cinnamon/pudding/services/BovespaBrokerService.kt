package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.utils.LorittaBovespaBrokerUtils
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.serializable.BrokerTickerInformation
import net.perfectdreams.loritta.serializable.StoredBrokerSonhosTransaction
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
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
        TickerPrices.selectAll().where { TickerPrices.enabled eq true }
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
     * Gets ticker [tickerId] information in the database
     *
     * @return a list of all tickers
     */
    suspend fun getTicker(tickerId: String) = getTickerOrNull(tickerId) ?: error("Ticker $tickerId is not present in the database!")

    /**
     * Gets ticker [tickerId] information in the database
     *
     * @return a list of all tickers
     */
    suspend fun getTickerOrNull(tickerId: String) = pudding.transaction {
        TickerPrices.selectFirstOrNull { TickerPrices.ticker eq tickerId }
            ?.let {
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
            .select(BoughtStocks.ticker, stockCount, sumPrice, averagePrice)
            .where {
                BoughtStocks.user eq userId
            }.groupBy(BoughtStocks.ticker)
            .map {
                BrokerUserStockShares(
                    it[BoughtStocks.ticker].value,
                    it[stockCount],
                    it[sumPrice]!!,
                    it[averagePrice]!!
                )
            }
    }

    /**
     * Buys [quantity] shares in the [tickerId] ticker in [userId]'s account
     *
     * @param userId   the user ID that is buying the assets
     * @param tickerId the ticker's ID
     * @param quantity how many assets are going to be bought
     * @throws StaleTickerDataException if the data is stale and shouldn't be relied on
     * @throws TooManySharesException   if the amount of stocks bought plus the user's current stock count will be more than [LorittaBovespaBrokerUtils.MAX_STOCK_SHARES_PER_USER]
     * @throws NotEnoughSonhosException if the user doesn't have enough sonhos to purchase the assets
     * @throws OutOfSessionException    if the ticker isn't active
     */
    suspend fun buyStockShares(userId: Long, tickerId: String, quantity: Long): BoughtSharesResponse {
        if (0 >= quantity)
            throw TransactionActionWithLessThanOneShareException()

        return pudding.transaction {
            val tickerInformation = getTicker(tickerId)

            val valueOfStock = LorittaBovespaBrokerUtils.convertToBuyingPrice(tickerInformation.value)

            checkIfTickerIsInactive(tickerInformation)
            checkIfTickerDataIsStale(tickerInformation)

            val userProfile = pudding.users.getUserProfile(UserId(userId)) ?: error("User does not have a profile!")
            val currentStockCount = BoughtStocks.selectAll().where {
                BoughtStocks.user eq userProfile.id.value.toLong()
            }.count()

            if (quantity + currentStockCount > LorittaBovespaBrokerUtils.MAX_STOCK_SHARES_PER_USER)
                throw TooManySharesException(currentStockCount)

            val money = userProfile.money
            val howMuchValue = valueOfStock * quantity
            if (howMuchValue > money)
                throw NotEnoughSonhosException(money, howMuchValue)

            logger.info { "User $userId is trying to buy $quantity $tickerId for $howMuchValue" }

            val now = Clock.System.now()

            // By using shouldReturnGeneratedValues, the database won't need to synchronize on each insert
            // this increases insert performance A LOT and, because we don't need the IDs, it is very useful to make
            // stocks purchases be VERY fast
            BoughtStocks.batchInsert(0 until quantity, shouldReturnGeneratedValues = false) {
                this[BoughtStocks.user] = userId
                this[BoughtStocks.ticker] = tickerId
                this[BoughtStocks.price] = valueOfStock
                this[BoughtStocks.boughtAt] = now.toEpochMilliseconds()
            }

            Profiles.update({ Profiles.id eq userId }) {
                with(SqlExpressionBuilder) {
                    it.update(Profiles.money, Profiles.money - howMuchValue)
                }
            }

            // Cinnamon transactions log
            SimpleSonhosTransactionsLogUtils.insert(
                userId,
                now.toJavaInstant(),
                TransactionType.HOME_BROKER,
                howMuchValue,
                StoredBrokerSonhosTransaction(
                    LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.BOUGHT_SHARES,
                    tickerId,
                    tickerInformation.value,
                    quantity
                )
            )

            logger.info { "User $userId bought $quantity $tickerId for $howMuchValue" }

            return@transaction BoughtSharesResponse(
                tickerId,
                quantity,
                howMuchValue
            )
        }
    }

    /**
     * Sells [quantity] shares in the [tickerId] ticker from [userId]'s account
     *
     * @param userId   the user ID that is selling the assets
     * @param tickerId the ticker's ID
     * @param quantity how many assets are going to be sold
     * @throws StaleTickerDataException if the data is stale and shouldn't be relied on
     * @throws NotEnoughSharesException if the [userId] doesn't have enough stocks to be sold
     * @throws OutOfSessionException    if the ticker isn't active
     */
    suspend fun sellStockShares(userId: Long, tickerId: String, quantity: Long): SoldSharesResponse {
        if (0 >= quantity)
            throw TransactionActionWithLessThanOneShareException()

        return pudding.transaction {
            val tickerInformation = getTicker(tickerId)

            checkIfTickerIsInactive(tickerInformation)
            checkIfTickerDataIsStale(tickerInformation)

            val selfStocks = BoughtStocks.selectAll().where {
                BoughtStocks.user eq userId and (BoughtStocks.ticker eq tickerId)
            }.toList()

            // Proper exceptions
            if (quantity > selfStocks.size)
                throw NotEnoughSharesException(selfStocks.size)

            val stocksThatWillBeSold = selfStocks.take(quantity.toInt())

            val sellingPrice = LorittaBovespaBrokerUtils.convertToSellingPrice(tickerInformation.value)
            val howMuchWillBePaidToTheUser = sellingPrice * quantity

            logger.info { "User $userId is trying to sell $quantity $tickerId for $howMuchWillBePaidToTheUser" }

            val userProfit = howMuchWillBePaidToTheUser - stocksThatWillBeSold.sumOf { it[BoughtStocks.price] }

            val now = Clock.System.now()

            // The reason we batch the stocks in multiple queries is due to this issue:
            // https://github.com/LorittaBot/Loritta/issues/2343
            // https://stackoverflow.com/questions/49274390/postgresql-and-hibernate-java-io-ioexception-tried-to-send-an-out-of-range-inte
            // Since PostgreSQL JDBC 42.3.7, the max parameter size is 65_535 parameters. This issue only affects "inList" queries!
            stocksThatWillBeSold.chunked(65_535).forEachIndexed { index, chunkedStocks ->
                BoughtStocks.deleteWhere {
                    BoughtStocks.id inList chunkedStocks.map { it[BoughtStocks.id] }
                }
            }

            Profiles.update({ Profiles.id eq userId }) {
                with(SqlExpressionBuilder) {
                    it.update(Profiles.money, Profiles.money + howMuchWillBePaidToTheUser)
                }
            }

            // Cinnamon transactions log
            SimpleSonhosTransactionsLogUtils.insert(
                userId,
                now.toJavaInstant(),
                TransactionType.HOME_BROKER,
                howMuchWillBePaidToTheUser,
                StoredBrokerSonhosTransaction(
                    LorittaBovespaBrokerUtils.BrokerSonhosTransactionsEntryAction.SOLD_SHARES,
                    tickerId,
                    tickerInformation.value,
                    quantity
                )
            )

            logger.info { "User $userId sold $quantity $tickerId for $howMuchWillBePaidToTheUser" }

            return@transaction SoldSharesResponse(
                tickerId,
                quantity,
                howMuchWillBePaidToTheUser,
                userProfit
            )
        }
    }

    private fun checkIfTickerIsInactive(tickerInformation: BrokerTickerInformation) {
        if (!LorittaBovespaBrokerUtils.checkIfTickerIsActive(tickerInformation.status))
            throw OutOfSessionException(tickerInformation.status)
    }

    private fun checkIfTickerDataIsStale(tickerInformation: BrokerTickerInformation) {
        if (LorittaBovespaBrokerUtils.checkIfTickerDataIsStale(tickerInformation.lastUpdatedAt))
            throw StaleTickerDataException()
    }

    data class BrokerUserStockShares(
        val ticker: String,
        val count: Long,
        val sum: Long,
        val average: BigDecimal
    )

    data class BoughtSharesResponse(
        val tickerId: String,
        val quantity: Long,
        val value: Long
    )

    data class SoldSharesResponse(
        val tickerId: String,
        val quantity: Long,
        val earnings: Long,
        val profit: Long
    )

    class NotEnoughSonhosException(val userSonhos: Long, val howMuch: Long) : RuntimeException()
    class OutOfSessionException(val currentSession: String) : RuntimeException()
    class TooManySharesException(val currentSharesCount: Long) : RuntimeException()
    class NotEnoughSharesException(val currentBoughtSharesCount: Int) : RuntimeException()
    class StaleTickerDataException : RuntimeException()
    class TransactionActionWithLessThanOneShareException : RuntimeException()
}