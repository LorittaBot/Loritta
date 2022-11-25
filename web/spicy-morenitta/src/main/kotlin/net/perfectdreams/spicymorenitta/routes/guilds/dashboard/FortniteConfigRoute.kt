@file:JsExport
package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.serialization.Serializable
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.DashboardUtils
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.utils.SaveUtils
import net.perfectdreams.spicymorenitta.utils.onClick
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement

class FortniteConfigRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/fortnite") {
	companion object {
		private const val LOCALE_PREFIX = "modules.fortnite"
	}

	@Serializable
	class PartialGuildConfiguration(
			val textChannels: List<ServerConfig.TextChannel>,
			val fortniteConfig: ServerConfig.FortniteConfig
	)

	val rolesByExperience = mutableListOf<ServerConfig.RoleByExperience>()
	val experienceRoleRates = mutableListOf<ServerConfig.ExperienceRoleRate>()
	val noXpRoles = mutableListOf<Long>()
	val noXpChannels = mutableListOf<Long>()

	override fun onUnload() {
		rolesByExperience.clear()
		experienceRoleRates.clear()
		noXpRoles.clear()
		noXpChannels.clear()
	}

	override val keepLoadingScreen: Boolean
		get() = true

	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "textchannels", "fortnite")
			switchContentAndFixLeftSidebarScroll(call)

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}

			val stuff = document.select<HTMLDivElement>("#fortnite-stuff")

			stuff.append {
				div {
					createToggle(
							locale["$LOCALE_PREFIX.notifyItemRotation.title"],
							locale["$LOCALE_PREFIX.notifyItemRotation.subtext"],
							id = "advertise-new-items",
							isChecked = guild.fortniteConfig.advertiseNewItems,
							onChange = {
								updateDisabledSections()
								it
							}
					)

					hr {}

					div {
						id = "shop-announcement-channel"

						h5(classes = "section-title") {
							+ locale["$LOCALE_PREFIX.notifyItemRotation.channel"]
						}

						select {
							id = "choose-channel-shop"
							style = "width: 320px;"

							for (channel in guild.textChannels) {
								option {
									+("#${channel.name}")

									if (channel.id == guild.fortniteConfig.channelToAdvertiseNewItems)
										selected = true

									value = channel.id.toString()
								}
							}
						}
					}
				}
			}

			updateDisabledSections()
		}
	}

	fun updateDisabledSections() {
		if (document.select<HTMLInputElement>("#advertise-new-items").checked)
			document.select<HTMLDivElement>("#shop-announcement-channel").removeClass("blurSection")
		else
			document.select<HTMLDivElement>("#shop-announcement-channel").addClass("blurSection")

		/* val announcementTypeSelect = document.select<HTMLSelectElement>("#announcement-type")

		val announcementValue = LevelUpAnnouncementType.valueOf(announcementTypeSelect.value)

		if (announcementValue != LevelUpAnnouncementType.DISABLED) {
			document.select<HTMLDivElement>("#level-up-message").removeClass("blurSection")
		} else {
			document.select<HTMLDivElement>("#level-up-message").addClass("blurSection")
		}

		if (announcementValue == LevelUpAnnouncementType.DIFFERENT_CHANNEL) {
			document.select<HTMLDivElement>("#select-custom-channel").removeClass("blurSection")
		} else {
			document.select<HTMLDivElement>("#select-custom-channel").addClass("blurSection")
		} */
	}

	@JsName("prepareSave")
	fun prepareSave() {
		SaveUtils.prepareSave("fortnite", extras = {
			it["advertiseNewItems"] = document.select<HTMLInputElement>("#advertise-new-items").checked
			it["channelToAdvertiseNewItems"] = document.select<HTMLSelectElement>("#choose-channel-shop").value
		})
	}
}