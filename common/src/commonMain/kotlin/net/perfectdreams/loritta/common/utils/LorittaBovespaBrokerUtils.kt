package net.perfectdreams.loritta.common.utils

import kotlinx.datetime.*
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.i18n.I18nKeysData
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

object LorittaBovespaBrokerUtils {
    const val MAX_STOCK_SHARES_PER_USER = 100_000
    const val OUT_OF_SESSION = "out_of_session" // Inactive stock
    const val MARKET = "market" // Active stock, can be bought/sold
    @OptIn(ExperimentalTime::class)
    val OUTDATED_STOCKS_TIME = 60.seconds // After how much time the data should be considered stale
    val TIME_OPEN = Pair(10, 0)
    val TIME_CLOSING = Pair(16, 55)
    val BOVESPA_TIMEZONE = TimeZone.of("America/Sao_Paulo")

    val trackedTickerCodes = listOf(
        // ===[ INDUSTRIAL GOODS ]===
        StockTickerInfo(
            "AZUL4",
            "Azul",
            CompanyCategory.INDUSTRIAL_GOODS
        ),

        // ===[ BASIC MATERIALS ]===
        StockTickerInfo(
            "VALE3",
            "Vale S.A.",
            CompanyCategory.BASIC_MATERIALS
        ),

        // ===[ OIL, GAS AND BIOFUELS ]
        StockTickerInfo(
            "PETR4",
            "Petrobrás",
            CompanyCategory.OIL_GAS_AND_BIOFUELS
        ),

        // ===[ CONSUMER GOODS ]===
        StockTickerInfo(
            "MGLU3",
            "Magazine Luiza",
            CompanyCategory.CONSUMER_GOODS
        ),
        StockTickerInfo(
            "VIIA3",
            "Via Varejo",
            CompanyCategory.CONSUMER_GOODS
        ),
        StockTickerInfo(
            "AMER3",
            "B2W Digital",
            CompanyCategory.CONSUMER_GOODS
        ),
        StockTickerInfo(
            "ABEV3",
            "AMBEV",
            CompanyCategory.CONSUMER_GOODS
        ),
        StockTickerInfo(
            "TSLA34",
            "Tesla",
            CompanyCategory.CONSUMER_GOODS
        ),

        // ===[ FINANCE ]===
        StockTickerInfo(
            "ITUB4",
            "Itaú Unibanco",
            CompanyCategory.FINANCE
        ),
        StockTickerInfo(
            "BBDC4",
            "Bradesco S.A.",
            CompanyCategory.FINANCE
        ),
        StockTickerInfo(
            "IRBR3",
            "IRB Brasil S.A",
            CompanyCategory.FINANCE
        ),
        StockTickerInfo(
            "BBAS3",
            "Banco do Brasil S.A",
            CompanyCategory.FINANCE
        ),
        StockTickerInfo(
            "B3SA3",
            "B3",
            CompanyCategory.FINANCE
        ),
        StockTickerInfo(
            "IGTI3",
            "Jereissati Participações S.A.",
            CompanyCategory.FINANCE
        ),
        StockTickerInfo(
            "ROXO34",
            "Nubank",
            CompanyCategory.FINANCE
        ),

        // ===[ PUBLIC SERVICES ]===
        StockTickerInfo(
            "CMIG4",
            "Companhia Energética de Minas Gerais",
            CompanyCategory.PUBLIC_SERVICES
        ),
        StockTickerInfo(
            "SBSP3",
            "SABESP",
            CompanyCategory.PUBLIC_SERVICES
        ),

        // ===[ COMMUNICATIONS ]===
        StockTickerInfo(
            "OIBR3",
            "Oi",
            CompanyCategory.COMMUNICATIONS
        ),

        // ===[ TECHNOLOGY ]===
        StockTickerInfo(
            "LWSA3",
            "Locaweb",
            CompanyCategory.TECHNOLOGY
        ),
        StockTickerInfo(
            "NVDC34",
            "Nvidia",
            CompanyCategory.TECHNOLOGY
        ),
        StockTickerInfo(
            "A1MD34",
            "Advanced Micro Devices",
            CompanyCategory.TECHNOLOGY
        ),
    )

    // The openTime/closingTime YY/MM/DD is just Loritta's creation date (30/03/2017), because this date formatting type on Discord will only show the time in HH:MM format
    // tl;dr: just a nice and cute easter egg :3
    val TIME_OPEN_DISCORD_TIMESTAMP = "<t:${LocalDateTime(2017, 3, 30, TIME_OPEN.first, TIME_OPEN.second, 0).toInstant(BOVESPA_TIMEZONE).epochSeconds}:t>"
    val TIME_CLOSING_DISCORD_TIMESTAMP = "<t:${LocalDateTime(2017, 3, 30, TIME_CLOSING.first, TIME_CLOSING.second, 0).toInstant(BOVESPA_TIMEZONE).epochSeconds}:t>"

    val validStocksCodes = trackedTickerCodes.map { it.ticker }.toSet()

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

    fun checkIfTickerIsActive(currentSession: String): Boolean {
        val now = Clock.System.now().toLocalDateTime(BOVESPA_TIMEZONE)

        val isWithinOpeningHours = when {
            now.hour < TIME_OPEN.first || now.hour > TIME_CLOSING.first -> false
            now.hour == TIME_OPEN.first && now.minute < TIME_OPEN.second -> false
            now.hour == TIME_CLOSING.first && now.minute > TIME_CLOSING.second -> false
            else -> true
        }

        return currentSession == LorittaBovespaBrokerUtils.MARKET && isWithinOpeningHours
    }

    fun checkIfTickerDataIsStale(lastUpdatedAt: Instant) = Clock.System.now() > lastUpdatedAt.plus(LorittaBovespaBrokerUtils.OUTDATED_STOCKS_TIME)

    data class StockTickerInfo(
        val ticker: String,
        val name: String,
        val category: CompanyCategory
    )

    enum class BrokerSonhosTransactionsEntryAction {
        BOUGHT_SHARES,
        SOLD_SHARES
    }

    enum class CompanyCategory(val emoji: Emote, val i18nName: StringI18nData) {
        INDUSTRIAL_GOODS(Emotes.Factory, I18nKeysData.Commands.Command.Broker.Categories.IndustrialGoods),
        BASIC_MATERIALS(Emotes.Diamond, I18nKeysData.Commands.Command.Broker.Categories.BasicMaterials),
        OIL_GAS_AND_BIOFUELS(Emotes.OilDrum, I18nKeysData.Commands.Command.Broker.Categories.OilGasAndBiofuels),
        FINANCE(Emotes.LoriCard, I18nKeysData.Commands.Command.Broker.Categories.Finance),
        PUBLIC_SERVICES(Emotes.HighVoltage, I18nKeysData.Commands.Command.Broker.Categories.PublicServices),
        TECHNOLOGY(Emotes.Computer, I18nKeysData.Commands.Command.Broker.Categories.Technology),
        COMMUNICATIONS(Emotes.Antenna, I18nKeysData.Commands.Command.Broker.Categories.Communications),
        CONSUMER_GOODS(Emotes.ShoppingBags, I18nKeysData.Commands.Command.Broker.Categories.ConsumerGoods)
    }
}