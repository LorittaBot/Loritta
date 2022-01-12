package net.perfectdreams.loritta.cinnamon.common.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

object LorittaBovespaBrokerUtils {
    const val MAX_STOCK_SHARES_PER_USER = 100_000
    const val OUT_OF_SESSION = "out_of_session" // Inactive stock
    const val MARKET = "market" // Active stock, can be bought/sold
    @OptIn(ExperimentalTime::class)
    val OUTDATED_STOCKS_TIME = Duration.seconds(60) // After how much time the data should be considered stale
    val TIME_OPEN = Pair(10, 0)
    val TIME_CLOSING = Pair(17, 55)
    val BOVESPA_TIMEZONE = TimeZone.of("America/Sao_Paulo")

    val trackedTickerCodes = mapOf(
        "GOLL4"  to "Gol",
        "AZUL4"  to "Azul",
        "PETR4"  to "Petrobrás",
        "MGLU3"  to "Magazine Luiza",
        "VIIA3"  to "Via Varejo",
        "LAME4"  to "Lojas Americanas",
        "ITUB4"  to "Itaú Unibanco",
        "VALE3"  to "Vale S.A.",
        "BBDC4"  to "Bradesco S.A.",
        "IRBR3"  to "IRB Brasil S.A",
        "BBAS3"  to "Banco do Brasil S.A",
        "CRFB3"  to "Atacadão",
        "CMIG4"  to "Companhia Energética de Minas Gerais",
        "NUBR33" to "Nubank",
        "OIBR3"  to "Oi",
        "ABEV3"  to "AMBEV",
        "TSLA34" to "Tesla",
        "B3SA3"  to "B3",
        "SBSP3"  to "SABESP",
        "LWSA3"  to "Locaweb",
        "CIEL3"  to "Cielo",
        "IGTI3"  to "Jereissati Participações S.A.",
        "NVDC34" to "Nvidia",
        "A1MD34" to "Advanced Micro Devices"
    )

    // The openTime/closingTime YY/MM/DD is just Loritta's creation date (30/03/2017), because this date formatting type on Discord will only show the time in HH:MM format
    // tl;dr: just a nice and cute easter egg :3
    val TIME_OPEN_DISCORD_TIMESTAMP = "<t:${LocalDateTime(2017, 3, 30, TIME_OPEN.first, TIME_OPEN.second, 0).toInstant(BOVESPA_TIMEZONE).epochSeconds}:t>"
    val TIME_CLOSING_DISCORD_TIMESTAMP = "<t:${LocalDateTime(2017, 3, 30, TIME_CLOSING.first, TIME_CLOSING.second, 0).toInstant(BOVESPA_TIMEZONE).epochSeconds}:t>"

    val validStocksCodes = trackedTickerCodes.keys

    /**
     * Converts reais (from TradingView) to sonhos
     *
     * *Currently this does nothing to the input, it just returns the current value*
     *
     * @param input the input in reais
     * @return      the value in sonhos
     */
    fun convertReaisToSonhos(input: Long) = input

    fun convertToBuyingPrice(input: Long) = input + 1
    fun convertToSellingPrice(input: Long) = input - 1

    fun checkIfTickerIsActive(currentSession: String) = currentSession == LorittaBovespaBrokerUtils.MARKET

    @OptIn(ExperimentalTime::class)
    fun checkIfTickerDataIsStale(lastUpdatedAt: Instant) = Clock.System.now() > lastUpdatedAt.plus(LorittaBovespaBrokerUtils.OUTDATED_STOCKS_TIME)

    enum class BrokerSonhosTransactionsEntryAction {
        BOUGHT_SHARES,
        SOLD_SHARES
    }
}