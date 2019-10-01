package net.perfectdreams.loritta.listeners

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.QuirkyConfig
import net.perfectdreams.loritta.QuirkyStuff

class BoostGuildListener(val config: QuirkyConfig) : ListenerAdapter() {
	override fun onGuildMemberUpdateBoostTime(event: GuildMemberUpdateBoostTimeEvent) {
		if (event.guild.id != Constants.PORTUGUESE_SUPPORT_GUILD_ID)
			return

		if (event.oldTimeBoosted == null && event.newTimeBoosted != null) {
			// Ativou Boost
			GlobalScope.launch(loritta.coroutineDispatcher) {
				QuirkyStuff.onBoostActivate(event.member)
			}
			return
		}

		if (event.newTimeBoosted != null && event.oldTimeBoosted != null) {
			// Desativou Boost
			GlobalScope.launch(loritta.coroutineDispatcher) {
				QuirkyStuff.onBoostDeactivate(event.member)
			}
			return
		}
	}
}