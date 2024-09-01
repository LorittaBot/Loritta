package net.perfectdreams.loritta.morenitta.utils.debug

import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.listeners.EventLogListener
import net.perfectdreams.loritta.morenitta.listeners.PreStartGatewayEventReplayListener
import net.perfectdreams.loritta.morenitta.modules.InviteLinkModule
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import java.util.concurrent.ThreadPoolExecutor

object DebugLog {
	private val logger = KotlinLogging.logger {}
	var cancelAllEvents = false
		get() {
			if (field)
				logger.warn { "All received events are cancelled and ignored!" }
			return field
		}

	fun showExtendedInfo(loritta: LorittaBot) {
		val mb = 1024 * 1024
		val runtime = Runtime.getRuntime()

		logger.info("===[ EXTENDED INFO ]===")
		logger.info("Used Memory:"
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb)
		logger.info("Free Memory:"
				+ runtime.freeMemory() / mb)
		logger.info("Total Memory:" + runtime.totalMemory() / mb)
		logger.info("Max Memory:" + runtime.maxMemory() / mb)
		logger.info("coroutineExecutor: ${(loritta.coroutineExecutor as ThreadPoolExecutor).activeCount}")
		logger.info("Shards Pre Login states that aren't finished:")
		for ((shardId, state) in loritta.preLoginStates) {
			if (state.value != PreStartGatewayEventReplayListener.ProcessorState.FINISHED) {
				logger.info("Shard $shardId: ${state.value}")
			}
		}
		logger.info("Active voice connections: ${loritta.voiceConnectionsManager.voiceConnections.size}")
		logger.info("TemmieDiscordAuth mutexes instances: ${TemmieDiscordAuth.accessTokenMutexes.size}")
		logger.info("Pudding Semaphore Permits: ${loritta.pudding.semaphore.availablePermits}")
		logger.info("> Command Stuff")
		logger.info("commandManager.commandMap.size: ${loritta.legacyCommandManager.commandMap.size}")
		logger.info("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
		logger.info("locales.size: ${loritta.legacyLocales.size}")
		logger.info("ignoreIds.size: ${loritta.ignoreIds.size}")
		logger.info("> Tasks Stuff")
		logger.info("loritta.twitch.cachedGames: ${loritta.twitch.cachedGames.size}")
		logger.info("loritta.twitch.cachedStreamerInfo: ${loritta.twitch.cachedStreamerInfo.size}")
		logger.info("gameInfoCache.size: ${loritta.twitch.cachedGames.size}")
		logger.info("> Invite Stuff")
		logger.info("cachedInviteLinks.size: ${InviteLinkModule.cachedInviteLinks.size}")
		logger.info("> Misc Stuff")
		logger.info("fanArts.size: ${loritta.fanArts.size}")
		logger.info("eventLogListener.downloadedAvatarJobs: ${EventLogListener.downloadedAvatarJobs}")
		logger.info("Cached Retrieved Users: ${loritta.lorittaShards.cachedRetrievedUsers.size()}")
		logger.info("> Executors")

		val pendingMessagesSize = loritta.pendingMessages.size
		val availableProcessors = LorittaBot.MESSAGE_EXECUTOR_THREADS
		val isMessagesOverloaded = pendingMessagesSize > availableProcessors
		logger.info("Pending Messages ($pendingMessagesSize): Active: ${loritta.pendingMessages.filter { it.isActive }.count()}; Cancelled: ${loritta.pendingMessages.filter { it.isCancelled }.count()}; Complete: ${loritta.pendingMessages.filter { it.isCompleted }.count()};")
		logger.info("> Bluesky Stuff")
		logger.info("Pending posts: ${loritta.blueSkyRelay.postStream.getCount()}")
		logger.info("Last event received at: ${loritta.blueSkyRelay.firehoseClient.lastEventReceivedAt}")
		if (isMessagesOverloaded)
			logger.warn { "Loritta is overloaded! There are $pendingMessagesSize messages pending to be executed, ${pendingMessagesSize - availableProcessors} more than it should be!" }
	}
}