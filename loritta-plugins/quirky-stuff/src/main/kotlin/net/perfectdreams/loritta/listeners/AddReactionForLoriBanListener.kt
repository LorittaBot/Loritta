package net.perfectdreams.loritta.listeners

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.QuirkyConfig
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.transactions.transaction

class AddReactionForLoriBanListener(val config: QuirkyConfig) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (!config.loriBan.enabled
				|| event.channel.idLong !in config.loriBan.channels
				|| !event.reactionEmote.isEmote
				|| event.reactionEmote.idLong != 593161404937404416L)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val message = event.channel.retrieveMessageById(event.messageIdLong)
					.await()

			val split = message.contentRaw.split(" ")
			val toBeBannedUserId = split[0]
			val args = split.toMutableList().apply { this.removeAt (0) }

			val profile = loritta.getLorittaProfile(toBeBannedUserId) ?: return@launch

			if (profile.isBanned)
				return@launch

			val users = event.reaction.retrieveUsers().await()
					.filter { !it.isBot }

			val count = users.size

			val shouldBeBanned = if (users.any { loritta.config.isOwner(it.idLong) }) {
				true
			} else {
				count >= config.loriBan.requiredReactionCount
			}

			logger.info { "Trying to ban user ${profile.id.value}... count is $count. Can we ban? $shouldBeBanned"}

			if (shouldBeBanned) {
				val reason = args.joinToString(" ")
				logger.info { "Banning ${profile.id.value} with reason ${reason}, message sent by ${message.idLong}" }

				transaction(Databases.loritta) {
					profile.isBanned = true
					profile.bannedReason = reason
				}

				event.channel.sendMessage("Usuário ${profile.id.value} foi banido com sucesso. Obrigada por ter reportado o usuário! ${Emotes.LORI_HEART}")
						.queue()
			}
		}
	}
}