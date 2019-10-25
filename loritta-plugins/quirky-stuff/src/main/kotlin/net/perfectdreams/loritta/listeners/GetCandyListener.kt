package net.perfectdreams.loritta.listeners

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.Halloween2019ProfileCreator
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.QuirkyConfig
import net.perfectdreams.loritta.dao.Giveaway
import net.perfectdreams.loritta.tables.CollectedCandies
import net.perfectdreams.loritta.tables.Giveaways
import net.perfectdreams.loritta.tables.Halloween2019Players
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class GetCandyListener(val config: QuirkyConfig) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val mutex = Mutex()
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (event.reactionEmote.name != "\uD83C\uDF6C")
			return

		if (event.user.isBot)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val lorittaProfile = loritta.getLorittaProfile(event.user.idLong) ?: return@launch

			val isParticipating = transaction(Databases.loritta) {
				Halloween2019Players.select {
					Halloween2019Players.user eq lorittaProfile.id
				}.count() != 0
			}

			val getTheCandy = isParticipating && Calendar.getInstance()[Calendar.MONTH] == 9

			if (!getTheCandy)
				return@launch

			val isGiveaway = transaction(Databases.loritta) {
				Giveaway.find {
					Giveaways.messageId eq event.messageIdLong
				}.count() != 0
			}

			if (isGiveaway)
				return@launch

			if (!event.reaction.retrieveUsers().await().any { it.id == loritta.discordConfig.discord.clientId })
				return@launch

			mutex.withLock {
				val collectedCandies = transaction(Databases.loritta) {
					CollectedCandies.select {
						(CollectedCandies.user eq lorittaProfile.id.value) and
								(CollectedCandies.guildId eq event.guild.idLong) and
								(CollectedCandies.channelId eq event.channel.idLong) and
								(CollectedCandies.messageId eq event.messageIdLong)
					}.toMutableList()
				}

				if (collectedCandies.isEmpty()) {
					transaction(Databases.loritta) {
						CollectedCandies.insert {
							it[user] = lorittaProfile.id
							it[guildId] = event.guild.idLong
							it[channelId] = event.channel.idLong
							it[messageId] = event.messageIdLong
						}
					}

					val howMuchDidTheUserCollect = transaction(Databases.loritta) {
						CollectedCandies.select {
							(CollectedCandies.user eq lorittaProfile.id.value)
						}.count()
					}

					val dreams = howMuchDidTheUserCollect % 125

					if (dreams == 0) {
						transaction(Databases.loritta) {
							lorittaProfile.money += 10_000
						}

						try {
							event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect doces**, como recompensa você ganhou **10k sonhos**!\n\nAgora me dê esses doces para eu poder me esbaldar neles. ${Emotes.LORI_TEMMIE}")
									.await()
						} catch (e: Exception) {}
					}

					if (howMuchDidTheUserCollect == 400) {
						try {
							event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect doces**, como recompensa você ganhou uma badge exclusiva para o seu `+perfil`!\n\nAgora me dê esses doces para eu poder me esbaldar neles. ${Emotes.LORI_TEMMIE}")
									.await()
						} catch (e: Exception) {}
					}

					if (howMuchDidTheUserCollect == 777) {
						transaction(Databases.loritta) {
							val settings = lorittaProfile.settings
							settings.boughtProfiles = settings.boughtProfiles.toMutableList().apply { this.add(Halloween2019ProfileCreator::class.simpleName!!) }.toTypedArray()
						}

						try {
							event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect doces**, como recompensa você ganhou um design exclusivo para o seu `+perfil`! Ative ele em <https://loritta.website/user/@me/dashboard/profiles>\n\nAgora me dê esses doces para eu poder me esbaldar neles. ${Emotes.LORI_TEMMIE}\n\nFeliz Halloween!")
									.await()
						} catch (e: Exception) {}
					}
				}
			}
		}
	}
}