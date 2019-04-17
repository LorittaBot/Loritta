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
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
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

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(event.channelJoined.idLong) { Mutex() }

			mutex.withLock {
				// Carregar a configuração do servidor
				val config = loritta.getServerConfigForGuild(event.guild.id)

				EventLog.onVoiceJoin(config, event.member, event.channelJoined)

				if (!config.musicConfig.isEnabled)
					return@withLock

				if ((config.musicConfig.musicGuildId ?: "").isEmpty())
					return@withLock

				val voiceChannel = event.guild.getVoiceChannelById(config.musicConfig.musicGuildId) ?: return@withLock

				if (voiceChannel.members.isEmpty()) // Whoops, demorou demais!
					return@withLock

				if (voiceChannel.members.contains(event.guild.selfMember)) // Mas... fui eu mesmo que entrei!
					return@withLock

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
	}

	override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(event.channelLeft.idLong) { Mutex() }

			mutex.withLock {
				val config = loritta.getServerConfigForGuild(event.guild.id)

				EventLog.onVoiceLeave(config, event.member, event.channelLeft)

				if (!config.musicConfig.isEnabled)
					return@withLock

				if ((config.musicConfig.musicGuildId ?: "").isEmpty())
					return@withLock

				val voiceChannel = event.guild.getVoiceChannelById(config.musicConfig.musicGuildId) ?: return@withLock

				if (voiceChannel.members.any { !it.user.isBot && (!it.voiceState.isDeafened && !it.voiceState.isGuildDeafened) })
					return@withLock

				// Caso não tenha ninguém no canal de voz, vamos retirar o music manager da nossa lista
				loritta.audioManager.musicManagers.remove(event.guild.idLong)

				val link = loritta.audioManager.lavalink.getLink(event.guild)
				link.disconnect() // E desconectar do canal de voz
			}
		}
	}
}