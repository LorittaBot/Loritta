package net.perfectdreams.loritta.platform.discord.legacy.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import net.perfectdreams.loritta.api.OnlineStatus
import net.perfectdreams.loritta.api.entities.Member
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser

class DiscordMember(@JsonIgnore val memberHandle: net.dv8tion.jda.api.entities.Member) : Member, JDAUser(memberHandle.user) {
	override val onlineStatus: OnlineStatus // Gambiarra:tm:
		get() = OnlineStatus.valueOf(memberHandle.onlineStatus.name)
}