package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import com.google.common.cache.CacheBuilder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.Raspadinhas
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.GACampaigns
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.edit
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.onReactionByAuthor
import net.perfectdreams.loritta.morenitta.utils.sendStyledReply
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.update
import java.util.concurrent.TimeUnit

class ScratchCardCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("scratchcard", "raspadinha"), net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY) {
	companion object {
		private val mutexes = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build<Long, Mutex>()
			.asMap()
		private const val LORITTA_COMBO = 100_000
		private const val PANTUFA_COMBO = 10_000
		private const val GABI_COMBO = 1_000
		private const val DOKYO_COMBO = 375
		private const val GESSY_COMBO = 250
		private const val TOBIAS_COMBO = 130
		private val logger by HarmonyLoggerFactory.logger {}
		private const val LOCALE_PREFIX = "commands.command"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.scratchcard.description")

		usage {
			arguments {
				argument(ArgumentType.TEXT) {}
			}
		}

		canUseInPrivateChannel = false

		executesDiscord {
			if (SonhosUtils.checkIfEconomyIsDisabled(this))
				return@executesDiscord

			val context = this

			if (context.args.firstOrNull() == "ganhar" || context.args.firstOrNull() == "win" || context.args.firstOrNull() == "claim") {
				checkRaspadinha(context, context.lorittaUser.profile, context.args.getOrNull(1)?.toLongOrNull())
			} else if (context.args.firstOrNull() == "comprar" || context.args.firstOrNull() == "buy") {
				buyRaspadinha(context, context.lorittaUser.profile)
			} else {
				val raspadinhaCount = loritta.pudding.transaction {
					Raspadinhas.selectAll().where {
						Raspadinhas.receivedById eq context.user.idLong
					}.count()
				}
				val earnings = Raspadinhas.value.sum()
				val raspadinhaEarnings = loritta.pudding.transaction {
					Raspadinhas.select(Raspadinhas.receivedById, earnings).where { 
						Raspadinhas.receivedById eq context.user.idLong
					}.groupBy(Raspadinhas.receivedById)
						.firstOrNull()
				}

				context.reply(
					LorittaReply(
						"**Raspadinha da Loritta**",
						"<:loritta:331179879582269451>"
					),
					LorittaReply(
						"Ganhe prêmios comprando um ticket para raspar!",
						"<:starstruck:540988091117076481>",
						mentionUser = false
					),
					LorittaReply(
						"Ao comprar, raspe clicando nos spoilers e veja os emojis que aparecem!",
						"\uD83C\uDFAB",
						mentionUser = false
					),
					LorittaReply(
						"Se tiver alguma combinação na horizontal, vertical ou na diagional, você pode ganhar prêmios!",
						"\uD83D\uDC65",
						mentionUser = false
					),
					LorittaReply(
						"**Combinação de <:loritta:664849802961485894>:** $LORITTA_COMBO sonhos",
						mentionUser = false
					),
					LorittaReply(
						"**Combinação de <:pantufa:664849802793713686>:** $PANTUFA_COMBO sonhos",
						mentionUser = false
					),
					LorittaReply(
						"**Combinação de <:gabriela:664849802927800351>:** $GABI_COMBO sonhos",
						mentionUser = false
					),
					LorittaReply(
						"**Combinação de <:dokyo:664849803397562369>:** $DOKYO_COMBO sonhos",
						mentionUser = false
					),
					LorittaReply(
						"**Combinação de <:gessy:664849803334909952>:** $GESSY_COMBO sonhos",
						mentionUser = false
					),
					LorittaReply(
						"**Combinação de <:tobias_nosa:450476856303419432>:** $TOBIAS_COMBO sonhos",
						mentionUser = false
					),
					LorittaReply(
						"Você já comprou **${raspadinhaCount} raspadinhas** e, com elas, você ganhou **${raspadinhaEarnings?.get(earnings) ?: 0} sonhos**",
						mentionUser = false
					),
					LorittaReply(
						"Compre uma raspadinha da Loritta por **150 sonhos** usando `${context.serverConfig.commandPrefix}raspadinha comprar`!",
						prefix = "\uD83D\uDCB5",
						mentionUser = false
					)
				)
			}
		}
	}
	private suspend fun buyRaspadinha(context: DiscordCommandContext, profile: Profile, message: Message? = null, _boughtScratchCardsInThisMessage: Int = 0) {
		var boughtScratchCardsInThisMessage = _boughtScratchCardsInThisMessage
		val mutex = mutexes.getOrPut(context.user.idLong, { Mutex() })
		mutex.withLock {
			if (150 > profile.money) {
				context.sendStyledReply {
					this.append {
						this.message = "Você precisa de 150 sonhos para poder comprar uma raspadinha!"
						prefix = Constants.ERROR
					}

					this.append {
						this.message = context.i18nContext.get(
							GACampaigns.sonhosBundlesUpsellDiscordMessage(
                                loritta.config.loritta.dashboard.url,
								"scratch-card-legacy",
								"bet-not-enough-sonhos"
							)
						)
						prefix = Emotes.LORI_RICH.asMention
						mentionUser = false
					}
				}
				return@withLock
			}

			loritta.newSuspendedTransaction {
				profile.takeSonhosNested(150)
			}

			logger.info { "User ${context.user.idLong} bought one raspadinha ticket!" }

			val array = Array(3) { Array<Char>(3, init = { 'Z' }) }

			for (x in 0 until 3) {
				for (y in 0 until 3) {
					val randomNumber = LorittaBot.RANDOM.nextInt(1, 101)

					when (randomNumber) {
						100 -> { // 1
							array[x][y] = 'L'
						}
						in 94..99 -> { // 3
							array[x][y] = 'P'
						}
						in 78..93 -> { // 6
							array[x][y] = 'B'
						}
						in 59..77 -> { // 20
							array[x][y] = 'D'
						}
						in 34..58 -> { // 25
							array[x][y] = 'G'
						}
						in 0..33 -> { // 25
							array[x][y] = 'T'
						}
					}
				}
			}

			fun transformToEmote(char: Char): String {
				return when (char) {
					'L' -> "<:loritta:664849802961485894>"
					'P' -> "<:pantufa:664849802793713686>"
					'B' -> "<:gabriela:664849802927800351>"
					'D' -> "<:dokyo:664849803397562369>"
					'G' -> "<:gessy:664849803334909952>"
					'T' -> "<:tobias_nosa:450476856303419432>"
					else -> throw RuntimeException("I don't know what emote is for $char")
				}
			}

			val id = loritta.pudding.transaction {
				Raspadinhas.insertAndGetId {
					it[receivedById] = context.user.idLong
					it[receivedAt] = System.currentTimeMillis()
					it[pattern] = array.joinToString(
						"\n",
						transform = {
							it.joinToString("")
						})
					it[scratched] = false
				}
			}

			val scratchCardTemplate = """<:scratch_01:664139718279168002><:scratch_02:664139717889097739><:scratch_03:664139718161596416><:scratch_04:664139718329630721><:scratch_05:664139718304464896><:scratch_06:664139718132236318>
<:scratch_07:664139718337888266>||${transformToEmote(array[0][0])}||||${transformToEmote(array[1][0])}||||${transformToEmote(array[2][0])}||<:scratch_11:664139718220316702><:scratch_12:664139718220316685>
<:scratch_13:664139718308397076>||${transformToEmote(array[0][1])}||||${transformToEmote(array[1][1])}||||${transformToEmote(array[2][1])}||<:scratch_17:664139718321242134><:scratch_18:664139718123978763>
<:scratch_19:664139718140755986>||${transformToEmote(array[0][2])}||||${transformToEmote(array[1][2])}||||${transformToEmote(array[2][2])}||<:scratch_23:664139718266716160><:scratch_24:664139718354665492> 
<:scratch_25:664139718354796545><:scratch_26:664139718014795779><:scratch_27:664139718237224981><:scratch_28:664139718388351007><:scratch_29:664139718430162954><:scratch_30:664139717989629968>"""

			var invisibleSpoilers = ""
			repeat(boughtScratchCardsInThisMessage) {
				// Ao editar mensagens, os spoilers continuam abertos
				// Como workaround, podemos adicionar spoilers invisíveis no começo da mensagem
				invisibleSpoilers += "||\u200D||||\u200D||||\u200D||||\u200D||||\u200D||||\u200D||||\u200D||||\u200D||||\u200D||||\u200D||||\u200D||"
			}

			val content = "${Emotes.LORI_RICH} **|** ${context.getUserMention(false)} aqui está a sua raspadinha com número $id! Raspe clicando na parte cinza e, se o seu cartão for premiado com combinações de emojis na horizontal/vertical/diagonal, clique em ${Emotes.LORI_RICH} para receber a sua recompensa! Mas cuidado, não tente resgatar prêmios de uma raspadinha que não tem prêmios!! Se você quiser comprar um novo ticket pagando 150 sonhos, aperte em \uD83D\uDD04!!\n$scratchCardTemplate"
			var contentWithInvisibleSpoilers = "$invisibleSpoilers$content"
			if (contentWithInvisibleSpoilers.length >= 2000 && message != null) {
				// Se a mensagem está ficando grande demais por causa dos spoilers, vamos editar para que seja vazia para "liberar" os spoilers usados
				message.edit(MessageCreateBuilder().setContent("...").build(), clearReactions = false)
				contentWithInvisibleSpoilers = content
				boughtScratchCardsInThisMessage = 0
			}

			val theMessage = message?.edit(MessageCreateBuilder().setContent(contentWithInvisibleSpoilers).build(), clearReactions = false) ?: context.discordMessage.channel.sendMessage(contentWithInvisibleSpoilers).await()

			theMessage?.onReactionByAuthor(context) {
				if (it.emoji.isEmote("\uD83D\uDD04")) {
					buyRaspadinha(context, loritta.getOrCreateLorittaProfile(context.user.idLong), theMessage, boughtScratchCardsInThisMessage + 1)
				}

				if (it.emoji.isEmote("593979718919913474")) {
					checkRaspadinha(context, loritta.getOrCreateLorittaProfile(context.user.idLong), id.value)
				}
			}

			if (message == null) {
				theMessage?.addReaction("lori_rica:593979718919913474")?.queue()
				theMessage?.addReaction("\uD83D\uDD04")?.queue()
			}
		}
	}

