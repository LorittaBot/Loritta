package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import net.perfectdreams.spicymorenitta.views.dashboard.Stuff
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import kotlin.browser.document
import kotlin.dom.clear

class MusicConfigRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/music") {
	override val keepLoadingScreen: Boolean
		get() = true

	@Serializable
	class PartialGuildConfiguration(
			val activeDonationKeys: List<ServerConfig.DonationKey>,
			val voiceChannels: List<ServerConfig.VoiceChannel>,
			val musicConfig: ServerConfig.MusicConfig
	)

	val musicChannels = mutableListOf<Long>()

	override fun onUnload() {
		super.onUnload()
		musicChannels.clear()
	}

	@ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "music", "activekeys", "voicechannels")
			val activeDonationKeyValue = guild.activeDonationKeys.sumByDouble { it.value }
			switchContentAndFixLeftSidebarScroll(call)
			musicChannels.addAll(guild.musicConfig.channels)

			document.select<HTMLDivElement>("#save-button").onClick {
				prepareSave()
			}

			document.select<HTMLDivElement>("#music-stuff").append {
				div {
					createToggle("Ativar", "nosa", "enable-music-toggle", guild.musicConfig.enabled) { result ->
						if (result) {
							if (19.99 > activeDonationKeyValue) {
								Stuff.showPremiumFeatureModal()
								return@createToggle false
							}
						}
						result
					}

					div(classes = "flavourText") {
						+ "Canais de Música"
					}

					div(classes = "toggleSubText") {
						+ "Wow, que incrível!"
					}

					select {
						id = "choose-channel-blacklisted"
						style = "width: 320px;"

						for (channel in guild.voiceChannels) {
							option {
								+ ("#${channel.name}")

								value = channel.id
							}
						}
					}

					+ " "

					button(classes = "button-discord button-discord-info pure-button") {
						+ locale["loritta.add"]

						onClickFunction = {
							val role = document.select<HTMLSelectElement>("#choose-channel-blacklisted").value

							musicChannels.add(role.toLong())

							updateBlacklistedChannelsList(guild)
						}
					}

					div(classes = "list-wrapper") {
						id = "channel-blacklisted-list"
					}

					hr {}
				}
			}

			updateBlacklistedChannelsList(guild)
		}
	}

	fun updateBlacklistedChannelsList(guild: PartialGuildConfiguration) {
		val list = document.select<HTMLDivElement>("#channel-blacklisted-list")

		list.clear()

		list.append {
			musicChannels.forEach { channelId ->
				val guildChannel = guild.voiceChannels.firstOrNull { it.id.toLong() == channelId } ?: return@forEach

				list.append {
					span(classes = "discord-mention") {
						style = "cursor: pointer; margin-right: 4px;"

						+ "#${guildChannel.name}"

						onClickFunction = {
							musicChannels.remove(channelId)

							updateBlacklistedChannelsList(guild)
						}

						span {
							style = "border-left: 1px solid rgba(0, 0, 0, 0.15);opacity: 0.7;padding-left: 3px;font-size: 14px;margin-left: 3px;padding-right: 3px;"

							i(classes = "fas fa-times") {}
						}
					}
				}
			}
		}
	}

	fun prepareSave() {
		SaveUtils.prepareSave("music", extras = {
			it["enabled"] = (page.getElementById("enable-music-toggle") as HTMLInputElement).checked
			it["channels"] = musicChannels.map { it.toString() }
		})
	}
}