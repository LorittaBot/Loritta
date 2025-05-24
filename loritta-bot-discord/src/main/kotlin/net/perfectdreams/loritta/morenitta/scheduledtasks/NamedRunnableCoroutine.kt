package net.perfectdreams.loritta.morenitta.scheduledtasks

import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.tables.GlobalTasks
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

interface NamedRunnableCoroutine : RunnableCoroutine {
    val taskName: String

    fun updateStoredTimer(loritta: LorittaBot) {
        GlobalTasks.upsert(GlobalTasks.id) {
            it[GlobalTasks.id] = this@NamedRunnableCoroutine.taskName
            it[GlobalTasks.lastExecutedByClusterId] = loritta.clusterId
            it[GlobalTasks.lastExecutedAt] = Instant.now()
        }
    }
}