package net.perfectdreams.loritta.morenitta.utils

import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.LorittaBot.Companion.RANDOM
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MiscellaneousConfig
import net.perfectdreams.loritta.morenitta.threads.BomDiaECiaThread
import net.perfectdreams.loritta.morenitta.utils.extensions.asUserNameCodeBlockPreviewTag
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.queueAfterWithMessagePerSecondTarget
import net.perfectdreams.loritta.morenitta.utils.extensions.stripLinks
import net.perfectdreams.loritta.morenitta.utils.locale.getPronoun
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

class BomDiaECia(val loritta: LorittaBot) {
	val thread: BomDiaECiaThread = BomDiaECiaThread(loritta)

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
			"a priscilla alcantara",
			"o sbt",
			"o bom dia & cia",
			"o bom dia e cia",
			"o bom dia & companhia",
			"o yudi e a priscilla",
			"o yudi tamashiro e a priscilla",
			"o yudi e a priscilla alcantara",
			"o yudi tamashiro e a priscilla alcantara"
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
			"5",
			"um",
			"one",
			"dois",
			"doís",
			"two",
			"três",
			"treis",
			"three",
			"quatro",
			"quatru",
			"four",
			"cinco",
			"cincu",
			"five"
		)

		val otherStuff = listOf(
			"jogo da vida",
			"banco imobiliário",
			"tablet",
			"notebook",
			"laptop",
			"celular",
			"skate",
			"mp4",
			"mp3",
			"patins"
		)

		for (playstation in playstations) {
			addWithVariations("quero ganhar 1 $playstation")
			addWithVariations("quero ganhar um $playstation")
			addWithVariations("eu quero ganhar um $playstation")
			addWithVariations("eu quero ganhar 1 $playstation")
			addWithVariations(playstation)

			for (game in otherStuff) {
				addWithVariations("não quero ganhar um $game, quero ganhar 1 $playstation")
				addWithVariations("não quero ganhar 1 $game, quero ganhar 1 $playstation")
				addWithVariations("não quero ganhar um $game, quero ganhar um $playstation")
				addWithVariations("não quero ganhar 1 $game, quero ganhar um $playstation")
				addWithVariations("não quero ganhar um $game, eu quero ganhar um $playstation")
				addWithVariations("não quero ganhar 1 $game, eu quero ganhar um $playstation")
				addWithVariations("não quero ganhar um $game, eu quero ganhar 1 $playstation")
				addWithVariations("não quero ganhar 1 $game, eu quero ganhar 1 $playstation")

				addWithVariations("quero ganhar 1 $playstation, e não quero ganhar um $game")
				addWithVariations("quero ganhar 1 $playstation, e não quero ganhar 1 $game")
				addWithVariations("quero ganhar um $playstation, e não quero ganhar um $game")
				addWithVariations("quero ganhar um $playstation, e não quero ganhar 1 $game")
				addWithVariations("eu quero ganhar um $playstation, e não quero ganhar um $game")
				addWithVariations("eu quero ganhar um $playstation, e não quero ganhar 1 $game")
				addWithVariations("eu quero ganhar 1 $playstation, não quero ganhar um $game")
				addWithVariations("eu quero ganhar 1 $playstation, não quero ganhar 1 $game")
			}

			for (number in numbers) {
				addWithVariations("$playstation $number")
				addWithVariations("quero ganhar 1 $playstation $number")
				addWithVariations("quero ganhar um $playstation $number")
				addWithVariations("eu quero ganhar um $playstation $number")
				addWithVariations("eu quero ganhar 1 $playstation $number")

				for (game in otherStuff) {
					addWithVariations("não quero ganhar um $game, quero ganhar 1 $playstation $number")
					addWithVariations("não quero ganhar 1 $game, quero ganhar 1 $playstation $number")
					addWithVariations("não quero ganhar um $game, quero ganhar um $playstation $number")
					addWithVariations("não quero ganhar 1 $game, quero ganhar um $playstation $number")
					addWithVariations("não quero ganhar um $game, eu quero ganhar um $playstation $number")
					addWithVariations("não quero ganhar 1 $game, eu quero ganhar um $playstation $number")
					addWithVariations("não quero ganhar um $game, eu quero ganhar 1 $playstation $number")
					addWithVariations("não quero ganhar 1 $game, eu quero ganhar 1 $playstation $number")
				}

				addWithVariations("4002-8922 é o funk do yudi que vai dar $playstation $number")
				addWithVariations("4002-8922 é o funk do japonês que vai te dar $playstation $number")
			}
		}

		this.add("bts? eu só conheço o sbt!")
		this.add("bts? eu só conheço o sbt do Silvio Santos!")
	}

	val randomImages = listOf(
		"https://stuff.loritta.website/bom-dia-e-cia/bom-dia-e-cia-1.png",
		"https://stuff.loritta.website/bom-dia-e-cia/bom-dia-e-cia-2.png",
		"https://stuff.loritta.website/bom-dia-e-cia/bom-dia-e-cia-3.png",
		"https://stuff.loritta.website/bom-dia-e-cia/bom-dia-e-cia-4.png"
	)

	init {
		thread.start()
	}

	var activeTextChannels = ConcurrentHashMap<String, YudiTextChannelInfo>()
	var triedToCall = mutableSetOf<Long>()
	var lastBomDiaECia = 0L
	var available = false

	var currentText = randomTexts[0]

	private val logger by HarmonyLoggerFactory.logger {}

	var validTextChannels: Set<TextChannel>? = null

	fun handleBomDiaECia(forced: Boolean) {
		if (forced)
			thread.interrupt()

		triedToCall.clear()

		logger.info { "Vamos anunciar o Bom Dia & Cia! (Agora é a hora!)" }

		val validTextChannels = getActiveTextChannels()
		this.validTextChannels = validTextChannels

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
				embed.setImage(randomImages.random())
				embed.setColor(Color(74, 39, 138))

				textChannel.sendMessage(MessageCreateBuilder().setEmbeds(embed.build()).build()).queue()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		if (thread.isInterrupted)
			thread.start()
	}

	@Synchronized
	fun announceWinner(channel: GuildMessageChannelUnion, guild: Guild, user: User) {
		activeTextChannels.clear()

		val validTextChannels = this.validTextChannels
			?: return // If there isn't any valid active channels, we don't need to announce the winner

		val messageForLocales = mutableMapOf<String, MessageCreateData>()

		loritta.legacyLocales.forEach { localeId, locale ->
			val message = MessageCreateBuilder()
				.addContent("<:yudi:446394608256024597> **|** Parabéns ${user.asUserNameCodeBlockPreviewTag()} por ter ligado primeiro em `${guild.name.stripCodeMarks().stripLinks()}` (`${guild.id}`)!")
				.setAllowedMentions(listOf(Message.MentionType.EMOJI))
			messageForLocales[localeId] = message.build()
		}

		validTextChannels.forEachIndexed { index, textChannel ->
			// TODO: Localization!
			textChannel.sendMessage(messageForLocales["default"]!!)
				.queueAfterWithMessagePerSecondTarget(index)
		}

		GlobalScope.launch(loritta.coroutineDispatcher) {
			delay(30000)
			if (triedToCall.isNotEmpty()) {
				val pronoun = loritta.newSuspendedTransaction {
					loritta.getOrCreateLorittaProfile(user.idLong).settings.gender.getPronoun(loritta.localeManager.getLocaleById("default"))
				}

				channel.sendMessage(
					MessageCreate {
						styled(
							"Sabia que ${user.asMention} foi $pronoun primeir$pronoun de **${triedToCall.size} usuários** a conseguir ligar no Bom Dia & Cia? ${Emotes.LORI_OWO}",
							"<:yudi:446394608256024597>"
						)

						actionRow(
							loritta.interactivityManager
								.button(
									false,
									ButtonStyle.SECONDARY,
									builder = {
										emoji = Emoji.fromUnicode("⁉")
									}
								) {
									it.deferChannelMessage(true)

									val triedToCall = triedToCall.mapNotNull {
										HarmonyLoggerFactory.logger {}.value.info { "BomDiaECia#retrieveUserInfoById - UserId: ${it}" }
										loritta.lorittaShards.retrieveUserInfoById(it)
									}

									it.reply(true) {
										styled(
											"Pois é, ${triedToCall.joinToString(", ", transform = { "`" + it.name + "`" })} tentaram ligar... mas falharam!",
											"<:yudi:446394608256024597>",
										)
									}
								}
						)
					}
				).await()
			}
		}

		this.validTextChannels = null
	}

	fun getActiveTextChannels(): Set<TextChannel> {
		val validTextChannels = mutableSetOf<TextChannel>()

		activeTextChannels.entries.forEach {
			val textChannel = loritta.lorittaShards.getTextChannelById(it.key)

			if (textChannel != null && textChannel.canTalk() && textChannel.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				if (it.value.users.size >= 5 && it.value.lastMessageSent > (System.currentTimeMillis() - 180000)) {
					val serverConfig = runBlocking { loritta.getOrCreateServerConfig(textChannel.guild.idLong) }
					val miscellaneousConfig = runBlocking { serverConfig.getCachedOrRetreiveFromDatabase<MiscellaneousConfig?>(loritta, ServerConfig::miscellaneousConfig) }

					val enableBomDiaECia = miscellaneousConfig?.enableBomDiaECia ?: false

					if (enableBomDiaECia)
						validTextChannels.add(textChannel)
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