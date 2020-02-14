package com.mrpowergamerbr.loritta.audio

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import lavalink.client.io.Link
import lavalink.client.io.jda.JdaLavalink
import net.dv8tion.jda.api.entities.VoiceChannel
import java.net.URI

class AudioManager(val loritta: Loritta) {
	val lavalink = JdaLavalink(
			loritta.discordConfig.discord.clientId,
			loritta.discordConfig.discord.maxShards
	) { shardId ->
		lorittaShards.shardManager.getShardById(shardId)
	}

	init {
		loritta.discordConfig.lavalink.nodes.forEach {
			lavalink.addNode(it.name, URI("ws://${it.address}"), it.password)
		}
	}

	fun connect(voiceChannel: VoiceChannel): Link {
		val jdaLink = lavalink.getLink(voiceChannel.guild)
		jdaLink.connect(voiceChannel)
		return jdaLink
	}
}