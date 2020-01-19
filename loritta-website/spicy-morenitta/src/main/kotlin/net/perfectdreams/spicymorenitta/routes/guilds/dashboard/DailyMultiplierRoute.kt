package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import kotlinx.serialization.ImplicitReflectionSerializer
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.views.dashboard.Stuff
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import kotlin.browser.document

class DailyMultiplierRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/daily-multiplier") {
	override val keepLoadingScreen: Boolean
		get() = true

	@ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrieveGuildConfiguration(call.parameters["guildid"]!!)
			switchContentAndFixLeftSidebarScroll(call)

			document.select<HTMLDivElement>("#save-button").onClick {
				prepareSave()
			}

			(page.getElementById("cmn-toggle-1") as HTMLInputElement).checked = guild.donationConfig.dailyMultiplier

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-1") {
				if (guild.donationKey == null || 59.99 > guild.donationKey.value) {
					Stuff.showPremiumFeatureModal()
					return@applyBlur false
				}
				return@applyBlur true
			}
		}
	}

	fun prepareSave() {
		SaveUtils.prepareSave("daily_multiplier", extras = {
			it["dailyMultiplier"] = (page.getElementById("cmn-toggle-1") as HTMLInputElement).checked
		})
	}
}