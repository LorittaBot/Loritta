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
import org.w3c.files.FileReader
import kotlin.browser.document

class BadgeRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/badge") {
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

			(page.getElementById("cmn-toggle-1") as HTMLInputElement).checked = guild.donationConfig.customBadge

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-1") {
				if (guild.donationKey == null || 19.99 > guild.donationKey.value) {
					Stuff.showPremiumFeatureModal()
					return@applyBlur false
				}
				return@applyBlur true
			}
		}
	}

	fun prepareSave() {
		val file = page.getElementById("upload-badge").asDynamic().files[0]

		if (file != null) {
			val reader = FileReader()

			reader.readAsDataURL(file)
			reader.onload = {
				val imageAsBase64 = reader.result
				save(imageAsBase64 as? String)
			}
		} else {
			save(null)
		}
	}

	fun save(base64Image: String?) {
		SaveUtils.prepareSave("badge", extras = {
			it["customBadge"] = (page.getElementById("cmn-toggle-1") as HTMLInputElement).checked
			it["badgeImage"] = base64Image
		})
	}
}