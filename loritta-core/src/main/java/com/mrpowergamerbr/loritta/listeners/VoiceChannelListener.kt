package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.eventlog.EventLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.TimeUnit

class VoiceChannelListener(val loritta: Loritta) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val mutexes = Caffeine.newBuilder()
				.expireAfterAccess(60, TimeUnit.SECONDS)
				.build<Long, Mutex>()
				.asMap()
	}

	override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		onVoiceChannelConnect(event.member, event.channelJoined)
	}

	override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		onVoiceChannelLeave(event.member, event.channelLeft)
		onVoiceChannelConnect(event.member, event.channelJoined)
	}

	override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		onVoiceChannelLeave(event.member, event.channelLeft)
	}

	fun onVoiceChannelConnect(member: Member, channelJoined: VoiceChannel) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(channelJoined.idLong) { Mutex() }

			mutex.withLock {
				// Carregar a configuração do servidor
				val config = loritta.getServerConfigForGuild(channelJoined.guild.id)

				EventLog.onVoiceJoin(config, member, channelJoined)

				if (!config.musicConfig.isEnabled)
					return@withLock

				if (config.musicConfig.musicGuildId != channelJoined.id)
					return@withLock

				if (channelJoined.members.isEmpty()) // Whoops, demorou demais!
					return@withLock

				if (channelJoined.members.contains(channelJoined.guild.selfMember)) // Mas... eu já estou neste canal!
					return@withLock

				val mm = loritta.audioManager.getGuildAudioPlayer(channelJoined.guild)

				// Se não está tocando nada e o sistema de músicas aleatórias está ativado, toque uma!
				if (mm.player.playingTrack == null && config.musicConfig.autoPlayWhenEmpty && config.musicConfig.urls.isNotEmpty())
					LorittaUtilsKotlin.startRandomSong(channelJoined.guild, config)
				else if (mm.player.playingTrack != null && !channelJoined.members.contains(channelJoined.guild.selfMember)) {
					mm.player.isPaused = false
					val link = loritta.audioManager.lavalink.getLink(channelJoined.guild)
					link.connect(channelJoined)
				}
			}
		}
	}

	fun onVoiceChannelLeave(member: Member, channelLeft: VoiceChannel) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(channelLeft.idLong) { Mutex() }

			mutex.withLock {
				val config = loritta.getServerConfigForGuild(channelLeft.guild.id)

				EventLog.onVoiceLeave(config, member, channelLeft)

				if (!config.musicConfig.isEnabled)
					return@withLock

				if (config.musicConfig.musicGuildId != channelLeft.id)
					return@withLock

				if (channelLeft.members.any { !it.user.isBot && (it.voiceState?.isDeafened != true && it.voiceState?.isGuildDeafened != true) })
					return@withLock

				// Caso não tenha ninguém no canal de voz, vamos retirar o music manager da nossa lista
				loritta.audioManager.musicManagers.remove(channelLeft.guild.idLong)

				val link = loritta.audioManager.lavalink.getLink(channelLeft.guild)
				link.disconnect() // E desconectar do canal de voz
			}
		}
	}
}