package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import io.ktor.client.request.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> HttpRequestBuilder.setJsonBody(data: T) {
    setBody(
        Json.encodeToString(data)
    )
}