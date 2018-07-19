package com.mrpowergamerbr.loritta.analytics

import com.google.common.flogger.FluentLogger
import com.mrpowergamerbr.loritta.amino.AminoRepostTask
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.threads.NewRssFeedThread
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.youtube.CreateYouTubeWebhooksTask
import java.util.concurrent.ThreadPoolExecutor

/**
 * Sends internal analytics to the console
 */
class InternalAnalyticSender : Runnable {
	companion object {
		private val logger = FluentLogger.forEnclosingClass()
	}

	override fun run() {
		val mb = 1024 * 1024
		val runtime = Runtime.getRuntime()

		logger.atFine().log("Loritta's status...")
		logger.atFine().log("Shards: ${lorittaShards.shards.size}")
		logger.atFine().log("Total Servers: ${lorittaShards.getGuildCount()}")
		logger.atFine().log("Used Memory:"
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
		logger.atFine().log("Free Memory:"
				+ runtime.freeMemory() / mb)
		logger.atFine().log("Total Memory:" + runtime.totalMemory() / mb)
		logger.atFine().log("Max Memory:" + runtime.maxMemory() / mb)
		logger.atFine().log("commandManager.commandMap.size: ${loritta.commandManager.commandMap.size}")
		logger.atFine().log("commandManager.defaultCmdOptions.size: ${loritta.commandManager.defaultCmdOptions.size}")
		logger.atFine().log("dummyServerConfig.guildUserData.size: ${loritta.dummyServerConfig.guildUserData.size}")
		logger.atFine().log("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
		logger.atFine().log("locales.size: ${loritta.locales.size}")
		logger.atFine().log("ignoreIds.size: ${loritta.ignoreIds.size}")
		logger.atFine().log("userCooldown.size: ${loritta.userCooldown.size}")
		logger.atFine().log("southAmericaMemesPageCache.size: ${loritta.southAmericaMemesPageCache.size}")
		logger.atFine().log("musicManagers.size: ${loritta.audioManager.musicManagers.size}")
		logger.atFine().log("Total Track Queue: ${loritta.audioManager.musicManagers.values.sumBy { it.scheduler.queue.size }}")
		logger.atFine().log("songThrottle.size: ${loritta.audioManager.songThrottle.size}")
		logger.atFine().log("youTubeKeys.size: ${loritta.youtubeKeys.size}")
		logger.atFine().log("fanArts.size: ${loritta.fanArts.size}")
		logger.atFine().log("storedLastIds.size: ${AminoRepostTask.storedLastIds.size}")
		logger.atFine().log("gameInfoCache.size: ${NewLivestreamThread.gameInfoCache.size}")
		logger.atFine().log("isLivestreaming.size: ${NewLivestreamThread.isLivestreaming.size}")
		logger.atFine().log("displayNameCache.size: ${NewLivestreamThread.displayNameCache.size}")
		logger.atFine().log("lastItemTime.size: ${NewRssFeedThread.lastItemTime.size}")
		logger.atFine().log("YouTube's Last Notified: ${CreateYouTubeWebhooksTask.lastNotified.size}")
		logger.atFine().log("executor: ${(loritta.executor as ThreadPoolExecutor).activeCount}")
		logger.atFine().log("Total Thread Count: ${Thread.getAllStackTraces().keys.size}")
	}
}