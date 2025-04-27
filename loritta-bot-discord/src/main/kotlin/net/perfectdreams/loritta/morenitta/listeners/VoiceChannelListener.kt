package net.perfectdreams.loritta.morenitta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import net.perfectdreams.loritta.morenitta.utils.eventlog.EventLog
import java.util.concurrent.TimeUnit

class VoiceChannelListener(val loritta: LorittaBot) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build<Long, Mutex>()
			.asMap()
	}

	override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
		if (DebugLog.cancelAllEvents)
			return

		val channelLeft = event.channelLeft
		val channelJoined = event.channelJoined
		logger.info { "Received Voice Update of member ${event.member.idLong} on guild ${event.guild.idLong} - They left channel ${channelLeft?.idLong} and joined channel ${channelJoined?.idLong}" }
		if (event.guild.selfMember == event.member && channelLeft != null && channelJoined == null) {
			// Clean up voice connection if Loritta disconnected and didn't join a new channel
			GlobalScope.launch {
				val guildId = channelLeft.guild.idLong
				logger.info { "Cleaning up Loritta's voice connection @ $guildId" }

				loritta.voiceConnectionsManager.voiceConnectionsMutexes.getOrPut(guildId) { Mutex() }.withLock {
					val vcConnection = loritta.voiceConnectionsManager.voiceConnections[guildId]
					if (vcConnection != null) {
						// Shutdown voice connection if it exists
						loritta.voiceConnectionsManager.shutdownVoiceConnection(
							guildId,
							vcConnection
						)
					}
				}
			}
		}

		if (channelLeft != null)
			onVoiceChannelLeave(event.member, channelLeft)
		if (channelJoined != null)
			onVoiceChannelConnect(event.member, channelJoined)
	}

	fun onVoiceChannelConnect(member: Member, channelJoined: AudioChannelUnion) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(channelJoined.idLong) { Mutex() }

			logger.info { "Attempting to process voice channel join of member ${member.idLong} on guild ${channelJoined.guild.idLong} to channel ${channelJoined.idLong}! Is mutex locked? ${mutex.isLocked}" }

			mutex.withLock {
				logger.info { "Processing voice channel join of member ${member.idLong} on guild ${channelJoined.guild.idLong} to channel ${channelJoined.idLong}!" }

				// Carregar a configuração do servidor
				val serverConfig = loritta.getOrCreateServerConfig(channelJoined.guild.idLong)
				EventLog.onVoiceJoin(loritta, serverConfig, member, channelJoined)
			}
		}
	}

	fun onVoiceChannelLeave(member: Member, channelLeft: AudioChannelUnion) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(channelLeft.idLong) { Mutex() }

			logger.info { "Attempting to process voice channel leave of member ${member.idLong} on guild ${channelLeft.guild.idLong} from channel ${channelLeft.idLong}! Is mutex locked? ${mutex.isLocked}" }

			mutex.withLock {
				logger.info { "Processing voice channel leave of member ${member.idLong} on guild ${channelLeft.guild.idLong} from channel ${channelLeft.idLong}!" }

				val serverConfig = loritta.getOrCreateServerConfig(channelLeft.guild.idLong)
				EventLog.onVoiceLeave(loritta, serverConfig, member, channelLeft)
			}
		}
	}
}