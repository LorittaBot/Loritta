package com.mrpowergamerbr.loritta.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.User

data class TrackRequest(
		val requestedBy: User,
		val track: AudioTrack
)