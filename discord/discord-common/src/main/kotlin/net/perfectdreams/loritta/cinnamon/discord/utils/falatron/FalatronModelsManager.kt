package net.perfectdreams.loritta.cinnamon.discord.utils.falatron

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
import net.perfectdreams.loritta.cinnamon.utils.JsonIgnoreUnknownKeys
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
        models.emit(JsonIgnoreUnknownKeys.decodeFromString<FalatronModelResponse>(response.bodyAsText()).models)
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
        val dublador: String,
        val image: String,
        val name: String,
        val path: String
    )
}