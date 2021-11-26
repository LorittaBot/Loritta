import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.browser.document
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.spicymorenitta.dashboard.styles.AppStylesheet
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.SidebarCategory
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.SidebarEntry
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.Test
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.UserOverviewContent
import net.perfectdreams.loritta.spicymorenitta.dashboard.components.userdash.UserOverview
import net.perfectdreams.loritta.spicymorenitta.dashboard.screen.Screen
import net.perfectdreams.loritta.spicymorenitta.dashboard.utils.RoutingManager
import net.perfectdreams.loritta.webapi.data.CreateSessionResponse
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Hr
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.get
import org.w3c.dom.parsing.DOMParser

val http = HttpClient {}

class SpicyMorenitta {
    val routingManager = RoutingManager(this)

    fun start() {
        routingManager.switchToUserOverview()

        val a = 297153970613387264L
        console.log(a.toBigInteger().toString())
        println(a.toULong())
        println("Something is going on $a")

        // Load the session token
        GlobalScope.launch {
            val response = Json.decodeFromString<CreateSessionResponse>(http.post("http://127.0.0.1:8000/api/v1/session"))

            println(response.token)
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