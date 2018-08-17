package com.mrpowergamerbr.loritta.audio

import com.mrpowergamerbr.loritta.utils.loritta
import lavalink.client.player.LavalinkPlayer
import net.dv8tion.jda.core.entities.Guild

/**
 * Holder for both the player and a track scheduler for one guild.
 */
class GuildMusicManager(val guild: Guild) {
	/**
	 * Audio player for the guild.
	 */
	val player: LavalinkPlayer
			get() = loritta.audioManager.lavalink.getLink(guild).player

	/**
	 * Track scheduler for the player.
	 */
	val scheduler = TrackScheduler(guild, player)

	init {
		player.addListener(scheduler)
	}
}