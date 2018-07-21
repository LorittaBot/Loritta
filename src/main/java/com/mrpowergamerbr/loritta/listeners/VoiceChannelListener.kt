package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.logger
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

class VoiceChannelListener(val loritta: Loritta) : ListenerAdapter() {
	val logger by logger()

	override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val config = loritta.getServerConfigForGuild(event.guild.id)

			if (!config.musicConfig.isEnabled)
				return@execute

			if ((config.musicConfig.musicGuildId ?: "").isEmpty())
				return@execute

			val voiceChannel = event.guild.getVoiceChannelById(config.musicConfig.musicGuildId) ?: return@execute

			if (voiceChannel.members.isEmpty())
				return@execute

			if (voiceChannel.members.contains(event.guild.selfMember))
				return@execute

			val mm = loritta.audioManager.getGuildAudioPlayer(event.guild)

			val link = loritta.audioManager.lavalink.getLink(event.guild)
			link.connect(voiceChannel)
			mm.player.isPaused = false

			if (mm.player.playingTrack == null)
				LorittaUtilsKotlin.startRandomSong(event.guild, config)
		}
	}

	override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val config = loritta.getServerConfigForGuild(event.guild.id)

			if (!config.musicConfig.isEnabled)
				return@execute

			if ((config.musicConfig.musicGuildId ?: "").isEmpty())
				return@execute

			val voiceChannel = event.guild.getVoiceChannelById(config.musicConfig.musicGuildId) ?: return@execute

			if (voiceChannel.members.any { !it.user.isBot && (!it.voiceState.isDeafened && !it.voiceState.isGuildDeafened) })
				return@execute

			val mm = loritta.audioManager.getGuildAudioPlayer(event.guild)

			if (mm.player.playingTrack != null) {
				mm.player.isPaused = true // Pausar música caso todos os usuários saiam
			}

			val link = loritta.audioManager.lavalink.getLink(event.guild)
			link.disconnect() // E desconectar do canal de voz
		}
	}
}