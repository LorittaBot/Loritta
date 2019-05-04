package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
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
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

class BomDiaECia {
	companion object {
		val randomTexts = listOf(
				"amo o yudi!",
				"amo o yudi",
				"amo a priscilla!",
				"amo a priscilla",
				"amo o yudi e a priscilla!",
				"amo o yudi e a priscilla",
				"bts? eu só conheço o sbt!",
				"preisteicho dois!",
				"preisteicho treis!",
				"preisteicho!",
				"playstation dois!",
				"playstation treis!",
				"playstation!",
				"é o funk do yudi que vai dar preisteicho dois!",
				"é o funk do yudi que vai dar preisteicho 2!",
				"é o funk do yudi que vai dar preisteicho 3!",
				"é o funk do yudi que vai dar preisteicho treis!",
				"é o funk do yudi que vai dar playstation dois!",
				"é o funk do yudi que vai dar playstation 2!",
				"é o funk do yudi que vai dar playstation 3!",
				"é o funk do yudi que vai dar playstation treis!",
				"não quero ganhar um jogo da vida!",
				"alôoooo, cê tá me escutando?"
		)
		val randomImages by lazy {
			listOf(
					"${Loritta.config.loritta.website.url}assets/img/bom-dia-cia.jpg",
					"${Loritta.config.loritta.website.url}assets/img/bom-dia-cia-2.jpg",
					"${Loritta.config.loritta.website.url}assets/img/bom-dia-cia-3.jpg",
					"${Loritta.config.loritta.website.url}assets/img/bom-dia-cia-4.jpg"
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