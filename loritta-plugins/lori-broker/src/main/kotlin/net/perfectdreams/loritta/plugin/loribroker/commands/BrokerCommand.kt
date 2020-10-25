package net.perfectdreams.loritta.plugin.loribroker.commands

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.network.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.plugin.loribroker.LoriBrokerPlugin
import net.perfectdreams.loritta.plugin.loribroker.tables.BoughtStocks
import net.perfectdreams.loritta.tables.SonhosTransaction
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.PaymentUtils
import net.perfectdreams.loritta.utils.SonhosPaymentReason
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.Connection

class BrokerCommand(val plugin: LoriBrokerPlugin) : DiscordAbstractCommandBase(plugin.loritta, plugin.aliases, CommandCategory.ECONOMY) {
	override fun command() = create {
		localizedDescription("commands.economy.broker.description")

		executesDiscord {
			if (this.args.getOrNull(0) == "fix" && this.user.idLong == 123170274651668480L) {
				newSuspendedTransaction(Dispatchers.IO, Databases.loritta, Connection.TRANSACTION_READ_UNCOMMITTED) {
					val stuff = SonhosTransaction.select {
						SonhosTransaction.reason eq SonhosPaymentReason.STOCKS and (SonhosTransaction.receivedBy.isNotNull()) and (SonhosTransaction.givenAt greaterEq 1603475884000L) and (SonhosTransaction.givenAt lessEq 1603475890000L)
					}.toList()


					for (r in stuff) {
						val profile = LorittaLauncher.loritta._getLorittaProfile(r[SonhosTransaction.receivedBy]!!) ?: continue

						profile.money -= r[SonhosTransaction.quantity].toLong()

						SonhosTransaction.deleteWhere { SonhosTransaction.id eq r[SonhosTransaction.id] }
					}
				}

				sendMessage("Fixed!")
				return@executesDiscord
			}

			if (this.args.getOrNull(0) == "desdobramento" && this.user.idLong == 123170274651668480L) {
				val byUser = mutableMapOf<Long, Long>()

				newSuspendedTransaction {
					val x = BoughtStocks.select {
						BoughtStocks.ticker eq "MELI34"
					}

					x.forEach {
						byUser[it[BoughtStocks.user]] = byUser.getOrPut(it[BoughtStocks.user], { 0 }) + it[BoughtStocks.price]
					}
				}

				for ((user, track) in byUser) {
					newSuspendedTransaction {
						val profile = LorittaLauncher.loritta._getLorittaProfile(user)

						if (profile != null) {
							profile.addSonhosNested(
									track
							)

							PaymentUtils.addToTransactionLogNested(
									track,
									SonhosPaymentReason.STOCKS,
									receivedBy = user
							)
						}
					}
				}

				sendMessage("Desobramento vendido!")
				return@executesDiscord
			}

			val stocks = plugin.validStocksCodes.map {
				plugin.tradingApi.getOrRetrieveTicker(
						it,
						listOf("lp", "description", "current_session")
				)
			}

			val embed = plugin.getBaseEmbed()
					.setTitle(locale["commands.economy.broker.title"])
					.setDescription(
							locale.getList(
									"commands.economy.broker.explanation",
									locale["commands.economy.broker.buyExample", serverConfig.commandPrefix],
									locale["commands.economy.broker.sellExample", serverConfig.commandPrefix],
									locale["commands.economy.broker.portfolioExample", serverConfig.commandPrefix],
									Emotes.DO_NOT_DISTURB,
									Emotes.LORI_CRYING
							).joinToString("\n")
					)

			for (stock in stocks) {
				val tickerId = stock["short_name"]!!.jsonPrimitive.content
				val tickerName = plugin.fancyTickerNames[tickerId]

				if (stock["current_session"]!!.jsonPrimitive.content != LoriBrokerPlugin.MARKET)
					embed.addField(
							"${Emotes.DO_NOT_DISTURB} `${stock["short_name"]?.jsonPrimitive?.content}` ($tickerName)",
							"${plugin.convertReaisToSonhos(stock["lp"]?.jsonPrimitive?.double!!)} sonhos",
							true
					)
				else
					embed.addField(
							"${Emotes.ONLINE} `${stock["short_name"]?.jsonPrimitive?.content}` ($tickerName)",
							"${plugin.convertReaisToSonhos(stock["lp"]?.jsonPrimitive?.double!!)} sonhos",
							true
					)
			}

			sendMessage(embed.build())
		}
	}
}