	private suspend fun checkRaspadinha(context: DiscordCommandContext, profile: Profile, id: Long?) {
		val mutex = mutexes.getOrPut(context.user.idLong, { Mutex() })
		mutex.withLock {
			val raspadinha = loritta.pudding.transaction {
				Raspadinhas.selectAll().where {
					Raspadinhas.id eq id
				}.firstOrNull()
			}

			if (raspadinha == null) {
				context.reply(
					LorittaReply(
						"Essa raspadinha não existe!",
						Constants.ERROR
					)
				)
				return@withLock
			}

			if (raspadinha[Raspadinhas.receivedById] != context.user.idLong) {
				context.reply(
					LorittaReply(
						"Nossa, não sabia que você era assim... tentando roubar os prêmios de outras raspadinhas que não foi você que comprou...",
						Constants.ERROR
					)
				)
				return@withLock
			}

			if (raspadinha[Raspadinhas.scratched]) {
				context.reply(
					LorittaReply(
						"Você já recebeu o prêmio desta raspadinha!",
						Constants.ERROR
					)
				)
				return@withLock
			}

			val array = Array(3) { Array<Char>(3, init = { 'Z' }) }

			val splittedStoredPattern = raspadinha[Raspadinhas.pattern].split("\n")

			for ((y, lines) in splittedStoredPattern.withIndex()) {
				for ((x, char) in lines.withIndex()) {
					array[x][y] = char
				}
			}

			fun checkArrayFor(ch: Char): Int {
				var combos = 0
				if (array[0][0] == ch && array[1][0] == ch && array[2][0] == ch) // horizontal, primeira linha
					combos++
				if (array[0][1] == ch && array[1][1] == ch && array[2][1] == ch) // horizontal, segunda linha
					combos++
				if (array[0][2] == ch && array[1][2] == ch && array[2][2] == ch) // horizontal, terceira linha
					combos++

				if (array[0][0] == ch && array[0][1] == ch && array[0][2] == ch) // vertical, primeira linha
					combos++
				if (array[1][0] == ch && array[1][1] == ch && array[1][2] == ch) // vertical, segunda linha
					combos++
				if (array[2][0] == ch && array[2][1] == ch && array[2][2] == ch) // vertical, terceira linha
					combos++

				if (array[0][0] == ch && array[1][1] == ch && array[2][2] == ch) // diagonal1
					combos++
				if (array[2][0] == ch && array[1][1] == ch && array[0][2] == ch) // diagonal2
					combos++

				return combos
			}

			var prize = 0
			val loriCombos = checkArrayFor('L')
			val pantufaCombos = checkArrayFor('P')
			val gabiCombos = checkArrayFor('B')
			val dokyoCombos = checkArrayFor('D')
			val gessyCombos = checkArrayFor('G')
			val tobiasCombos = checkArrayFor('T')

			prize += (loriCombos * LORITTA_COMBO)
			prize += (pantufaCombos * PANTUFA_COMBO)
			prize += (gabiCombos * GABI_COMBO)
			prize += (dokyoCombos * DOKYO_COMBO)
			prize += (gessyCombos * GESSY_COMBO)
			prize += (tobiasCombos * TOBIAS_COMBO)

			loritta.pudding.transaction {
				Raspadinhas.update({ Raspadinhas.id eq id }) {
					it[scratched] = true
					it[value] = prize
				}
			}

			if (prize == 0) {
				loritta.newSuspendedTransaction {
					if (1000 > profile.money) {
						profile.money = 0
					} else {
						profile.takeSonhosNested(1000)
					}
				}

				context.reply(
					LorittaReply(
						"Qual parte de *não resgate um prêmio se você não ganhou* você não entendeu? Só por fazer perder o meu tempo, você perdeu 1000 sonhos. ${Emotes.LORI_SHRUG}"
					)
				)
			} else {
				logger.info { "User ${context.user.idLong} won $prize sonhos in the raspadinha! Combos: Lori: $loriCombos; Pantufa: $pantufaCombos; Gabi: $gabiCombos; Dokyo: $dokyoCombos; Gessy: $gessyCombos; Tobias: $tobiasCombos" }
				loritta.newSuspendedTransaction {
					profile.addSonhosNested(prize.toLong())
				}
				context.reply(
					LorittaReply(
						"Parabéns, você ganhou **$prize sonhos** na sua raspadinha!",
						Emotes.LORI_PAT
					)
				)
			}
		}
	}
}