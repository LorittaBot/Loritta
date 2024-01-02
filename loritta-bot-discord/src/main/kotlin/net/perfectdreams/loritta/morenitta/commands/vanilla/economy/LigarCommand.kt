package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.cinnamon.pudding.tables.BomDiaECiaWinners
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.LorittaBot.Companion.RANDOM
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.PaymentUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.textChannel
import net.perfectdreams.loritta.serializable.SonhosPaymentReason
import org.jetbrains.exposed.sql.insert
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class LigarCommand(loritta: LorittaBot) : AbstractCommand(loritta, "ligar", category = net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	companion object {
		val coroutineExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
	}

	override fun getDescription(locale: BaseLocale): String {
		return "Experimental"
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		val phoneNumber = context.args.getOrNull(0)?.replace("-", "")

		if (phoneNumber != null) {
			if (phoneNumber == "40028922") {
				val profile = context.lorittaUser.profile

				if (75 > profile.money) {
					context.reply(
						LorittaReply(
							"Você não tem sonhos suficientes para completar esta ligação!",
							Constants.ERROR
						),
						LorittaReply(
							context.i18nContext.get(
								GACampaigns.sonhosBundlesUpsellDiscordMessage(
									"https://loritta.website/", // Hardcoded, woo
									"call-legacy",
									"yudi-is-sad-cuz-cant-call-him"
								)
							),
							prefix = Emotes.LORI_RICH.asMention
						)
					)
					return
				}

				loritta.newSuspendedTransaction {
					profile.takeSonhosAndAddToTransactionLogNested(
						75,
						SonhosPaymentReason.BOM_DIA_E_CIA
					)
				}

				GlobalScope.launch(coroutineExecutor) {
					if (loritta.bomDiaECia.available) {
						val args = context.args.toMutableList()
						args.removeAt(0)
						val text = args.joinToString(" ")
							.toLowerCase()

						if (text.contains("\u200B") || text.contains("\u200C") || text.contains("\u200D")) {
							context.reply(
								LorittaReply(
									"Poxa, não foi dessa vez amiguinho... mas não desista, ligue somente durante o programa, tá? Valeu! Aliás, não utilize CTRL-C e CTRL-V para você tentar vencer mais rápido. :^)",
									"<:yudi:446394608256024597>"
								)
							)
							loritta.bomDiaECia.triedToCall.add(context.userHandle.idLong)
							return@launch
						}

						if (text != loritta.bomDiaECia.currentText) {
							context.reply(
								LorittaReply(
									"Poxa, não foi dessa vez amiguinho... mas não desista, ligue somente durante o programa, tá? Valeu! Não se esqueça de escrever a nossa frase para que você possa ganhar o prêmio!",
									"<:yudi:446394608256024597>"
								)
							)
							loritta.bomDiaECia.triedToCall.add(context.userHandle.idLong)
							return@launch
						}

						loritta.bomDiaECia.available = false

						val randomPrize = RANDOM.nextInt(5_000, 10_001)
							.toLong()
						val guild = context.guild
						val user = context.userHandle
						val prizeAsBigDecimal = randomPrize.toBigDecimal()
						val wonMillis = System.currentTimeMillis()

						loritta.newSuspendedTransaction {
							profile.addSonhosNested(randomPrize)

							BomDiaECiaWinners.insert {
								it[guildId] = guild.idLong
								it[userId] = user.idLong
								it[wonAt] = wonMillis
								it[prize] = prizeAsBigDecimal
							}

							PaymentUtils.addToTransactionLogNested(
								randomPrize,
								SonhosPaymentReason.BOM_DIA_E_CIA,
								receivedBy = context.userHandle.idLong,
								givenAtMillis = wonMillis
							)
						}

						val wordsTyped = context.args.size
						val timeDiff = wonMillis - loritta.bomDiaECia.lastBomDiaECia
						val wordsPerMinute = ((60 * wordsTyped) / (timeDiff / 1000)).toDouble()
						val wpmAsInt = wordsPerMinute.roundToInt()

						logger.info("${context.userHandle.id} ganhou ${randomPrize} no Bom Dia & Cia!")
						logger.info("Demorou ${timeDiff}ms a acertar o Bom Dia & Cia, num total aproximado de ${wpmAsInt} palavras por minuto!")

						context.reply(
							LorittaReply(
								"Rodamos a roleta e... Parabéns! Você ganhou **${randomPrize} Sonhos**!\n*(Psiu, você sabia que conseguiu digitar aproximadamente **${wpmAsInt}** palavras por minuto? Impressionante, você tá de parabéns! <a:lori_yay_wobbly:638040459721310238>)*",
								"<:yudi:446394608256024597>"
							)
						)

						loritta.bomDiaECia.announceWinner(context.message.guildChannel, context.guild, context.userHandle)
					} else {
						context.reply(
							LorittaReply(
								"Poxa, não foi dessa vez amiguinho... mas não desista, ligue somente durante o programa, tá? Valeu! (Você apenas deve ligar após a <@297153970613387264> anunciar no chat para ligar!)",
								"<:yudi:446394608256024597>"
							)
						)
						if (30000 > System.currentTimeMillis() - loritta.bomDiaECia.lastBomDiaECia)
							loritta.bomDiaECia.triedToCall.add(context.userHandle.idLong)
					}
				}
			} else {
				context.reply(
					"Número desconhecido",
					"\uD83D\uDCF4"
				)
			}
		} else {
			this.explain(context)
		}
	}
}