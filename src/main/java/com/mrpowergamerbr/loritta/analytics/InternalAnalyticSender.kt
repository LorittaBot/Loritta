package com.mrpowergamerbr.loritta.analytics

import com.mrpowergamerbr.loritta.threads.AminoRepostThread
import com.mrpowergamerbr.loritta.threads.NewLivestreamThread
import com.mrpowergamerbr.loritta.threads.NewRssFeedThread
import com.mrpowergamerbr.loritta.utils.logger
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.youtube.CreateYouTubeWebhooksTask
import java.util.concurrent.ThreadPoolExecutor

/**
 * Sends internal analytics to the console
 */
class InternalAnalyticSender : Runnable {
	companion object {
		val logger by logger()
	}

	override fun run() {
		val mb = 1024 * 1024
		val runtime = Runtime.getRuntime()

		LorittaAnalytics.logger.info("Loritta's status...")
		LorittaAnalytics.logger.info("Shards: ${lorittaShards.shards.size}")
		LorittaAnalytics.logger.info("Total Servers: ${lorittaShards.getGuildCount()}")
		LorittaAnalytics.logger.info("Used Memory:"
				+ (runtime.totalMemory() - runtime.freeMemory()) / mb);
		LorittaAnalytics.logger.info("Free Memory:"
				+ runtime.freeMemory() / mb)
		LorittaAnalytics.logger.info("Total Memory:" + runtime.totalMemory() / mb)
		LorittaAnalytics.logger.info("Max Memory:" + runtime.maxMemory() / mb)
		LorittaAnalytics.logger.info("commandManager.commandMap.size: ${loritta.commandManager.commandMap.size}")
		LorittaAnalytics.logger.info("commandManager.defaultCmdOptions.size: ${loritta.commandManager.defaultCmdOptions.size}")
		LorittaAnalytics.logger.info("dummyServerConfig.guildUserData.size: ${loritta.dummyServerConfig.guildUserData.size}")
		LorittaAnalytics.logger.info("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
		LorittaAnalytics.logger.info("locales.size: ${loritta.locales.size}")
		LorittaAnalytics.logger.info("ignoreIds.size: ${loritta.ignoreIds.size}")
		LorittaAnalytics.logger.info("userCooldown.size: ${loritta.userCooldown.size}")
		LorittaAnalytics.logger.info("southAmericaMemesPageCache.size: ${loritta.southAmericaMemesPageCache.size}")
		LorittaAnalytics.logger.info("musicManagers.size: ${loritta.audioManager.musicManagers.size}")
		LorittaAnalytics.logger.info("Total Track Queue: ${loritta.audioManager.musicManagers.values.sumBy { it.scheduler.queue.size }}")
		LorittaAnalytics.logger.info("songThrottle.size: ${loritta.audioManager.songThrottle.size}")
		LorittaAnalytics.logger.info("youTubeKeys.size: ${loritta.youtubeKeys.size}")
		LorittaAnalytics.logger.info("fanArts.size: ${loritta.fanArts.size}")
		LorittaAnalytics.logger.info("storedLastIds.size: ${AminoRepostThread.storedLastIds.size}")
		LorittaAnalytics.logger.info("gameInfoCache.size: ${NewLivestreamThread.gameInfoCache.size}")
		LorittaAnalytics.logger.info("isLivestreaming.size: ${NewLivestreamThread.isLivestreaming.size}")
		LorittaAnalytics.logger.info("displayNameCache.size: ${NewLivestreamThread.displayNameCache.size}")
		LorittaAnalytics.logger.info("lastItemTime.size: ${NewRssFeedThread.lastItemTime.size}")
		LorittaAnalytics.logger.info("YouTube's Last Notified: ${CreateYouTubeWebhooksTask.lastNotified.size}")
		LorittaAnalytics.logger.info("executor: ${(loritta.executor as ThreadPoolExecutor).activeCount}")
		LorittaAnalytics.logger.info("Total Thread Count: ${Thread.getAllStackTraces().keys.size}")
	}
}