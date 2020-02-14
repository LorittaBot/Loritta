package net.perfectdreams.loritta.plugin.funky.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.perfectdreams.loritta.api.entities.User

data class TrackRequest(
		val requestedBy: User,
		val track: AudioTrack
)