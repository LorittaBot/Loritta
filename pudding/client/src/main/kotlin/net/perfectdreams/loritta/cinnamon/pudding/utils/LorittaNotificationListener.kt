package net.perfectdreams.loritta.cinnamon.pudding.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification

fun LorittaNotificationListener(
    m: Pudding,
    callback: (LorittaNotification) -> (Unit)
) = PostgreSQLNotificationListener(
    m,
    mapOf(
        "loritta" to {
            val lorittaNotification = Json.decodeFromString<LorittaNotification>(it)

            callback.invoke(lorittaNotification)
        }
    )
)