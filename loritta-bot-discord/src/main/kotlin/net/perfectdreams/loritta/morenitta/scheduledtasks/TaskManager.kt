package net.perfectdreams.loritta.morenitta.scheduledtasks

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.cinnamon.pudding.tables.GlobalTasks
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

class TaskManager(val m: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    // uuhh how does this work?

    suspend fun scheduleCoroutineEveryDayAtSpecificHour(
        time: LocalTime,
        action: NamedRunnableCoroutine
    ) {
        val task = m.transaction {
            GlobalTasks
                .selectAll()
                .where {
                    GlobalTasks.id eq action.taskName
                }
                .firstOrNull()
        }

        if (task != null) {
            // If the task has already been used in the past, we need to figure out if he SHOULD dispatch it asap or not
            val lastExecutedAt = task[GlobalTasks.lastExecutedAt]
            val lastExecutedAtWithOffset = lastExecutedAt.atOffset(ZoneOffset.UTC)
            var lastExecutedAtTemp = lastExecutedAtWithOffset

            while (true) {
                val lastExecutedAtWithTime = lastExecutedAtTemp
                    .withHour(time.hour)
                    .withMinute(time.minute)
                    .withSecond(time.second)
                    .withNano(time.nano)

                val gonnaBeScheduledAtTime = if (lastExecutedAtTemp >= lastExecutedAtWithTime) {
                    // If today at time is larger than today, then it means that we need to schedule it for tomorrow
                    lastExecutedAtWithTime.plusDays(1)
                } else lastExecutedAtWithTime

                val now = Instant.now()

                if (gonnaBeScheduledAtTime >= now.atOffset(ZoneOffset.UTC)) {
                    // logger.info { "Time is in the future" }
                    // Is in the future, should be scheduled
                    val diff = gonnaBeScheduledAtTime.toInstant().toEpochMilli() - System.currentTimeMillis()

                    scheduleCoroutineAtFixedRate(
                        m.tasksScope,
                        1.days,
                        diff.milliseconds,
                        action
                    )
                    break
                } else {
                    // Is in the past, should be executed now
                    executeTask(action)

                    lastExecutedAtTemp = gonnaBeScheduledAtTime
                }
            }
        } else {
            // If the task is null, then we need to calculate it based on "now"
            val now = Instant.now()
            val today = LocalDate.now(ZoneOffset.UTC)
            val todayAtTime = LocalDateTime.of(today, time)
            val gonnaBeScheduledAtTime =  if (now > todayAtTime.toInstant(ZoneOffset.UTC)) {
                // If today at time is larger than today, then it means that we need to schedule it for tomorrow
                todayAtTime.plusDays(1)
            } else todayAtTime

            val diff = gonnaBeScheduledAtTime.toInstant(ZoneOffset.UTC).toEpochMilli() - System.currentTimeMillis()

            scheduleCoroutineAtFixedRate(
                m.tasksScope,
                1.days,
                diff.milliseconds,
                action
            )
        }
    }

    /**
     * Schedules [action] to be executed on [scope] every [period] with a [initialDelay]
     */
    fun scheduleCoroutineAtFixedRate(scope: CoroutineScope, period: Duration, initialDelay: Duration = Duration.Companion.ZERO, action: NamedRunnableCoroutine) {
        logger.info { "Scheduling ${action.taskName} to be ran every $period with a $initialDelay initial delay" }

        val taskName = action.taskName
        val logger by HarmonyLoggerFactory.logger(taskName)

        scope.launch(CoroutineName("$taskName Scheduler")) {
            delay(initialDelay)

            val mutex = Mutex()

            while (true) {
                launch(CoroutineName("$taskName Task")) {
                    logger.info { "Preparing to run task $taskName - Is mutex locked? ${mutex.isLocked}" }
                    mutex.withLock {
                        executeTask(action)
                    }
                }
                logger.info { "Waiting $period to execute next $taskName task..." }
                delay(period)
            }
        }
    }

    /**
     * Executes the [action]'s task
     */
    // This is split up in a separate function to avoid code duplication
    private suspend fun executeTask(action: NamedRunnableCoroutine) {
        val taskName = action.taskName

        logger.info { "Running task $taskName..." }
        try {
            action.run()
        } catch (e: Throwable) {
            logger.warn(e) { "Uncaught error when running the task $taskName!" }
        }
        logger.info { "Task $taskName has finished running!" }
    }
}