package com.mrpowergamerbr.loritta.analytics

import com.mrpowergamerbr.loritta.amino.AminoRepostTask
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.threads.NewRssFeedTask
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.youtube.CreateYouTubeWebhooksTask
import mu.KotlinLogging
import java.util.concurrent.ThreadPoolExecutor

/**
 * Sends internal analytics to the console
 */
class InternalAnalyticSender : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun run() {
		val mb = 1024 * 1024
		val runtime = Runtime.getRuntime()

		logger.info("Loritta's status...")
		logger.info("Shards: ${lorittaShards.getShards().size}")
		logger.info("Total Servers: ${lorittaShards.getGuildCount()}")
		logger.info("Used Memory:"
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
		logger.info("Free Memory:"
				+ runtime.freeMemory() / mb)
		logger.info("Total Memory:" + runtime.totalMemory() / mb)
		logger.info("Max Memory:" + runtime.maxMemory() / mb)
		logger.info("commandManager.commandMap.size: ${loritta.commandManager.commandMap.size}")
		logger.info("commandManager.defaultCmdOptions.size: ${loritta.commandManager.defaultCmdOptions.size}")
		logger.info("dummyServerConfig.guildUserData.size: ${loritta.dummyServerConfig.guildUserData.size}")
		logger.info("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
		logger.info("locales.size: ${loritta.locales.size}")
		logger.info("ignoreIds.size: ${loritta.ignoreIds.size}")
		logger.info("userCooldown.size: ${loritta.userCooldown.size}")
		logger.info("musicManagers.size: ${loritta.audioManager.musicManagers.size}")
		logger.info("Total Track Queue: ${loritta.audioManager.musicManagers.values.sumBy { it.scheduler.queue.size }}")
		logger.info("songThrottle.size: ${loritta.audioManager.songThrottle.size}")
		logger.info("youTubeKeys.size: ${loritta.youtubeKeys.size}")
		logger.info("fanArts.size: ${loritta.fanArts.size}")
		logger.info("storedLastEntries.size: ${AminoRepostTask.storedLastIds.size}")
		logger.info("gameInfoCache.size: ${NewLivestreamThread.gameInfoCache.size}")
		logger.info("isLivestreaming.size: ${NewLivestreamThread.isLivestreaming.size}")
		logger.info("displayNameCache.size: ${NewLivestreamThread.displayNameCache.size}")
		logger.info("storedLastEntries.size: ${NewRssFeedTask.storedLastEntries}")
		logger.info("YouTube's Last Notified: ${CreateYouTubeWebhooksTask.lastNotified.size}")
		logger.info("executor: ${(loritta.executor as ThreadPoolExecutor).activeCount}")
		logger.info("Total Thread Count: ${Thread.getAllStackTraces().keys.size}")
	}
}