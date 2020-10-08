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
import net.perfectdreams.loritta.tables.BannedUsers
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class CheckReactionsForLoriBanListener(val config: StaffLorittaBanConfig) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (!config.enabled
				|| event.channel.idLong !in config.channels
				|| !event.reactionEmote.isEmote
				|| event.reactionEmote.idLong != 686370257308483612L)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val message = event.channel.retrieveMessageById(event.messageIdLong)
					.await()

			if (message.reactions.any { it.reactionEmote.isEmote("lori_brava") })
				return@launch

			val split = message.contentRaw.split(" ")
			val isUnban = split[0] == "unban"
			val toBeBannedUserId = split[1]
			val args = split.toMutableList()
					.apply { this.removeAt(0) }
					.apply { this.removeAt(0) }

			val profile = loritta.getLorittaProfile(toBeBannedUserId) ?: return@launch
			val bannedState = profile.getBannedState()

			if (isUnban) {
				if (bannedState == null)
					return@launch
			} else {
				if (bannedState != null)
					return@launch
			}

			val users = event.reaction.retrieveUsers().await()
					.filter { !it.isBot }

			val count = users.size

			val shouldBeBanned = if (users.any { loritta.config.isOwner(it.idLong) }) {
				true
			} else {
				count >= config.requiredReactionCount
			}

			logger.info { "Trying to ban (is unban? $isUnban) user ${profile.id.value}... count is $count. Can we ban? $shouldBeBanned"}

			if (shouldBeBanned) {
				val reason = args.joinToString(" ")
				logger.info { "Banning (is unban $isUnban) ${profile.id.value} with reason ${reason}, message sent by ${message.idLong}" }

				if (isUnban && bannedState != null) {
					transaction(Databases.loritta) {
						BannedUsers.deleteWhere {
							BannedUsers.id eq bannedState[BannedUsers.id]
						}
					}

					event.channel.sendMessage("Usuário ${profile.id.value} foi desbanido com sucesso. Obrigada por ter corrigido a cagada de alguém... eu acho né... ${Emotes.LORI_COFFEE}")
							.queue()
				} else {
					transaction(Databases.loritta) {
						BannedUsers.insert {
							it[userId] = profile.userId
							it[bannedAt] = System.currentTimeMillis()
							it[bannedBy] = message.author.idLong
							it[valid] = true
							it[expiresAt] = null
							it[BannedUsers.reason] = reason
						}
					}

					event.channel.sendMessage("Usuário ${profile.id.value} foi banido com sucesso. Obrigada por ter reportado o usuário! ${Emotes.LORI_HEART}")
							.queue()
				}

				message.addReaction("lori_brava:556525700425711636").queue()
			}
		}
	}
}