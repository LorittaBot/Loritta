package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.threads.BomDiaECiaThread
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import java.awt.Color
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap

class BomDiaECia() {
	val thread: BomDiaECiaThread = BomDiaECiaThread()

	init {
		thread.start()
	}

	var activeTextChannels = ConcurrentHashMap<String, YudiTextChannelInfo>()
	var available = false

	var randomTexts = mutableListOf(
			"amo o yudi e a priscilla!",
			"bts? eu só conheço o sbt!",
			"preisteicho dois!",
			"preisteicho treis!",
			"preisteicho!",
			"não quero ganhar um jogo da vida!"
	)

	var currentText = randomTexts[0]

	val logger by logger()

	fun handleBomDiaECia(forced: Boolean) {
		if (forced)
			thread.interrupt()

		logger.info("Vamos anunciar o Bom Dia & Cia! (Agora é a hora!)")

		val validTextChannels = getActiveTextChannels()

		available = true

		val embedsForLocales = mutableMapOf<String, MessageEmbed>()

		currentText = randomTexts[RANDOM.nextInt(randomTexts.size)]

		val obfuscatedText = currentText.toCharArray()
				.joinToString("\u200B")

		loritta.locales.forEach { localeId, locale ->
			val embed = EmbedBuilder()
			embed.setTitle("<:sbt:447560158344904704> Bom Dia & Cia")
			embed.setDescription("Você aí de casa querendo prêmios agora, neste instante? Então ligue para o Bom Dia & Cia! Corra que apenas a primeira pessoa que ligar irá ganhar prêmios! (Cada tentativa de ligação custa **75 Sonhos**!) `+ligar 4002-8922 ${obfuscatedText}`")
			embed.setImage("https://loritta.website/assets/img/bom-dia-cia.jpg")
			embed.setColor(Color(74, 39, 138))

			embedsForLocales[localeId] = embed.build()
		}

		validTextChannels.forEach {
			// TODO: Localization!
			try {
				it.sendMessage(embedsForLocales["default"]).queue()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		if (thread.isInterrupted)
			thread.start()
	}

	@Synchronized
	fun announceWinner(guild: Guild, user: User) {
		val validTextChannels = getActiveTextChannels()

		activeTextChannels.clear()

		val messageForLocales = mutableMapOf<String, Message>()

		loritta.locales.forEach { localeId, locale ->
			val message = MessageBuilder().append("<:yudi:446394608256024597> **|** Parabéns `${user.name.stripCodeMarks()}#${user.discriminator}` por ter ligado primeiro no `${guild.name.stripCodeMarks()}`!")

			messageForLocales[localeId] = message.build()
		}

		validTextChannels.forEach {
			// TODO: Localization!
			it.sendMessage(messageForLocales["default"]).queue()
		}
	}

	fun getActiveTextChannels(): Set<TextChannel> {
		val validTextChannels = mutableSetOf<TextChannel>()

		activeTextChannels.entries.forEach {
			val textChannel = lorittaShards.getTextChannelById(it.key)

			if (textChannel != null && textChannel.canTalk() && textChannel.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				if (it.value.users.size >= 10 && it.value.lastMessageSent > (System.currentTimeMillis() - 180000)) {
					val serverConfig = loritta.getServerConfigForGuild(textChannel.guild.id)

					if (serverConfig.miscellaneousConfig.enableBomDiaECia) {
						validTextChannels.add(textChannel)
					}
				}
			}
		}

		return validTextChannels
	}

	class YudiTextChannelInfo {
		val users = ConcurrentHashMap.newKeySet<User>()
		var lastMessageSent = System.currentTimeMillis()
	}
}