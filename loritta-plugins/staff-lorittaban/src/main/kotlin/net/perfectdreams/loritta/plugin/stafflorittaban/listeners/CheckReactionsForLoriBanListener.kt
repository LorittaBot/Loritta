package net.perfectdreams.loritta.plugin.stafflorittaban.listeners

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.plugin.stafflorittaban.StaffLorittaBanConfig
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.transactions.transaction

class CheckReactionsForLoriBanListener(val config: StaffLorittaBanConfig) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (!config.enabled
				|| event.channel.idLong !in config.channels
				|| !event.reactionEmote.isEmote
				|| event.reactionEmote.idLong != 593161404937404416L)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val message = event.channel.retrieveMessageById(event.messageIdLong)
					.await()

			if (message.reactions.any { it.reactionEmote.isEmote("lori_brava") })
				return@launch

			val split = message.contentRaw.split(" ")
			val toBeBannedUserId = split[0]
			val args = split.toMutableList().apply { this.removeAt(0) }

			val profile = loritta.getLorittaProfile(toBeBannedUserId) ?: return@launch

			if (profile.isBanned)
				return@launch

			val users = event.reaction.retrieveUsers().await()
					.filter { !it.isBot }

			val count = users.size

			val shouldBeBanned = if (users.any { loritta.config.isOwner(it.idLong) }) {
				true
			} else {
				count >= config.requiredReactionCount
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

				message.addReaction("lori_brava:556525700425711636").queue()
			}
		}
	}
}