package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.threads.BomDiaECiaThread
import com.mrpowergamerbr.loritta.utils.extensions.getRandom
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.extensions.stripLinks
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.Emotes
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

class BomDiaECia {
	companion object {
		val randomTexts = mutableListOf<String>().apply {
			fun addWithVariations(source: String) {
				this.add(source)
				this.add("$source!")
			}

			// Gerar todas as mensagens possíveis do bom dia & cia
			// "alôoooo, cê tá me escutando?"
			for (i in 0..10) {
				var str = "alô"
				repeat(i) {
					str += "o"
				}
				this.add("$str, cê tá me escutando?")
				this.add("$str, cê está me escutando?")
				this.add("$str, você tá me escutando?")
				this.add("$str, você está me escutando?")
			}

			val ilove = listOf(
					"o yudi",
					"a priscilla",
					"o yudi tamashiro",
					"a priscilla alcântara",
					"o sbt",
					"o bom dia & cia",
					"o bom dia e cia",
					"o bom dia & companhia",
					"o yudi e a priscilla",
					"o yudi tamashiro e a priscilla",
					"o yudi e a priscilla alcântara",
					"o yudi tamashiro e a priscilla alcântara"
			)

			for (person in ilove) {
				addWithVariations("eu gosto d$person")
				addWithVariations("eu amo $person")
				addWithVariations("eu adoro $person!")
				addWithVariations("eu idolatro $person")

				addWithVariations("gosto d$person")
				addWithVariations("amo $person")
				addWithVariations("adoro $person!")
				addWithVariations("idolatro $person")
			}

			val playstations = listOf(
					"playstation",
					"preisteicho",
					"preisteixo",
					"prêiesteicho",
					"prêisteixo",
					"playesteicho",
					"preíesteichu"
			)

			val numbers = listOf(
					"1",
					"2",
					"3",
					"4",
					"um",
					"dois",
					"três",
					"treis",
					"quatro"
			)

			for (playstation in playstations) {
				addWithVariations(playstation)
				addWithVariations("quero ganhar 1 $playstation")
				addWithVariations("quero ganhar um $playstation")
				addWithVariations("eu quero ganhar um $playstation")
				addWithVariations("eu quero ganhar 1 $playstation")
				addWithVariations("não quero ganhar um jogo da vida, quero ganhar 1 $playstation")
				addWithVariations("não quero ganhar um jogo da vida, quero ganhar um $playstation")
				addWithVariations("não quero ganhar um jogo da vida, eu quero ganhar um $playstation")
				addWithVariations("não quero ganhar um jogo da vida, eu quero ganhar 1 $playstation")

				for (number in numbers) {
					addWithVariations("$playstation $number")
					addWithVariations("quero ganhar 1 $playstation $number")
					addWithVariations("quero ganhar um $playstation $number")
					addWithVariations("eu quero ganhar um $playstation $number")
					addWithVariations("eu quero ganhar 1 $playstation $number")

					addWithVariations("não quero ganhar um jogo da vida, quero ganhar 1 $playstation $number")
					addWithVariations("não quero ganhar um jogo da vida, quero ganhar um $playstation $number")
					addWithVariations("não quero ganhar um jogo da vida, eu quero ganhar um $playstation $number")
					addWithVariations("não quero ganhar um jogo da vida, eu quero ganhar 1 $playstation $number")

					addWithVariations("4002-8922 é o funk do yudi que vai dar $playstation $number")
					addWithVariations("4002-8922 é o funk do japonês que vai te dar $playstation $number")
				}
			}

			this.add("bts? eu só conheço o sbt!")
		}

		val randomImages by lazy {
			listOf(
					"${loritta.instanceConfig.loritta.website.url}assets/img/bom-dia-cia.jpg",
					"${loritta.instanceConfig.loritta.website.url}assets/img/bom-dia-cia-2.jpg",
					"${loritta.instanceConfig.loritta.website.url}assets/img/bom-dia-cia-3.jpg",
					"${loritta.instanceConfig.loritta.website.url}assets/img/bom-dia-cia-4.jpg"
			)
		}
	}

	val thread: BomDiaECiaThread = BomDiaECiaThread()

	init {
		thread.start()
	}

	var activeTextChannels = ConcurrentHashMap<String, YudiTextChannelInfo>()
	var triedToCall = mutableSetOf<Long>()
	var lastBomDiaECia = 0L
	var available = false

