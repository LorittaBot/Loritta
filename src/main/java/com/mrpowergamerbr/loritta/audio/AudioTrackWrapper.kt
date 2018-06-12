package com.mrpowergamerbr.loritta.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.core.entities.User

data class AudioTrackWrapper(
		val track: AudioTrack,
		val isAutoPlay: Boolean = false,
		val user: User,
		val metadata: MutableMap<String, String> = mutableMapOf())