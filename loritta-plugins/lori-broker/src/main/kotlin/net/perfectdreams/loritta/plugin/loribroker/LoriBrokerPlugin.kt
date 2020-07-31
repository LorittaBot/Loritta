package net.perfectdreams.loritta.plugin.loribroker

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerBuyStockCommand
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerCommand
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerPortfolioCommand
import net.perfectdreams.loritta.plugin.loribroker.commands.BrokerSellStockCommand
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.tradingviewscraper.TradingViewAPI
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.io.File
import kotlin.math.floor

class LoriBrokerPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
	private var _tradingApi: TradingViewAPI? = null
	val tradingApi: TradingViewAPI
		get() = _tradingApi ?: throw RuntimeException("TradingView API not started!")


	val validStocksCodes = listOf(
			"OIBR3", // Oi
			"USDBRL", // Dólar
			"GOLL4", // Gol
			"AZUL4", // Azul
			"PETR4", // Petrobras
			"MELI34", // MercadoLivre
			"MGLU3", // Magazine Luiza
			"VVAR3", // ViaVarejo
			"LAME4", // Americanas
			"PCAR3", // Pão de Açúcar
			"ITUB4", // Itaú
			"VALE3", // Vale
			"BIDU34", // Baidu
			"ETHBRL", // Etherum
			"BTCBRL" // Bitcoin
	)

	val fancyTickerNames = mapOf(
			"OIBR3" to "Oi",
			"USDBRL" to "Dólar / Reais",
			"GOLL4" to "Gol",
			"AZUL4" to "Azul",
			"PETR4" to "Petrobrás",
			"MELI34" to "MercadoLivre",
			"MGLU3" to "Magazine Luiza",
			"VVAR3" to "Via Varejo",
			"LAME4" to "Lojas Americanas",
			"PCAR3" to "Pão de Açúcar",
			"ITUB4" to "Itaú Unibanco",
			"VALE3" to "Vale S.A.",
			"BIDU34" to "Baidu",
			"ETHBRL" to "Etherum / Reais",
			"BTCBRL" to "Bitcoin / Reais"
	)
	// Only allow one user to buy/sell stocks at the same time, to avoid synchronization issues
	val mutex = Mutex()
	val aliases = listOf(
			"broker",
			"corretora"
	)

	override fun onEnable() {
		loritta as Loritta

		launch {
			val sessionId = File(dataFolder, "session-id.txt")
					.readText()

			val result = loritta.http.get<String>("https://br.tradingview.com/quote_token/") {
				header("Cookie", "sessionid=$sessionId;")
			}.removePrefix("\"")
					.removeSuffix("\"")

			_tradingApi = TradingViewAPI(result)

			tradingApi.connect()
		}

		transaction(Databases.loritta) {
			SchemaUtils.createMissingTablesAndColumns(
					BoughtStocks
			)
		}

		registerCommand(BrokerCommand.command(this, loritta))
		registerCommand(BrokerBuyStockCommand.command(this, loritta))
		registerCommand(BrokerSellStockCommand.command(this, loritta))
		registerCommand(BrokerPortfolioCommand.command(this, loritta))
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
	 * The input is multiplied by 10, floor'd and then converted to long
	 * @param input the input in reais
	 * @return      the value in sonhos
	 */
	fun convertReaisToSonhos(input: Double): Long {
		return floor(input * 10).toLong()
	}

	fun getBaseEmbed() = EmbedBuilder()
			.setAuthor("Loritta's Home Broker")
			.setColor(BROKER_COLOR)
			.setThumbnail("${(loritta as LorittaDiscord).instanceConfig.loritta.website.url}assets/img/loritta_stonks.png")

	companion object {
		private val logger = KotlinLogging.logger {}
		private val BROKER_COLOR = Color(23, 62, 163)
	}
}