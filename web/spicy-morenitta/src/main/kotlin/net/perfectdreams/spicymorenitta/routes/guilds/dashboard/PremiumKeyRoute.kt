package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.Serializable
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.extensions.listIsEmptySection
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import kotlin.js.Date

class PremiumKeyRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/premium") {
	override val keepLoadingScreen: Boolean
		get() = true

	@Serializable
	class PartialGuildConfiguration(
			val guildInfo: ServerConfig.MiniGuild,
			val activeDonationKeys: List<ServerConfig.DonationKey>,
			val donationKeys: Array<ServerConfig.DonationKey>
	)

	var guildId: Long? = null
	var guildInfo: ServerConfig.MiniGuild? = null
	var currentActiveKeys = mutableListOf<Long>()

	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			guildId = call.parameters["guildid"]!!.toLong()
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "userkeys", "activekeys", "guildinfo")
			switchContentAndFixLeftSidebarScroll(call)
			generateStuff(guild)
		}
	}

	fun generateStuff(guild: PartialGuildConfiguration) {
		currentActiveKeys = guild.activeDonationKeys.map { it.id }.toMutableList()
		guildInfo = guild.guildInfo
		val premiumContent = page.getElementById("premium-content")
		premiumContent.clear()

		premiumContent.appendChild(
				document.create.div {
					id = "premium-stuff"
					val activeDonationKeys = guild.activeDonationKeys.filter { it.expiresAt >= Date().getTime() }
					h1 { + "Plano Atual:" }
					val activeValue = activeDonationKeys.sumByDouble { it.value }
					if (activeValue >= PremiumPlans.COMPLETE.value) {
						p {
							+ "Completo"
						}
					} else if (activeValue >= PremiumPlans.RECOMMENDED.value) {
						p {
							+ "Recomendado"
						}
					} else if (activeValue >= PremiumPlans.ESSENTIAL.value) {
						p {
							+"Essencial"
						}
					} else {
						p {
							+ "Grátis"
						}
					}

					h1 { + "Keys ativas neste servidor" }
					div {
						if (activeDonationKeys.isEmpty()) {
							listIsEmptySection()
						} else {
							for (donationKey in activeDonationKeys) {
								createKeyEntry(donationKey, true)
							}
						}

					}
					hr {}
					h1 { + "Suas Keys" }
					// Apenas mostrar keys que estão ainda válidas (Ou seja, que ainda não expiraram!)
					val userKeys = guild.donationKeys
							.filter { it.expiresAt >= Date().getTime() }
							.filter { it.id !in currentActiveKeys }
					if (userKeys.isEmpty()) {
						listIsEmptySection()
					} else {
						for (donationKey in userKeys) {
							createKeyEntry(donationKey, false)
						}
					}
				})
	}

	fun DIV.createKeyEntry(donationKey: ServerConfig.DonationKey, isActivatedHere: Boolean) {
		this.div(classes = "discord-generic-entry timer-entry") {
			img(classes = "amino-small-image") {
				style = "width: 6%; height: auto; border-radius: 999999px; float: left; position: relative; bottom: 8px;"
				src = if (isActivatedHere) {
					guildInfo?.iconUrl ?: ""
				} else if (donationKey.activeIn != null) {
					donationKey.activeIn.iconUrl ?: ""
				} else {
					donationKey.user?.effectiveAvatarUrl ?: ""
				}
			}
			div(classes = "pure-g") {
				div(classes = "pure-u-1 pure-u-md-18-24") {
					div {
						style = "margin-left: 10px; margin-right: 10;"
						div(classes = "amino-title entry-title") {
							style = "font-family: Whitney,Helvetica Neue,Helvetica,Arial,sans-serif;"
							+ "Key ${donationKey.id}"
						}
						div(classes = "amino-title toggleSubText") {
							if (donationKey.activeIn != null) {
								+"R$${donationKey.value} • Ativo em ${donationKey.activeIn.name} • Expirará em ${DateUtils.formatDateDiff(Date().getTime(), donationKey.expiresAt.toDouble())}"
							} else {
								+"R$${donationKey.value} • Expirará em ${DateUtils.formatDateDiff(Date().getTime(), donationKey.expiresAt.toDouble())}"
							}
						}
					}
				}
				div(classes = "pure-u-1 pure-u-md-6-24 vertically-centered-right-aligned") {
					button(classes="button-discord button-discord-edit pure-button edit-timer-button") {
						onClickFunction = {
							if (isActivatedHere)
								currentActiveKeys.remove(donationKey.id)
							else
								currentActiveKeys.add(donationKey.id)

							SaveUtils.prepareSave("activekeys", {
								it["keyIds"] = currentActiveKeys.map { it.toString() }
							}, onFinish = {
								m.launch {
									val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(guildId.toString(), "userkeys", "activekeys", "guildinfo")

									generateStuff(guild)
								}
							})
						}

						+ if (isActivatedHere) "Desativar" else "Ativar"
					}
				}
			}
		}
	}
}