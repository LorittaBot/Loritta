package net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import net.perfectdreams.loritta.common.entities.Member
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser

class DiscordMember(@JsonIgnore val memberHandle: net.perfectdreams.loritta.deviousfun.entities.Member) : Member, JDAUser(memberHandle.user)