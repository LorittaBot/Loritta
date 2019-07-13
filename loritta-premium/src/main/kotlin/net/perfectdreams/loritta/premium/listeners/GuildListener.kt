package net.perfectdreams.loritta.premium.listeners

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.premium.LorittaPremium
import net.perfectdreams.loritta.premium.tables.DonationKeys
import net.perfectdreams.loritta.premium.utils.Constants
import net.perfectdreams.loritta.premium.utils.extensions.await
import java.awt.Color

class GuildListener(val m: LorittaPremium) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onGuildJoin(event: GuildJoinEvent) {
		if (m.config.doNotKickOut.contains(event.guild.idLong))
			return

		logger.info { "Added on guild ${event.guild}! Checking if the user has permission to have my fluffy and cutesy presence there..." }

		GlobalScope.launch(m.coroutineDispatcher) {
			val activeDonationKey = m.getActiveDonationKeyForGuild(event.guild)

			logger.info { "${event.guild} uses donation key $activeDonationKey"}

			if (activeDonationKey == null || Constants.PREMIUM_BOT_VALUE >= activeDonationKey[DonationKeys.value]) {
				// Enviar para todos da equipe que precisa doar para ter!
				logger.info { "Leaving guild ${event.guild} that uses key $activeDonationKey... sadly they don't meet our fancy requirements!" }

				val membersToSendDMs = event.guild.members.filter {
					!it.user.isBot && (it.hasPermission(Permission.ADMINISTRATOR) || it.hasPermission(Permission.MANAGE_SERVER))
				}

				var successfullySentDMs = 0

				for (member in membersToSendDMs) {
					try {
						member.user.openPrivateChannel().await().sendMessage(
								EmbedBuilder()
										.setTitle("Então, isso é constrangedor... \uD83D\uDCB8")
										.setDescription("""Então... parece que alguém da equipe do `${event.guild.name}` (que você também é da equipe!) tentou me adicionar mas... o servidor não tem uma key premium ativa!
											|
											|Enquanto eu faço nada de útil (apenas sirvo de enfeite!), a versão normal minha (a <@297153970613387264>!) possui váaaarias vantagens incríveis que você pode ganhar ao doar!
											|
											|Não sabe as vantagens? Então veja o [meu website](https://loritta.website/donate) para ver quanto você precisa doar para me ter (e várias outras incríveis vantagens)! Espero que você doe... <a:lori_temmie:515330130495799307>
											|
											|Então por enquanto é isso! Obrigada e eu espero que algum dia a gente se encontre de novo... <:lori_tristeliz:556524143281963008>
										""".trimMargin())
										.setImage("https://cdn.discordapp.com/attachments/592352881072668693/592368648715239424/Loritta_-_Heathecliff.png")
										.setColor(Color(0, 193, 223))
										.build()
						).await()
						logger.info { "Sent direct message to $member"}
						successfullySentDMs++
					} catch (e: Exception) {}
				}

				val addedOnServerButBadKeyChannel = m.jda.getTextChannelById(m.config.addedOnServerButBadKeyChannel)

				addedOnServerButBadKeyChannel?.sendMessage("Tentaram me adicionar em ${event.guild.name} (${event.guild.idLong}) mas infelizmente eles não possuem os requisitos fofos e lindos que a alteza real (eu <:lori_very_owo:562303822978875403>) requer! Eu avisei para ${successfullySentDMs}/${membersToSendDMs.size} membros que tinham permissão de administrador/gerenciar servidor do servidor que é necessário *doar* antes de tenta me adicionar. <:lori_cheese:592779169059045379>")?.await()

				// E sair do servidor ;w;
				event.guild.leave().await()
			}
		}
	}
}