package com.mrpowergamerbr.loritta.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mrpowergamerbr.loritta.analytics.AnalyticSender
import com.mrpowergamerbr.loritta.analytics.InternalAnalyticSender
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.eventlog.DeleteOldStoredMessagesTask
import com.mrpowergamerbr.loritta.utils.networkbans.ApplyBansTask
import com.mrpowergamerbr.loritta.website.OptimizeAssetsTask
import com.mrpowergamerbr.loritta.youtube.CreateYouTubeWebhooksTask
import net.perfectdreams.loritta.utils.LorittaDailyShopUpdateTask
import net.perfectdreams.loritta.utils.giveaway.SpawnGiveawayTask
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object LorittaTasks {
	lateinit var DAILY_TAX_TASK: DailyTaxTask

	fun startTasks() {
		DAILY_TAX_TASK = DailyTaxTask()

		if (loritta.config.loritta.environment == EnvironmentType.PRODUCTION)
			scheduleWithFixedDelay(LorittaLandRoleSync(), 0L, 1L, TimeUnit.MINUTES)
		scheduleWithFixedDelay(SponsorsSyncTask(), 0L, 1L, TimeUnit.MINUTES)
		scheduleWithFixedDelay(OptimizeAssetsTask(), 0L, 5L, TimeUnit.SECONDS)
		scheduleWithFixedDelay(MutedUsersTask(), 0L, 3L, TimeUnit.MINUTES)
		scheduleWithFixedDelay(AnalyticSender(), 0L, 1L, TimeUnit.MINUTES)
		scheduleWithFixedDelay(InternalAnalyticSender(), 0L, 15L, TimeUnit.SECONDS)
		scheduleWithFixedDelay(DAILY_TAX_TASK, 0L, 15L, TimeUnit.SECONDS)
		scheduleWithFixedDelay(ApplyBansTask(), 0L, 60L, TimeUnit.MINUTES)
		scheduleWithFixedDelay(SpawnGiveawayTask(), 0L, 1L, TimeUnit.HOURS)
		scheduleWithFixedDelay(DeleteOldStoredMessagesTask(), 0L, 1L, TimeUnit.HOURS)
		scheduleWithFixedDelay(UpdateFanArtsTask(), 0L, 5L, TimeUnit.MINUTES)

		if (loritta.isMaster) {
			val midnight = LocalTime.MIDNIGHT
			val today = LocalDate.now(ZoneOffset.UTC)
			val todayMidnight = LocalDateTime.of(today, midnight)
			val tomorrowMidnight = todayMidnight.plusDays(1)
			val diff = tomorrowMidnight.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

			scheduleAtFixedRate(LorittaDailyShopUpdateTask(), diff, TimeUnit.DAYS.toMillis(1L), TimeUnit.MILLISECONDS)

			scheduleWithFixedDelay(CreateYouTubeWebhooksTask(), 0L, 1L, TimeUnit.MINUTES)
		}
	}

	fun scheduleWithFixedDelay(task: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) {
		Executors.newScheduledThreadPool(1, ThreadFactoryBuilder().setNameFormat("${task::class.simpleName} Executor Thread-%d").build()).scheduleWithFixedDelay(task, initialDelay, delay, unit)
	}

	fun scheduleAtFixedRate(task: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) {
		Executors.newScheduledThreadPool(1, ThreadFactoryBuilder().setNameFormat("${task::class.simpleName} Executor Thread-%d").build()).scheduleAtFixedRate(task, initialDelay, delay, unit)
	}
}