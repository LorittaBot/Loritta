package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.eventlog.EventLog
import mu.KotlinLogging
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

class VoiceChannelListener(val loritta: Loritta) : ListenerAdapter() {
	private val logger = KotlinLogging.logger {}

	override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			// Carregar a configuração do servidor
			val config = loritta.getServerConfigForGuild(event.guild.id)

			EventLog.onVoiceJoin(config, event.member, event.channelJoined)

			if (!config.musicConfig.isEnabled)
				return@execute

			if ((config.musicConfig.musicGuildId ?: "").isEmpty())
				return@execute

			val voiceChannel = event.guild.getVoiceChannelById(config.musicConfig.musicGuildId) ?: return@execute

			if (voiceChannel.members.isEmpty()) // Whoops, demorou demais!
				return@execute

			if (voiceChannel.members.contains(event.guild.selfMember)) // Mas... fui eu mesmo que entrei!
				return@execute

			val mm = loritta.audioManager.getGuildAudioPlayer(event.guild)

			// Se não está tocando nada e o sistema de músicas aleatórias está ativado, toque uma!
			if (mm.player.playingTrack == null && config.musicConfig.autoPlayWhenEmpty && config.musicConfig.urls.isNotEmpty())
				LorittaUtilsKotlin.startRandomSong(event.guild, config)
			else if (mm.player.playingTrack != null && !voiceChannel.members.contains(event.guild.selfMember)) {
				mm.player.isPaused = false
				val link = loritta.audioManager.lavalink.getLink(event.guild)
				link.connect(voiceChannel)
			}
		}
	}

	override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val config = loritta.getServerConfigForGuild(event.guild.id)

			EventLog.onVoiceLeave(config, event.member, event.channelLeft)

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