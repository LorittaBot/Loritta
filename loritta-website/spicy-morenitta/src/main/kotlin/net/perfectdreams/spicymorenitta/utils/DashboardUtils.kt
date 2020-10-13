package net.perfectdreams.spicymorenitta.utils

import io.ktor.client.request.*
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.decodeFromString
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig

object DashboardUtils {
	suspend fun retrieveGuildConfiguration(guildId: Long) = retrieveGuildConfiguration(guildId.toString())

	suspend fun retrieveGuildConfiguration(guildId: String): ServerConfig.Guild {
		val result = http.get<String> {
			url("${window.location.origin}/api/v1/guilds/${guildId}/config")
		}

		return kotlinx.serialization.json.JSON.nonstrict.decodeFromString(result)
	}

	suspend inline fun <reified T : Any> retrievePartialGuildConfiguration(guildId: String, vararg sections: String): T {
		val result = http.get<String> {
			url("${window.location.origin}/api/v1/guilds/${guildId}/config/${sections.joinToString(",")}")
		}

		return kotlinx.serialization.json.JSON.nonstrict.decodeFromString(result)
	}

	fun UpdateNavbarSizePostRender.switchContentAndFixLeftSidebarScroll(call: ApplicationCall) {
		fixDummyNavbarHeight(call)
		SpicyMorenitta.INSTANCE.fixLeftSidebarScroll {
			switchContent(call)
		}
	}

	fun UpdateNavbarSizePostRender.launchWithLoadingScreenAndFixContent(call: ApplicationCall, callback: suspend CoroutineScope.() -> (Unit)) {
		SpicyMorenitta.INSTANCE.launch {
			SpicyMorenitta.INSTANCE.showLoadingScreen()
			callback.invoke(this)
			SpicyMorenitta.INSTANCE.hideLoadingScreen()
		}
	}
}