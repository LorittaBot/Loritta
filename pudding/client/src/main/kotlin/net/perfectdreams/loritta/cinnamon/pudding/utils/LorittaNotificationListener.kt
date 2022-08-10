package net.perfectdreams.loritta.cinnamon.pudding.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotificationResponse

class LorittaNotificationListener(m: Pudding) {
    val notifications = MutableSharedFlow<LorittaNotification>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val postgreSQLNotificationListener = PostgreSQLNotificationListener(
        m,
        mapOf(
            "loritta" to {
                val lorittaNotification = Json.decodeFromString<LorittaNotification>(it)

                coroutineScope.launch {
                    notifications.emit(lorittaNotification)
                }
            }
        )
    )

    fun start() = Thread(null, postgreSQLNotificationListener, "PostgreSQL Notification Listener").start()
}