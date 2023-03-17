@file:JsExport
package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

class GamerSaferVerifyRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/gamersafer-verify") {
	override val keepLoadingScreen: Boolean
		get() = true

	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			renderComposable("gamersafer-verify-wrapper") {
				Div {
					Text("owo")
				}
			}
		}
	}
}