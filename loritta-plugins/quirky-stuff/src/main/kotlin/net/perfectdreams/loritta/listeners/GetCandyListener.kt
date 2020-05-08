package net.perfectdreams.loritta.listeners

import com.google.common.cache.CacheBuilder
import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.Halloween2019
import net.perfectdreams.loritta.QuirkyConfig
import net.perfectdreams.loritta.dao.servers.Giveaway
import net.perfectdreams.loritta.tables.CollectedCandies
import net.perfectdreams.loritta.tables.Halloween2019Players
import net.perfectdreams.loritta.tables.servers.Giveaways
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.TimeUnit

class GetCandyListener(val config: QuirkyConfig) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val mutexes = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build<Long, Mutex>()
				.asMap()
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (event.reactionEmote.name !in Halloween2019.CANDIES && event.reactionEmote.name != Halloween2019.SPECIAL_CANDY)
			return

		if (event.user.isBot)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val lorittaProfile = loritta.getLorittaProfile(event.user.idLong) ?: return@launch

			val isParticipating = transaction(Databases.loritta) {
				Halloween2019Players.select {
					Halloween2019Players.user eq lorittaProfile.id
				}.count() != 0L
			}

			val getTheCandy = isParticipating && Calendar.getInstance()[Calendar.MONTH] == 9

			if (!getTheCandy)
				return@launch

			val isGiveaway = transaction(Databases.loritta) {
				Giveaway.find {
					Giveaways.messageId eq event.messageIdLong
				}.count() != 0L
			}

			if (isGiveaway)
				return@launch

			if (!event.reaction.retrieveUsers().await().any { it.id == loritta.discordConfig.discord.clientId })
				return@launch

			val message = event.channel.retrieveMessageById(event.messageIdLong).await()

			if (System.currentTimeMillis() - 900_000 >= message.timeCreated.toInstant().toEpochMilli()) {
				try {
					event.user.openPrivateChannel().await().sendMessage("O doce que você coletou está velho demais!")
							.await()
				} catch (e: Exception) {}
				return@launch
			}

			val mutex = mutexes.getOrPut(event.user.idLong, { Mutex() })

			mutex.withLock {
				val count = if (event.reactionEmote.name == Halloween2019.SPECIAL_CANDY) {
					RANDOM.nextInt(5, 16)
				} else {
					1
				}

				for (it in 0 until count) {
					val collectedCandies = if (it == 0) {
						transaction(Databases.loritta) {
							CollectedCandies.select {
								(CollectedCandies.user eq lorittaProfile.id.value) and
										(CollectedCandies.guildId eq event.guild.idLong) and
										(CollectedCandies.channelId eq event.channel.idLong) and
										(CollectedCandies.messageId eq event.messageIdLong)
							}.toMutableList()
						}
					} else {
						listOf<ResultRow>()
					}

					if (collectedCandies.isEmpty()) {
						if (it == 0) {
							if (count != 1)
								event.user.openPrivateChannel().await().sendMessage("Você vasculhou a ábobora e encontrou **$count doces** dentro dela! Delícioso, e o melhor de tudo? Todos esses doces vão ficar para mim quando acabar o Halloween! ${Emotes.LORI_YAY}\n\nObrigada por me ajudar a coletar doces no Halloween ;w;")
										.queue()
						}

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

						if (dreams == 0 && 1250 >= howMuchDidTheUserCollect) {
							transaction(Databases.loritta) {
								lorittaProfile.money += 10_000
							}

							try {
								event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect doces**, como recompensa você ganhou **10k sonhos**!\n\nAgora me dê esses doces para eu poder me esbaldar neles. ${Emotes.LORI_TEMMIE}")
										.await()
							} catch (e: Exception) {
							}
						}

						if (howMuchDidTheUserCollect == 400) {
							try {
								event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect doces**, como recompensa você ganhou uma badge exclusiva para o seu `+perfil`!\n\nAgora me dê esses doces para eu poder me esbaldar neles. ${Emotes.LORI_TEMMIE}")
										.await()
							} catch (e: Exception) {
							}
						}

						if (howMuchDidTheUserCollect == 777) {
							transaction(Databases.loritta) {
								val settings = lorittaProfile.settings
								settings.boughtProfiles = settings.boughtProfiles.toMutableList().apply { this.add("Halloween2019ProfileCreator") }.toTypedArray()
							}

							try {
								event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect doces**, como recompensa você ganhou um design exclusivo para o seu `+perfil`! Ative ele em <https://loritta.website/user/@me/dashboard/profiles>\n\nAgora me dê esses doces para eu poder me esbaldar neles. ${Emotes.LORI_TEMMIE}\n\nFeliz Halloween!")
										.await()
							} catch (e: Exception) {
							}
						}

						if (howMuchDidTheUserCollect == Halloween2019.BOOST_LEVEL) {
							try {
								event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect doces**, e agora você precisa me fazer um favor...\n\nEspalhar alegria e diversão para outras pessoas, para transformar o mundo em um lugar melhor.\n\nPara você conseguir realizar isto, eu estou te dando uma recompensa: Uma máquina de criação de doces, com ela, você pode ir em servidores e utilizar `+doces boost #NomeDoCanal` para aumentar em 10x o número de doces que aparecem no canal por cinco minutos.\n\nA máquina é meio instável, então você precisa esperar uma hora entre cada uso... mas espero que, com isso, você consiga ajudar outros membros!\n\nMas lembre-se, a máquina estará apenas disponível entre 29 e 31 de Outubro!\n\nFeliz Halloween, e obrigado pelos doces que você conseguiu para mim! ${Emotes.LORI_TEMMIE}\n\nhttps://cdn.discordapp.com/attachments/583406099047252044/634593178871267348/loriwtc3.png")
										.await()
							} catch (e: Exception) {
							}
						}
					} else {
						break
					}
				}
			}
		}
	}
}