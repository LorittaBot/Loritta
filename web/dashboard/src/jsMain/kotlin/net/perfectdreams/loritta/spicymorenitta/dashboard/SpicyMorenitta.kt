
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.formatters.IntlMFFormatter
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.userdash.UserOverview
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.RoutingManager
import net.perfectdreams.loritta.webapi.data.CreateSessionResponse
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

val http = HttpClient {}

class SpicyMorenitta {
    val routingManager = RoutingManager(this)

    fun start() {
        routingManager.switchToUserOverview()

        // Load the session token
        GlobalScope.launch {
            val response = Json.decodeFromString<CreateSessionResponse>(http.post("http://127.0.0.1:8000/api/v1/session"))

            println(response.token)
        }

        GlobalScope.launch {
            val result = http.get<String>("http://127.0.0.1:8000/api/v1/languages/en") {}

            val i18nContext = I18nContext(
                IntlMFFormatter(),
                Json.decodeFromString(result)
            )

            println(i18nContext.get(I18nKeysData.Achievements.Achievement.IsThatAnUndertaleReference.Title))
        }

        renderComposable(rootElementId = "root") {
            Style(AppStylesheet)

            // TODO: This is unused
            if (routingManager.loading) {
                Text("Loading something cool and epic... Hang tight!")
            } else {
                when (val screen = routingManager.delegatedScreenState) {
                    is Screen.UserOverview -> UserOverview(this@SpicyMorenitta, screen)
                    Screen.Test -> TODO()
                }
            }
        }
    }

    data class GuildData(
        val id: Long,
        val name: String,
        val icon: String,
        val features: List<String>
    )
}