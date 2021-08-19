package net.perfectdreams.loritta.plugin.loribroker

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerBuyStockCommand
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerCommand
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerPortfolioCommand
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerSellStockCommand
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.loritta.plugin.loribroker.utils.TradingViewRelayConnector
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class LoriBrokerPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
	private var _tradingApi: TradingViewRelayConnector? = null
	val tradingApi: TradingViewRelayConnector
		get() = _tradingApi ?: throw RuntimeException("TradingView API not started!")

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
			"IGTA3"  to "Iguatemi Empresa de Shopping Centers",
			"OIBR3"  to "Oi",
			"ABEV3"  to "AMBEV",
			"TSLA34" to "Tesla",
			"B3SA3"  to "B3",
			"SBSP3"  to "SABESP",
			"LWSA3"  to "Locaweb",
			"CIEL3"  to "Cielo"
	)

	val validStocksCodes = trackedTickerCodes.keys

	// Only allow one transaction per user to buy/sell stocks at the same time, to avoid synchronization issues
	val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build<Long, Mutex>()
			.asMap()
	val aliases = listOf(
			"broker",
			"corretora"
	)

	override fun onEnable() {
		loritta as Loritta

		launch {
			_tradingApi = TradingViewRelayConnector("wss://tvrs.perfectdreams.net/")

			tradingApi.start()
		}

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					BoughtStocks
			)
		}

		registerCommand(BrokerCommand(this))
		registerCommand(BrokerBuyStockCommand(this))
		registerCommand(BrokerSellStockCommand(this))
		registerCommand(BrokerPortfolioCommand(this))
	}

	override fun onDisable() {
		// We don't want Loritta to shutdown our task before it is fully shutted down, so we need to block it
		runBlocking {
			tradingApi.shutdown()
		}

		super.onDisable()
	}

	/**
	 * Converts reais (from TradingView) to sonhos
	 *
	 * The input is multiplied by 100, floor'd and then converted to long
	 * @param input the input in reais
	 * @return      the value in sonhos
	 */
	fun convertReaisToSonhos(input: Double): Long {
		return floor(input * 100).toLong()
	}

	fun convertToBuyingPrice(input: Long) = input + 1
	fun convertToSellingPrice(input: Long) = input - 1

	fun getBaseEmbed() = EmbedBuilder()
			.setAuthor("Loritta's Home Broker")
			.setColor(BROKER_COLOR)
			.setThumbnail("${(loritta as LorittaDiscord).instanceConfig.loritta.website.url}assets/img/loritta_stonks.png")

	companion object {
		private val logger = KotlinLogging.logger {}
		private val BROKER_COLOR = Color(23, 62, 163)
		const val OUT_OF_SESSION = "out_of_session" // Inactive stock
		const val MARKET = "market" // Active stock, can be bought/sold
		const val MAX_STOCKS = 100_000L

		const val CURRENT_PRICE_FIELD = "lp"
	}
}