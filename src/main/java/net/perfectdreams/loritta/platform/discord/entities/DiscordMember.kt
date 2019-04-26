package net.perfectdreams.loritta.platform.discord.entities

import net.perfectdreams.loritta.api.OnlineStatus
import net.perfectdreams.loritta.api.entities.Member

class DiscordMember(val memberHandle: net.dv8tion.jda.api.entities.Member) : DiscordUser(memberHandle.user), Member {
	override val onlineStatus: OnlineStatus // Gambiarra:tm:
		get() = OnlineStatus.valueOf(memberHandle.onlineStatus.name)
}