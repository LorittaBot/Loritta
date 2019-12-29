package net.perfectdreams.loritta.plugin.christmas2019.listeners

import com.github.salomonbrys.kotson.jsonObject
import com.google.common.cache.CacheBuilder
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.userAgent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.dao.Giveaway
import net.perfectdreams.loritta.plugin.christmas2019.Christmas2019
import net.perfectdreams.loritta.plugin.christmas2019.Christmas2019Config
import net.perfectdreams.loritta.plugin.christmas2019.modules.DropChristmasStuffModule
import net.perfectdreams.loritta.tables.Christmas2019Players
import net.perfectdreams.loritta.tables.CollectedChristmas2019Points
import net.perfectdreams.loritta.tables.Giveaways
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

class GetChristmasStuffListener(val config: Christmas2019Config) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val mutexes = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build<Long, Mutex>()
				.asMap()
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (event.user.isBot)
			return

		if (event.reactionEmote.isEmoji) {
			if (event.reactionEmote.name !in Christmas2019.emojis)
				return
		} else
			if ("${event.reactionEmote.name}:${event.reactionEmote.idLong}" !in Christmas2019.emojis)
				return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val lorittaProfile = loritta.getLorittaProfile(event.user.idLong) ?: return@launch

			val isParticipating = transaction(Databases.loritta) {
				Christmas2019Players.select {
					Christmas2019Players.user eq lorittaProfile.id
				}.count() != 0
			}

			val getTheCandy = isParticipating && Christmas2019.isEventActive()

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

			val message = event.channel.retrieveMessageById(event.messageIdLong).await()

			val reactedAt = DropChristmasStuffModule.dropInMessageAt[event.messageIdLong]
			if (reactedAt != null) {
				val diff = System.currentTimeMillis() - reactedAt

				logger.info { "User (${event.member.idLong}) took ${diff}ms to react in ${event.messageIdLong} @ ${event.guild}" }
				if (1_500 >= diff) {
					logger.info { "Looks like ${event.member.idLong} is using userbots to react on the messages! Took ${diff}ms to react in ${event.messageIdLong} @ ${event.guild}" }
					loritta.http.post<HttpResponse>("https://loritta-cluster3.loritta.website/api/v1/parallax/channels/660794036952760340/messages") {
						userAgent(loritta.lorittaCluster.getUserAgent())
						header("Authorization", loritta.lorittaInternalApiKey.name)

						body = gson.toJson(
								jsonObject(
										"content" to "Eu acho que <@${event.member.idLong}> (${event.member.idLong}) está usando userbots para reagir nas mensagens! Demorou ${diff}ms para reagir em ${event.messageIdLong} @ ${event.guild}"
								)
						)
					}
				}
			}

			if (System.currentTimeMillis() - 900_000 >= message.timeCreated.toInstant().toEpochMilli()) {
				try {
					event.user.openPrivateChannel().await().sendMessage("O presente que você coletou está velho demais! Já posso escutar você falando \"nosa mas presentes não tem data de validade!\"... e você está certo! Mas a gente precisa de algo para deixar o evento mais desafiador, né? ${Emotes.LORI_HEART}")
							.await()
				} catch (e: Exception) {}
				return@launch
			}

			val mutex = mutexes.getOrPut(event.user.idLong, { Mutex() })

			mutex.withLock {
				val count = 1

				for (it in 0 until count) {
					val collectedCandies = if (it == 0) {
						transaction(Databases.loritta) {
							CollectedChristmas2019Points.select {
								(CollectedChristmas2019Points.user eq lorittaProfile.id.value) and
										(CollectedChristmas2019Points.guildId eq event.guild.idLong) and
										(CollectedChristmas2019Points.channelId eq event.channel.idLong) and
										(CollectedChristmas2019Points.messageId eq event.messageIdLong)
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
							CollectedChristmas2019Points.insert {
								it[user] = lorittaProfile.id
								it[guildId] = event.guild.idLong
								it[channelId] = event.channel.idLong
								it[messageId] = event.messageIdLong
							}
						}

						val howMuchDidTheUserCollect = transaction(Databases.loritta) {
							CollectedChristmas2019Points.select {
								(CollectedChristmas2019Points.user eq lorittaProfile.id.value)
							}.count()
						}

						val dreams = howMuchDidTheUserCollect % 125

						if (dreams == 0 && 2500 > howMuchDidTheUserCollect) {
							transaction(Databases.loritta) {
								lorittaProfile.money += 10_000
							}

							try {
								event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect presentes**, como recompensa você ganhou **10k sonhos**!\n\nAgora me dê esses presentes para eu colocar de baixo da árvore de Natal. ${Emotes.LORI_TEMMIE}")
										.await()
							} catch (e: Exception) {
							}
						}

						if (howMuchDidTheUserCollect == 400) {
							try {
								event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect presentes**, como recompensa você ganhou uma badge exclusiva para o seu `+perfil`!\n\nAgora me dê esses presentes para eu colocar baixo da árvore de Natal. ${Emotes.LORI_TEMMIE}")
										.await()
							} catch (e: Exception) {
							}
						}

						if (howMuchDidTheUserCollect == 777) {
							transaction(Databases.loritta) {
								val settings = lorittaProfile.settings
								settings.boughtProfiles = settings.boughtProfiles.toMutableList().apply { this.add("Christmas2019ProfileCreator") }.toTypedArray()
							}

							try {
								event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect presentes**, como recompensa você ganhou um design exclusivo para o seu `+perfil`! Ative ele em <https://loritta.website/user/@me/dashboard/profiles>\n\nAgora me dê esses presentes para eu poder colocar de baixo da árvore de Natal! ${Emotes.LORI_TEMMIE}\n\nFeliz Natal!")
										.await()
							} catch (e: Exception) {
							}
						}

						if (howMuchDidTheUserCollect == 2500) {
							transaction(Databases.loritta) {
								val settings = lorittaProfile.settings
								settings.boughtProfiles = settings.boughtProfiles.toMutableList().apply { this.add("LorittaChristmas2019ProfileCreator") }.toTypedArray()
							}

							try {
								event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect presentes**, como recompensa você ganhou um design exclusivo para o seu `+perfil`! Ative ele em <https://loritta.website/user/@me/dashboard/profiles>\n\nAgora me dê esses presentes para eu poder colocar de baixo da árvore de Natal! ${Emotes.LORI_TEMMIE}\n\nFeliz Natal!")
										.await()
							} catch (e: Exception) {
							}
						}

						if (howMuchDidTheUserCollect == 3000) {
							transaction(Databases.loritta) {
								DonationKey.new {
									this.userId = event.member.idLong
									this.expiresAt = System.currentTimeMillis() + Constants.ONE_MONTH_IN_MILLISECONDS
									this.value = 199.99
								}
							}

							try {
								event.user.openPrivateChannel().await().sendMessage("Você coletou **$howMuchDidTheUserCollect presentes**, como recompensa você ganhou uma key de doador que vale R$ 199,99 para o seu servidor! Ative multiplicadores de sonhos, badges e muito mais no seu servidor! Ative em <https://loritta.website/dashboard> e veja todas as vantagens em <https://loritta.website/donate>\n\nAgora me dê esses presentes para eu poder colocar de baixo da árvore de Natal! ${Emotes.LORI_TEMMIE}\n\nFeliz Natal!")
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