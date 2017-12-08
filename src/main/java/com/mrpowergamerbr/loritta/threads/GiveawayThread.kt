package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.vanilla.`fun`.GiveawayCommand
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save

class GiveawayThread : Thread() {
	override fun run() {
		super.run()
		while (true) {
			try {
				processGiveaways()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(5000)
		}
	}

	fun processGiveaways() {
		val configs = loritta.ds.find(ServerConfig::class.java)
				.field("giveaways")
				.exists()

		for (config in configs) {
			val guild = LORITTA_SHARDS.getGuildById(config.guildId) ?: continue
			val toRemove = mutableListOf<GiveawayCommand.Giveaway>()
			for (giveaway in config.giveaways) {
				val textChannel = guild.getTextChannelById(giveaway.channelId)

				if (textChannel == null) {
					toRemove.add(giveaway)
					continue
				}

				val message = textChannel.getMessageById(giveaway.messageId).complete()

				if (message == null) {
					toRemove.add(giveaway)
					continue
				}

				if (System.currentTimeMillis() > giveaway.finishAt) {
					toRemove.add(giveaway)

					val users = message.reactions[0].users.complete().toMutableList()

					message.delete().queue()

					users.remove(guild.selfMember.user) // Não reagir com a Loritta

					if (users.size >= giveaway.userCount) {
						val winner = users[RANDOM.nextInt(users.size)]

						textChannel.sendMessage("Parabéns ${winner.name} por ganhar o giveaway!").queue()
					}
				} else {
					val embed = GiveawayCommand.createEmbed(giveaway.reason, giveaway.description, giveaway.reaction, giveaway.finishAt)

					message.editMessage(embed).queue()
				}
			}

			if (toRemove.isNotEmpty()) {
				config.giveaways.removeAll(toRemove)
				loritta save config
			}
		}
	}
}