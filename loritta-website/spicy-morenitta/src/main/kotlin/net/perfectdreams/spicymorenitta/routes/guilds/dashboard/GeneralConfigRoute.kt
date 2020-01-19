package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import jq
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.ImplicitReflectionSerializer
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.DashboardUtils
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.utils.Placeholders
import net.perfectdreams.spicymorenitta.utils.SaveUtils
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.removeClass

class GeneralConfigRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/dashboard/configure/{guildid}") {
	companion object {
		private const val LOCALE_PREFIX = "modules.levelUp"
	}

	val blacklistedChannels = mutableListOf<Long>()

	override fun onUnload() {
		blacklistedChannels.clear()
	}

	override val keepLoadingScreen: Boolean
		get() = true

	@ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrieveGuildConfiguration(call.parameters["guildid"]!!)
			blacklistedChannels.addAll(guild.blacklistedChannels)

			switchContentAndFixLeftSidebarScroll(call)

			val stuff = document.select<HTMLDivElement>("#general-stuff")

			stuff.append {
				div(classes = "userOptionsWrapper") {
					div(classes = "pure-g") {
						div(classes = "pure-u-1 pure-u-md-1-6") {
							img(src = "/assets/img/lori_avatar_v3.png") {
								style = "border-radius: 99999px; height: 100px;"
							}
						}
						div(classes = "pure-u-1 pure-u-md-2-3") {
							div(classes = "flavourText") {
								+ "Prefixo da Loritta"
							}

							input(type = InputType.text) {
								id = "command-prefix"
								value = guild.commandPrefix
							}
						}
					}
				}

				div {
					createToggle(
							locale["website.dashboard.general.deleteMessagesAfterExecuting.title"],
							locale["website.dashboard.general.deleteMessagesAfterExecuting.subtext"],
							"delete-message-after-command",
							guild.deleteMessageAfterCommand
					)

					hr {}

					createToggle(
							locale["website.dashboard.general.tellMissingPermissionOnAChannel.title"],
							locale["website.dashboard.general.tellMissingPermissionOnAChannel.subtext"],
							"warn-on-missing-permission",
							guild.warnOnMissingPermission
					)

					hr {}

					createToggle(
							locale["website.dashboard.general.tellUserWhenUsingUnknownCommand.title"],
							locale["website.dashboard.general.tellUserWhenUsingUnknownCommand.subtext"],
							"warn-on-unknown-command",
							guild.warnOnUnknownCommand
					)

					hr {}

					div(classes = "flavourText") {
						+ locale["website.dashboard.general.blacklistedChannels.title"]
					}

					div(classes = "toggleSubText") {
						+ locale["website.dashboard.general.blacklistedChannels.subtext"]
					}

					select {
						id = "choose-channel-blacklisted"
						style = "width: 320px;"

						for (channel in guild.textChannels) {
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

							blacklistedChannels.add(role.toLong())

							updateBlacklistedChannelsList(guild)
						}
					}

					div(classes = "list-wrapper") {
						id = "channel-blacklisted-list"
					}

					hr {}

					createToggle(
							locale["website.dashboard.general.tellUserWhenUsingCommandsOnABlacklistedChannel.title"],
							locale["website.dashboard.general.tellUserWhenUsingCommandsOnABlacklistedChannel.subtext"],
							"warn-if-blacklisted",
							guild.warnIfBlacklisted
					) {
						updateDisabledSections()
					}

					hr {}

					div {
						id = "hidden-if-disabled-blacklist"

						div(classes = "flavourText") {
							+ "???"
						}

						textArea {
							id = "blacklisted-warning"
							+ (guild.blacklistedWarning ?: "???")
						}
					}

					hr {}

					button(classes = "button-discord button-discord-success pure-button") {
						+ locale["loritta.save"]

						onClickFunction = {
							prepareSave()
						}
					}
				}
			}

			LoriDashboard.configureTextArea(
					jq("#blacklisted-warning"),
					true,
					null,
					false,
					null,
					true,
					Placeholders.DEFAULT_PLACEHOLDERS,
					customTokens = mapOf(),
					showTemplates = false
			)

			updateBlacklistedChannelsList(guild)
			updateDisabledSections()
		}
	}

	fun updateDisabledSections() {
		val warnIfBlacklisted = document.select<HTMLInputElement>("#warn-if-blacklisted").checked

		if (warnIfBlacklisted) {
			document.select<HTMLDivElement>("#hidden-if-disabled-blacklist").removeClass("blurSection")
		} else {
			document.select<HTMLDivElement>("#hidden-if-disabled-blacklist").addClass("blurSection")
		}
	}

	fun updateBlacklistedChannelsList(guild: ServerConfig.Guild) {
		val list = document.select<HTMLDivElement>("#channel-blacklisted-list")

		list.clear()

		list.append {
			blacklistedChannels.forEach { channelId ->
				val guildChannel = guild.textChannels.firstOrNull { it.id.toLong() == channelId } ?: return@forEach

				list.append {
					span(classes = "discord-mention") {
						style = "cursor: pointer; margin-right: 4px;"

						+ "#${guildChannel.name}"

						onClickFunction = {
							blacklistedChannels.remove(channelId)

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
		SaveUtils.prepareSave("default", extras = {
			it["commandPrefix"] = document.select<HTMLInputElement>("#command-prefix").value
			it["deleteMessageAfterCommand"] = document.select<HTMLInputElement>("#delete-message-after-command").checked
			it["warnOnMissingPermission"] = document.select<HTMLInputElement>("#warn-on-missing-permission").checked
			it["warnOnUnknownCommand"] = document.select<HTMLInputElement>("#warn-on-unknown-command").checked
			it["blacklistedChannels"] = blacklistedChannels.map { it.toString() }
			val warnIfBlacklisted = document.select<HTMLInputElement>("#warn-if-blacklisted").checked
			it["warnIfBlacklisted"] = warnIfBlacklisted
			if (warnIfBlacklisted) {
				it["blacklistedWarning"] = document.select<HTMLTextAreaElement>("#blacklisted-warning").value
			}
		})
	}
}