package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
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

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		onVoiceChannelConnect(event.member, event.channelJoined)
	}

	override fun onGuildVoiceMove(event: GuildVoiceMoveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		onVoiceChannelLeave(event.member, event.channelLeft)
		onVoiceChannelConnect(event.member, event.channelJoined)
	}

	override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		onVoiceChannelLeave(event.member, event.channelLeft)
	}

	fun onVoiceChannelConnect(member: Member, channelJoined: VoiceChannel) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(channelJoined.idLong) { Mutex() }

			mutex.withLock {
				// Carregar a configuração do servidor
				val serverConfig = loritta.getOrCreateServerConfig(channelJoined.guild.idLong)
				EventLog.onVoiceJoin(serverConfig, member, channelJoined)
			}
		}
	}

	fun onVoiceChannelLeave(member: Member, channelLeft: VoiceChannel) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(channelLeft.idLong) { Mutex() }

			mutex.withLock {
				val serverConfig = loritta.getOrCreateServerConfig(channelLeft.guild.idLong)
				EventLog.onVoiceLeave(serverConfig, member, channelLeft)
			}
		}
	}
}