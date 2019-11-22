package net.perfectdreams.loritta.listeners

import com.mrpowergamerbr.loritta.utils.extensions.await
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.QuirkyConfig

class AddReactionFurryAminoPtListener(val config: QuirkyConfig) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (!event.reactionEmote.isEmote
				|| event.reactionEmote.idLong != 593161404937404416L)
			return

		val securityRole = event.guild.getRoleById(320608529398497280L)
		val adminRole = event.guild.getRoleById(300279961686638603L)

		if (event.member.roles.contains(securityRole) || event.member.roles.contains(adminRole)) {
			GlobalScope.launch {
				val message = event.channel.retrieveMessageById(event.messageIdLong).await()
				message.delete().queue()

				val roles = event.member.roles.toMutableList()

				// Remover cargos e tals
				roles.remove(event.guild.getRoleById(302117788477292575L))
				roles.add(event.guild.getRoleById(232877728397918210L))

				event.guild.modifyMemberRoles(message.member!!, roles).queue()
			}
		}
	}
}