package net.perfectdreams.loritta.platform.discord.entities

import net.perfectdreams.loritta.api.entities.MessageChannel

class DiscordMessageChannel(handle: net.dv8tion.jda.api.entities.MessageChannel) : DiscordChannel(handle), MessageChannel {

}