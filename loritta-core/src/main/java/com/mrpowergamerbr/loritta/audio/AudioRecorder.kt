package com.mrpowergamerbr.loritta.audio

import com.mrpowergamerbr.loritta.Loritta
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import java.io.File

class AudioRecorder(val loritta: Loritta) {
	fun startRecording(guild: Guild, channel: VoiceChannel) {
		// File("/home/audio_test/${guild.idLong}-${System.currentTimeMillis()}.ogg")
		val audioRecorderHandler = AudioRecorderHandler(
				RecordingOptions(
						true,
						true
				)
		)
		guild.audioManager.sendingHandler = audioRecorderHandler
		guild.audioManager.receivingHandler = audioRecorderHandler
		guild.audioManager.openAudioConnection(channel)
	}

	fun stopRecording(guild: Guild): File {
		val audioRecorderHandler = guild.audioManager.sendingHandler as AudioRecorderHandler
		audioRecorderHandler.stop()

		guild.audioManager.sendingHandler = null
		guild.audioManager.receivingHandler = null
		guild.audioManager.closeAudioConnection()

		throw RuntimeException("test")
		// return audioRecorderHandler.output
	}

	fun isRecording(guild: Guild) = guild.audioManager.sendingHandler is AudioRecorderHandler

	class RecordingOptions(
			val recordSingleTrack: Boolean,
			val recordMultiTrack: Boolean
	)
}