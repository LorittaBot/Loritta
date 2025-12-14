package net.perfectdreams.loritta.morenitta.utils

import com.google.common.util.concurrent.ThreadFactoryBuilder
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.analytics.InternalAnalyticSender
import net.perfectdreams.loritta.morenitta.utils.eventlog.DeleteOldStoredMessagesTask
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LorittaTasks(val loritta: LorittaBot) {
	lateinit var DAILY_TAX_TASK: DailyTaxTask

	fun startTasks() {
		DAILY_TAX_TASK = DailyTaxTask(loritta)

		scheduleWithFixedDelay(SponsorsSyncTask(loritta), 0L, 1L, TimeUnit.MINUTES)
		scheduleWithFixedDelay(InternalAnalyticSender(loritta), 0L, 15L, TimeUnit.SECONDS)
		scheduleWithFixedDelay(DAILY_TAX_TASK, 0L, 15L, TimeUnit.SECONDS)
		scheduleWithFixedDelay(DeleteOldStoredMessagesTask(loritta), 0L, 1L, TimeUnit.HOURS)

		if (loritta.isMainInstance) {
			val midnight = LocalTime.MIDNIGHT
			val today = LocalDate.now(ZoneOffset.UTC)
			val todayMidnight = LocalDateTime.of(today, midnight)
			val tomorrowMidnight = todayMidnight.plusDays(1)
			val diff = tomorrowMidnight.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

			scheduleAtFixedRate(LorittaDailyShopUpdateTask(loritta), diff, TimeUnit.DAYS.toMillis(1L), TimeUnit.MILLISECONDS)
		}
	}

	fun scheduleWithFixedDelay(task: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) {
		Executors.newScheduledThreadPool(1, ThreadFactoryBuilder().setNameFormat("${task::class.simpleName} Executor Thread-%d").build()).scheduleWithFixedDelay(task, initialDelay, delay, unit)
	}

	fun scheduleAtFixedRate(task: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) {
		Executors.newScheduledThreadPool(1, ThreadFactoryBuilder().setNameFormat("${task::class.simpleName} Executor Thread-%d").build()).scheduleAtFixedRate(task, initialDelay, delay, unit)
	}
}