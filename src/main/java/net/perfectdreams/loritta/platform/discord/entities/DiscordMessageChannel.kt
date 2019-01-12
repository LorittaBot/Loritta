package net.perfectdreams.loritta.platform.discord.entities

import net.perfectdreams.loritta.api.entities.MessageChannel

class DiscordMessageChannel(handle: net.dv8tion.jda.core.entities.MessageChannel) : DiscordChannel(handle), MessageChannel {

}