package net.perfectdreams.loritta.cinnamon.pudding.utils

import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification

class LorittaNotificationListener(hikariDataSource: HikariDataSource, val name: String = "Loritta PostgreSQL Notification Listener") {
    val notifications = MutableSharedFlow<LorittaNotification>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val postgreSQLNotificationListener = PostgreSQLNotificationListener(
        hikariDataSource,
        mapOf(
            "loritta" to {
                val lorittaNotification = Json.decodeFromString<LorittaNotification>(it)

                coroutineScope.launch {
                    notifications.emit(lorittaNotification)
                }
            }
        )
    )

    fun start() = Thread(null, postgreSQLNotificationListener, name).start()
}