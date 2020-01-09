package net.perfectdreams.loritta.commands.economy

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.Raspadinhas
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ScratchCardCommand : LorittaCommand(arrayOf("scratchcard", "raspadinha"), CommandCategory.DISCORD) {
	companion object {
		val mutex = Mutex()
		private const val LORITTA_COMBO = 100_000
		private const val PANTUFA_COMBO = 10_000
		private const val GABI_COMBO = 1_000
		private const val DOKYO_COMBO = 375
		private const val GESSY_COMBO = 250
		private const val TOBIAS_COMBO = 130
		private val logger = KotlinLogging.logger {}
	}

	override fun getDescription(locale: BaseLocale): String? {
		return locale["commands.discord.scratchcard.description"]
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.TEXT) {}
		}
	}

	@Subcommand
	suspend fun root(context: DiscordCommandContext, locale: BaseLocale) {
		if (context.args.firstOrNull() == "ganhar" || context.args.firstOrNull() == "win" || context.args.firstOrNull() == "claim") {
			checkRaspadinha(context, context.lorittaUser.profile, context.args.getOrNull(1)?.toLongOrNull())
		} else if (context.args.firstOrNull() == "comprar" || context.args.firstOrNull() == "buy") {
			buyRaspadinha(context, context.lorittaUser.profile)
		} else {
			context.reply(
					LoriReply(
							"**Raspadinha da Loritta**",
							"<:loritta:331179879582269451>"
					),
					LoriReply(
							"Ganhe prêmios comprando um ticket para raspar!",
							"<:starstruck:540988091117076481>",
							mentionUser = false
					),
					LoriReply(
							"Ao comprar, raspe clicando nos spoilers e veja os emojis que aparecem!",
							"\uD83C\uDFAB",
							mentionUser = false
					),
					LoriReply(
							"Se tiver alguma combinação na horizontal, vertical ou na diagional, você pode ganhar prêmios!",
							"\uD83D\uDC65",
							mentionUser = false
					),
					LoriReply(
							"**Combinação de <a:lori_derp:653310964652834886>:** ${LORITTA_COMBO} sonhos",
							mentionUser = false
					),
					LoriReply(
							"**Combinação de <:pantufa_gasp:645104302460895272>:** ${PANTUFA_COMBO} sonhos",
							mentionUser = false
					),
					LoriReply(
							"**Combinação de <a:gabi_calca_1:647982987937447937>:** ${GABI_COMBO} sonhos",
							mentionUser = false
					),
					LoriReply(
							"**Combinação de <:dokyo_funkeiro:603389181808607262>:** ${DOKYO_COMBO} sonhos",
							mentionUser = false
					),
					LoriReply(
							"**Combinação de <:gesso:523176710439567392>:** ${GESSY_COMBO} sonhos",
							mentionUser = false
					),
					LoriReply(
							"**Combinação de <:tobias_nosa:450476856303419432>:** ${TOBIAS_COMBO} sonhos",
							mentionUser = false
					),
					LoriReply(
							"Compre uma raspadinha da Loritta por **125 sonhos** usando `${context.config.commandPrefix}raspadinha comprar`!",
							prefix = "\uD83D\uDCB5",
							mentionUser = false
					)
			)
		}
	}

	private suspend fun buyRaspadinha(context: DiscordCommandContext, profile: Profile) {
		mutex.withLock {
			if (125 > profile.money) {
				context.reply(
						LoriReply(
								"Você precisa de 125 sonhos para poder comprar uma raspadinha!",
								Constants.ERROR
						)
				)
				return@withLock
			}

			transaction(Databases.loritta) {
				profile.money -= 125
			}

			logger.info { "User ${context.userHandle.idLong} bought one raspadinha ticket!" }

			val array = Array(3) { Array<Char>(3, init = { 'Z' }) }

			for (x in 0 until 3) {
				for (y in 0 until 3) {
					val randomNumber = Loritta.RANDOM.nextInt(1, 101)

					when (randomNumber) {
						in 99..100 -> { // 1
							array[x][y] = 'L'
						}
						in 93..99 -> { // 3
							array[x][y] = 'P'
						}
						in 77..92 -> { // 6
							array[x][y] = 'B'
						}
						in 58..76 -> { // 20
							array[x][y] = 'D'
						}
						in 33..57 -> { // 25
							array[x][y] = 'G'
						}
						in 0..32 -> { // 25
							array[x][y] = 'T'
						}
					}
				}
			}

			fun transformToEmote(char: Char): String {
				return when (char) {
					'L' -> "<a:lori_derp:653310964652834886>"
					'P' -> "<:pantufa_gasp:645104302460895272>"
					'B' -> "<a:gabi_calca_1:647982987937447937>"
					'D' -> "<:dokyo_funkeiro:603389181808607262>"
					'G' -> "<:gesso:523176710439567392>"
					'T' -> "<:tobias_nosa:450476856303419432>"
					else -> throw RuntimeException("I don't know what emote is for $char")
				}
			}

			val id = transaction(Databases.loritta) {
				Raspadinhas.insertAndGetId {
					it[receivedById] = context.userHandle.idLong
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

			val message = context.sendMessage("${Emotes.LORI_RICH} **|** ${context.getAsMention(false)} aqui está a sua raspadinha com número $id! Raspe clicando na parte cinza e, se o seu cartão for premiado com combinações de emojis na horizontal/vertical/diagonal, clique em ${Emotes.LORI_RICH} para receber a sua recompensa! Mas cuidado, não tente resgatar prêmios de uma raspadinha que não tem prêmios!! Se você quiser comprar um novo ticket pagando 125 sonhos, aperte em \uD83D\uDD04!!\n$scratchCardTemplate")
			message.handle.onReactionAddByAuthor(context) {
				if (it.reactionEmote.isEmote("\uD83D\uDD04")) {
					buyRaspadinha(context, LorittaLauncher.loritta.getOrCreateLorittaProfile(context.handle.idLong))
				}

				if (it.reactionEmote.isEmote("593979718919913474")) {
					checkRaspadinha(context, LorittaLauncher.loritta.getOrCreateLorittaProfile(context.handle.idLong), id.value)
				}
			}
			message.handle.addReaction("lori_rica:593979718919913474").queue()
			message.handle.addReaction("\uD83D\uDD04").queue()
		}
	}

	private suspend fun checkRaspadinha(context: DiscordCommandContext, profile: Profile, id: Long?) {
		mutex.withLock {
			val raspadinha = transaction(Databases.loritta) {
				Raspadinhas.select {
					Raspadinhas.id eq id
				}.firstOrNull()
			}

			if (raspadinha == null) {
				context.reply(
						LoriReply(
								"Essa raspadinha não existe!",
								Constants.ERROR
						)
				)
				return@withLock
			}

			if (raspadinha[Raspadinhas.receivedById] != context.userHandle.idLong) {
				context.reply(
						LoriReply(
								"Nossa, não sabia que você era assim... tentando roubar os prêmios de outras raspadinhas que não foi você que comprou...",
								Constants.ERROR
						)
				)
				return@withLock
			}

			if (raspadinha[Raspadinhas.scratched]) {
				context.reply(
						LoriReply(
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

			transaction(Databases.loritta) {
				Raspadinhas.update({ Raspadinhas.id eq id }) {
					it[scratched] = true
					it[value] = prize
				}
			}

			if (prize == 0) {
				transaction(Databases.loritta) {
					if (1000 > profile.money) {
						profile.money = 0.0
					} else {
						profile.money -= 1000
					}
				}

				context.reply(
						LoriReply(
								"Qual parte de *não resgate um prêmio se você não ganhou* você não entendeu? Só por fazer perder o meu tempo, você perdeu 1000 sonhos. ${Emotes.LORI_SHRUG}"
						)
				)
			} else {
				ScratchCardCommand.logger.info { "User ${context.userHandle.idLong} won $prize sonhos in the raspadinha! Combos: Lori: $loriCombos; Pantufa: $pantufaCombos; Gabi: $gabiCombos; Dokyo: $dokyoCombos; Gessy: $gessyCombos; Tobias: $tobiasCombos" }
				transaction(Databases.loritta) {
					profile.money += prize
				}
				context.reply(
						LoriReply(
								"Parabéns, você ganhou **$prize sonhos** na sua raspadinha!",
								Emotes.LORI_PAT
						)
				)
			}
		}
	}
}