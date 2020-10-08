package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import jq
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onInputFunction
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
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.removeClass

class GeneralConfigRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure") {
	companion object {
		private const val LOCALE_PREFIX = "website.dashboard.general"
	}

	@Serializable
	class PartialGuildConfiguration(
			val general: ServerConfig.GeneralConfig,
			val textChannels: List<ServerConfig.TextChannel>
	)

	val blacklistedChannels = mutableListOf<Long>()

	override fun onUnload() {
		blacklistedChannels.clear()
	}

	override val keepLoadingScreen: Boolean
		get() = true

	private fun DIV.createFakeMessage(avatarUrl: String, name: String, content: DIV.() -> (Unit)) {
		div {
			style = "display: flex; align-items: center;"

			div {
				style = "display: flex; flex-direction: column; margin-right: 10px;"

				img(src = avatarUrl) {
					style = "border-radius: 100%;"
					width = "40"
				}
			}

			div {
				style = "display: flex; flex-direction: column;"

				div {
					style = "font-weight: 600;"
					+ name
				}

				div {
					content.invoke(this)
				}
			}
		}
	}

	@ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "general", "textchannels")
			blacklistedChannels.addAll(guild.general.blacklistedChannels)

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
								+ locale["${LOCALE_PREFIX}.commandPrefix.title"]
							}

							div(classes = "toggleSubText") {
								+ locale["${LOCALE_PREFIX}.commandPrefix.subtext"]
							}

							input(type = InputType.text) {
								id = "command-prefix"
								value = guild.general.commandPrefix

								onInputFunction = {
									val commandPrefix = document.select<HTMLInputElement>("#command-prefix")

									commandPrefix.value = commandPrefix.value.trim()

									document.select<HTMLSpanElement>("#command-prefix-preview")
											.innerText = commandPrefix.value
								}
							}
							div(classes = "discord-message-helper") {
								style = "display: flex; flex-direction: column; justify-content: center; font-family: Lato,Helvetica Neue,Helvetica,Arial,sans-serif;"

								createFakeMessage(
										m.userIdentification?.userAvatarUrl ?: "???",
										m.userIdentification?.username ?: "???"
								) {
									span {
										id = "command-prefix-preview"
										+ guild.general.commandPrefix
									}
									span {
										+ "ping"
									}
								}

								div {
									hr {}
								}

								createFakeMessage(
										"/assets/img/lori_avatar_v3.png",
										"Loritta"
								) {
									span {
										+ "\uD83C\uDFD3"
									}
									b {
										+ " | "
										span(classes = "discord-mention") {
											+ ("@" + (m.userIdentification?.username ?: "???"))
										}
										+ " Pong!"
									}
								}
							}
						}
					}
				}

				div {
					createToggle(
							locale["${LOCALE_PREFIX}.deleteMessagesAfterExecuting.title"],
							locale["${LOCALE_PREFIX}.deleteMessagesAfterExecuting.subtext"],
							"delete-message-after-command",
							guild.general.deleteMessageAfterCommand
					)

					hr {}

					createToggle(
							locale["${LOCALE_PREFIX}.tellUserWhenUsingUnknownCommand.title"],
							locale["${LOCALE_PREFIX}.tellUserWhenUsingUnknownCommand.subtext"],
							"warn-on-unknown-command",
							guild.general.warnOnUnknownCommand
					)

					hr {}

					div(classes = "flavourText") {
						+ locale["${LOCALE_PREFIX}.blacklistedChannels.title"]
					}

					div(classes = "toggleSubText") {
						+ locale["${LOCALE_PREFIX}.blacklistedChannels.subtext"]
					}

					select {
						id = "choose-channel-blacklisted"
						style = "width: 320px;"

						for (channel in guild.textChannels) {
							option {
								+ ("#${channel.name}")

								value = channel.id.toString()
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
							locale["${LOCALE_PREFIX}.tellUserWhenUsingCommandsOnABlacklistedChannel.title"],
							locale["${LOCALE_PREFIX}.tellUserWhenUsingCommandsOnABlacklistedChannel.subtext"],
							"warn-if-blacklisted",
							guild.general.warnIfBlacklisted
					) { result ->
						updateDisabledSections()
						result
					}

					hr {}

					div {
						id = "hidden-if-disabled-blacklist"

						div(classes = "flavourText") {
							+ locale["${LOCALE_PREFIX}.messageWhenUsingACommandInABlockedChannel.title"]
						}

						textArea {
							id = "blacklisted-warning"
							+ (guild.general.blacklistedWarning ?: "{@user} Você não pode usar comandos no {@channel}, bobinho(a)")
						}
					}

					hr {}

					button(classes = "button-discord button-discord-success pure-button") {
						style = "float: right;"

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
					EmbedEditorStuff.userInContextPlaceholders(locale),
					// customTokens = mapOf(),
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

	fun updateBlacklistedChannelsList(guild: PartialGuildConfiguration) {
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
		SaveUtils.prepareSave("general", extras = {
			it["commandPrefix"] = document.select<HTMLInputElement>("#command-prefix").value
			it["deleteMessageAfterCommand"] = document.select<HTMLInputElement>("#delete-message-after-command").checked
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