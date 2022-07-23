package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.soundbox

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

class FalatronModelsManager {
    val http = HttpClient {}
    val models = MutableStateFlow<List<FalatronModel>>(emptyList())
    val scope = CoroutineScope(Dispatchers.IO)

    fun startUpdater() {
        scope.launch {
            update()
            delay(30.minutes)
        }
    }

    private suspend fun update() {
        val response = http.get("https://falatron.com/static/models.json")
        models.emit(Json.decodeFromString<FalatronModelResponse>(response.bodyAsText()).models)
    }

    @Serializable
    data class FalatronModelResponse(
        val models: List<FalatronModel>
    )

    @Serializable
    data class FalatronModel(
        val author: String,
        val category: String,
        val description: String,
        val image: String,
        val name: String,
        val path: String
    )
}