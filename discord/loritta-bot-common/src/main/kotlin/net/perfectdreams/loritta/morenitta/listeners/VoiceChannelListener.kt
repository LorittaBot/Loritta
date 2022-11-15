package net.perfectdreams.loritta.morenitta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import net.perfectdreams.loritta.morenitta.utils.eventlog.EventLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
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

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		val channelLeft = event.channelLeft
		if (channelLeft != null)
			onVoiceChannelLeave(event.member, channelLeft)

		val channelJoined = event.channelJoined
		if (channelJoined != null)
			onVoiceChannelConnect(event.member, channelJoined)
	}

	fun onVoiceChannelConnect(member: Member, channelJoined: AudioChannelUnion) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(channelJoined.idLong) { Mutex() }

			mutex.withLock {
				// Carregar a configuração do servidor
				val serverConfig = loritta.getOrCreateServerConfig(channelJoined.guild.idLong)
				EventLog.onVoiceJoin(loritta, serverConfig, member, channelJoined)
			}
		}
	}

	fun onVoiceChannelLeave(member: Member, channelLeft: AudioChannelUnion) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val mutex = mutexes.getOrPut(channelLeft.idLong) { Mutex() }

			mutex.withLock {
				val serverConfig = loritta.getOrCreateServerConfig(channelLeft.guild.idLong)
				EventLog.onVoiceLeave(loritta, serverConfig, member, channelLeft)
			}
		}
	}
}