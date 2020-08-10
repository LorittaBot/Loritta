package net.perfectdreams.loritta.plugin.serversupport.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import mu.KotlinLogging
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.plugin.serversupport.ServerSupportPlugin
import java.util.concurrent.TimeUnit

class PleaseDontMentionStaffModule(val plugin: ServerSupportPlugin) : MessageReceivedModule {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val sentMessageAt = Caffeine.newBuilder()
			.expireAfterAccess(15, TimeUnit.MINUTES)
			.build<Long, Long>()

	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
		return (event.channel.idLong == 398987569485971466L) && loritta.config.loritta.environment == EnvironmentType.CANARY
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: LegacyBaseLocale): Boolean {
		sentMessageAt.put(event.author.idLong, System.currentTimeMillis())

		val supportMentioned = event.message.mentionedMembers.filter { it.roles.any { it.idLong == 399301696892829706L } }

		for (support in supportMentioned) {
			val lastSentAt = sentMessageAt.getIfPresent(support.idLong)

			if (lastSentAt == null) {
				// Don't mention staff grrr
				val replies = listOf(
                        LorittaReply(
                                "**Não mencione pessoas da equipe!** As vezes elas podem estar ocupadas... vai se ela está cagando e você aí, incomodando ela...",
                                prefix = "<:lori_rage:556525700425711636>"
                        ),
                        LorittaReply(
                                "Se você precisa de ajuda, mencione o <@&399301696892829706> na mensagem da sua dúvida, obrigada!",
                                mentionUser = false
                        )
				)

				event.channel.sendMessage(
						MessageBuilder()
								.setAllowedMentions(listOf(Message.MentionType.USER, Message.MentionType.CHANNEL))
								.setContent(replies.joinToString("\n", transform = { it.build(JDAUser(event.author)) } ))
								.build()
						).await()
			}
		}

		return false
	}
}