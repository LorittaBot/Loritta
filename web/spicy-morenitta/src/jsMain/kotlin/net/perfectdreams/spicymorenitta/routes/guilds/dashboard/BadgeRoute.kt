package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import kotlinx.browser.document
import kotlinx.serialization.Serializable
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import net.perfectdreams.spicymorenitta.views.dashboard.Stuff
import net.perfectdreams.spicymorenitta.views.dashboard.getPlan
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

class BadgeRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/badge") {
	override val keepLoadingScreen: Boolean
		get() = true

	@Serializable
	class PartialGuildConfiguration(
			val activeDonationKeys: List<ServerConfig.DonationKey>,
			val donationConfig: ServerConfig.DonationConfig
	)

	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<DailyMultiplierRoute.PartialGuildConfiguration>(call.parameters["guildid"]!!, "donation", "activekeys")
			switchContentAndFixLeftSidebarScroll(call)

			document.select<HTMLDivElement>("#save-button").onClick {
				prepareSave()
			}

			(page.getElementById("cmn-toggle-1") as HTMLInputElement).checked = guild.donationConfig.customBadge

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-1") {
				val donationValue = guild.activeDonationKeys.getPlan()

				if (!donationValue.hasCustomBadge) {
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
		SaveUtils.prepareSave("donation", extras = {
			it["customBadge"] = (page.getElementById("cmn-toggle-1") as HTMLInputElement).checked
			it["badgeImage"] = base64Image
		})
	}
}