	var currentText = randomTexts[0]

	private val logger = KotlinLogging.logger {}

	fun handleBomDiaECia(forced: Boolean) {
		if (forced)
			thread.interrupt()

		triedToCall.clear()

		logger.info("Vamos anunciar o Bom Dia & Cia! (Agora é a hora!)")

		val validTextChannels = getActiveTextChannels()

		available = true

		currentText = randomTexts[RANDOM.nextInt(randomTexts.size)]

		lastBomDiaECia = System.currentTimeMillis()

		val obfuscatedText = currentText.toCharArray()
				.joinToString("", transform = {
					val obfIdx = RANDOM.nextInt(3)

					if (obfIdx == 2)
						"\u200B$it"
					else if (obfIdx == 1)
						"\u200D$it"
					else
						"\u200C$it"
				})

		validTextChannels.forEach { textChannel ->
			// TODO: Localization!
			try {
				val embed = EmbedBuilder()
				embed.setTitle("<:sbt:447560158344904704> Bom Dia & Cia")
				embed.setDescription("Você aí de casa querendo prêmios agora, neste instante? Então ligue para o Bom Dia & Cia! Corra que apenas a primeira pessoa que ligar irá ganhar prêmios! (Cada tentativa de ligação custa **75 Sonhos**!) `${activeTextChannels[textChannel.id]?.prefix ?: "+"}ligar 4002-8922 $obfuscatedText`")
				embed.setImage(randomImages.getRandom())
				embed.setColor(Color(74, 39, 138))

				textChannel.sendMessage(embed.build()).queue()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		if (thread.isInterrupted)
			thread.start()
	}

	@Synchronized
	fun announceWinner(channel: TextChannel, guild: Guild, user: User) {
		val validTextChannels = getActiveTextChannels()

		activeTextChannels.clear()

		val messageForLocales = mutableMapOf<String, Message>()

		loritta.legacyLocales.forEach { localeId, locale ->
			val message = MessageBuilder().append("<:yudi:446394608256024597> **|** Parabéns `${user.name.stripCodeMarks()}#${user.discriminator}` por ter ligado primeiro no `${guild.name.stripCodeMarks().stripLinks()}`!")

			messageForLocales[localeId] = message.build()
		}

		validTextChannels.forEach {
			// TODO: Localization!
			it.sendMessage(messageForLocales["default"]!!).queue()
		}

		GlobalScope.launch(loritta.coroutineDispatcher) {
			delay(30000)
			if (triedToCall.isNotEmpty()) {
				channel.sendMessage("<:yudi:446394608256024597> **|** Sabia que o ${user.asMention} foi o primeiro de **${triedToCall.size} usuários** a conseguir ligar primeiro no Bom Dia & Cia? ${Emotes.LORI_OWO}").queue { message ->
					if (message.guild.selfMember.hasPermission(Permission.MESSAGE_ADD_REACTION)) {
						message.onReactionAddByAuthor(user.id) {
							if (it.reactionEmote.isEmote("⁉")) {
								loritta.messageInteractionCache.remove(it.messageIdLong)

								val triedToCall = triedToCall.mapNotNull { lorittaShards.getUserById(it) }
								channel.sendMessage("<:yudi:446394608256024597> **|** Pois é, ${triedToCall.joinToString(", ", transform = { "`" + it.name + "`" })} tentaram ligar... mas falharam!").queue()
							}
						}
						message.addReaction("⁉").queue()
					}
				}
			}
		}
	}

	fun getActiveTextChannels(): Set<TextChannel> {
		val validTextChannels = mutableSetOf<TextChannel>()

		activeTextChannels.entries.forEach {
			val textChannel = lorittaShards.getTextChannelById(it.key)

			if (textChannel != null && textChannel.canTalk() && textChannel.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				if (it.value.users.size >= 5 && it.value.lastMessageSent > (System.currentTimeMillis() - 180000)) {
					val serverConfig = loritta.getServerConfigForGuild(textChannel.guild.id)

					if (serverConfig.miscellaneousConfig.enableBomDiaECia) {
						validTextChannels.add(textChannel)
					}
				}
			}
		}

		return validTextChannels
	}

	class YudiTextChannelInfo(val prefix: String) {
		val users = ConcurrentHashMap.newKeySet<User>()
		var lastMessageSent = System.currentTimeMillis()
	}
}