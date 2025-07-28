@file:JsExport
package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import jq
import kotlinx.browser.document
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import legacyLocale
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.utils.levelup.LevelUpAnnouncementType
import net.perfectdreams.spicymorenitta.utils.locale.buildAsHtml
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import net.perfectdreams.spicymorenitta.views.dashboard.Stuff
import net.perfectdreams.spicymorenitta.views.dashboard.getPlan
import org.w3c.dom.*
import stripHtmlTagsUsingDom
import kotlin.js.Json
import kotlin.js.json

class LevelUpRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/level") {
	companion object {
		private const val LOCALE_PREFIX = "modules.levelUp"
	}

	@Serializable
	class PartialGuildConfiguration(
			val activeDonationKeys: List<ServerConfig.DonationKey>,
			val textChannels: List<ServerConfig.TextChannel>,
			val roles: List<ServerConfig.Role>,
			val levelUpConfig: ServerConfig.LevelUpConfig
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
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "activekeys", "textchannels", "roles", "level")
			switchContentAndFixLeftSidebarScroll(call)

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}
			
			val stuff = document.select<HTMLDivElement>("#level-stuff")

			stuff.append {
				generateLevelUpAnnouncementSection(guild)

				hr {}

				div {
					h5(classes = "section-title") {
						+ locale["$LOCALE_PREFIX.roleGiveType.title"]
					}

					createRadioButton(
							"role-level-up-style",
							locale["$LOCALE_PREFIX.roleGiveType.types.stack.title"],
							locale["$LOCALE_PREFIX.roleGiveType.types.stack.description"],
							"STACK",
							guild.levelUpConfig.roleGiveType == "STACK"
					)

					createRadioButton(
							"role-level-up-style",
							locale["$LOCALE_PREFIX.roleGiveType.types.remove.title"],
							locale["$LOCALE_PREFIX.roleGiveType.types.remove.description"],
							"REMOVE",
							guild.levelUpConfig.roleGiveType == "REMOVE"
					)
				}

				hr {}

				div {
					h5(classes = "section-title") {
						+ locale["$LOCALE_PREFIX.roleByXpLevel.title"]
					}

					locale.getList("$LOCALE_PREFIX.roleByXpLevel.description").forEach {
						p {
							+ it
						}
					}

					div(classes = "add-role") {
						locale.buildAsHtml(locale["$LOCALE_PREFIX.roleByXpLevel.whenUserGetsToXp"], { num ->
							if (num == 0) {
								input(InputType.number, classes = "required-xp") {
									placeholder = "1000"
									min = "0"
									max = "10000000"
									step = "1000"

									onChangeFunction = {
										document.select<HTMLSpanElement>("#give-role-level-calc")
												.innerText = ((document.select<HTMLInputElement>(".add-role .required-xp")
												.valueOrPlaceholderIfEmpty("1000").toLong() / 1000).toString())
									}
								}
							}

							if (num == 1) {
								span {
									id = "give-role-level-calc"
									+ "0"
								}
							}

							if (num == 2) {
								select {
									id = "choose-role"
									style = "width: 320px;"
								}
							}
						}) { str ->
							+ str
						}

						+ " "
						button(classes = "button-discord button-discord-info pure-button") {
							+ locale["loritta.add"]

							onClickFunction = {
								val plan = guild.activeDonationKeys.getPlan()
								if (rolesByExperience.size >= plan.maxLevelUpRoles) {
									Stuff.showPremiumFeatureModal {
										h2 {
											+ "Adicione mais cargos para Level Up!"
										}
										p {
											+ "Faça upgrade para poder adicionar mais cargos!"
										}
									}
								} else {
									addRoleToRoleByExperienceList(
											guild,
											ServerConfig.RoleByExperience(
													document.select<HTMLInputElement>(".add-role .required-xp")
															.valueOrPlaceholderIfEmpty("1000"),
													listOf(
															document.select<HTMLSelectElement>("#choose-role").value.toLong()
													)
											)
									)
								}
							}
						}
					}

					div(classes = "roles-by-xp-list list-wrapper") {}
				}

				hr {}

				div {
					h5(classes = "section-title") {
						+ locale["$LOCALE_PREFIX.customRoleRate.title"]
					}

					locale.getList("$LOCALE_PREFIX.customRoleRate.description").forEach {
						p {
							+ it
						}
					}

					div(classes = "add-custom-rate-role") {
						locale.buildAsHtml(locale["$LOCALE_PREFIX.customRoleRate.whenUserHasRoleRate"], { num ->
							if (num == 0) {
								select {
									id = "choose-role-custom-rate"
									style = "width: 320px;"
								}
							}

							if (num == 1) {
								input(InputType.number, classes = "xp-rate") {
									placeholder = "1.0"
									min = "0"
									max = "10"
									step = "0.05"
								}
							}
						}) { str ->
							+ str
						}

						+ " "
						button(classes = "button-discord button-discord-info pure-button") {
							+ locale["loritta.add"]

							onClickFunction = {
								addRoleToRolesWithCustomRateList(
										guild,
										ServerConfig.ExperienceRoleRate(
												document.select<HTMLSelectElement>("#choose-role-custom-rate").value.toLong(),
												document.select<HTMLInputElement>(".add-custom-rate-role .xp-rate")
														.valueOrPlaceholderIfEmpty("1.0").toDouble()
										)
								)
							}
						}
					}

					div(classes = "roles-with-custom-rate-list list-wrapper") {}
				}

				hr {}

				div {
					h5(classes = "section-title") {
						+ locale["$LOCALE_PREFIX.noXpRoles.title"]
					}

					locale.getList("$LOCALE_PREFIX.noXpRoles.description").forEach {
						p {
							+ it
						}
					}

					select {
						id = "choose-role-no-xp"
						style = "width: 320px;"
					}

					button(classes = "button-discord button-discord-info pure-button") {
						+ locale["loritta.add"]

						onClickFunction = {
							val role = document.select<HTMLSelectElement>("#choose-role-no-xp").value

							noXpRoles.add(role.toLong())

							updateNoXpRoleList(guild)
						}
					}

					div(classes = "list-wrapper") {
						id = "choose-role-no-xp-list"
					}
				}

				hr {}

				div {
					h5(classes = "section-title") {
						+ locale["$LOCALE_PREFIX.noXpChannels.title"]
					}

					locale.getList("$LOCALE_PREFIX.noXpChannels.description").forEach {
						p {
							+ it
						}
					}

					select {
						id = "choose-channel-no-xp"
						style = "width: 320px;"

						for (channel in guild.textChannels) {
							option {
								+ ("#${channel.name}")

								value = channel.id.toString()
							}
						}
					}

					button(classes = "button-discord button-discord-info pure-button") {
						+ locale["loritta.add"]

						onClickFunction = {
							val role = document.select<HTMLSelectElement>("#choose-channel-no-xp").value

							noXpChannels.add(role.toLong())

							updateNoXpChannelsList(guild)
						}
					}

					div(classes = "list-wrapper") {
						id = "choose-channel-no-xp-list"
					}

					hr {}

					button(classes = "button-discord button-discord-attention pure-button") {
						i(classes = "fas fa-redo") {}

						+ " ${locale["$LOCALE_PREFIX.resetXp.title"]}"

						onClickFunction = {
							val modal = TingleModal(
								jsObject<TingleOptions> {
									footer = true
									cssClass = arrayOf("tingle-modal--overflow")
									closeMethods = arrayOf()
								}
							)

							modal.addFooterBtn("<i class=\"fas fa-redo\"></i> ${locale["$LOCALE_PREFIX.resetXp.clearAll"]}", "button-discord button-discord-attention pure-button button-discord-modal") {
								modal.close()

								SaveUtils.prepareSave("reset_xp", {})
							}

							modal.addFooterBtn("<i class=\"fas fa-times\"></i> ${locale["$LOCALE_PREFIX.resetXp.cancel"]}", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
								modal.close()
							}

							modal.setContent(
									createHTML().div {
										div(classes = "category-name") {
											+ locale["$LOCALE_PREFIX.resetXp.areYouSure"]
										}

										div {
											style = "text-align: center;"

											img(src = "https://stuff.loritta.website/loritta-stop-heathecliff.png") {
												width = "250"
											}

											locale.getList("$LOCALE_PREFIX.resetXp.description").forEach {
												p {
													+ it
												}
											}
										}
									}
							)
							modal.open()
						}
					}
				}
			}

			val optionData = mutableListOf<dynamic>()
			val chooseRoleToAddOptionData = mutableListOf<dynamic>()
			
			fun generateOptionForRole(role: ServerConfig.Role): dynamic {
				val option = object {}.asDynamic()
				option.id = role.id
				var text = "<span style=\"font-weight: 600;\">${stripHtmlTagsUsingDom(role.name)}</span>"

				val color = role.getColor()

				if (color != null) {
					text = "<span style=\"font-weight: 600; color: rgb(${color.red}, ${color.green}, ${color.blue})\">${stripHtmlTagsUsingDom(role.name)}</span>"
				}

				option.text = text

				if (!role.canInteract || role.isManaged) {
					if (role.isManaged) {
						option.text = "${text} <span class=\"keyword\" style=\"background-color: rgb(225, 149, 23);\">${legacyLocale["DASHBOARD_RoleByIntegration"]}</span>"
					} else {
						option.text = "${text} <span class=\"keyword\" style=\"background-color: rgb(231, 76, 60);\">${legacyLocale["DASHBOARD_NoPermission"]}</span>"
					}
				}
				
				return option
			}

			for (it in guild.roles.filter { !it.isPublicRole }) {
				chooseRoleToAddOptionData.add(generateOptionForRole(it))
			}
			
			for (it in guild.roles) {
				optionData.add(generateOptionForRole(it))
			}

			val options = object {}.asDynamic()

			options.data = optionData.toTypedArray()
			options.escapeMarkup = { str: dynamic ->
				str
			}

			val optionsRoleToAdd = object {}.asDynamic()

			optionsRoleToAdd.data = chooseRoleToAddOptionData.toTypedArray()
			optionsRoleToAdd.escapeMarkup = { str: dynamic ->
				str
			}

			jq("#choose-role-no-xp").asDynamic().select2(
					options
			)

			jq("#choose-role").asDynamic().select2(
					optionsRoleToAdd
			)

			jq("#choose-role-custom-rate").asDynamic().select2(
					options
			)

			guild.levelUpConfig.rolesByExperience.forEach {
				addRoleToRoleByExperienceList(guild, it)
			}

			guild.levelUpConfig.experienceRoleRates.forEach {
				addRoleToRolesWithCustomRateList(guild, it)
			}

			noXpRoles.addAll(guild.levelUpConfig.noXpRoles)
			noXpChannels.addAll(guild.levelUpConfig.noXpChannels)
			updateNoXpRoleList(guild)
			updateNoXpChannelsList(guild)

			LoriDashboard.configureTextArea(
					jq("#announcement-message"),
					true,
					false,
					null,
					true,
					EmbedEditorStuff.userInContextPlaceholders(locale) +
							EmbedEditorStuff.userCurrentExperienceInContextPlaceholders(locale),
					/* Placeholders.DEFAULT_PLACEHOLDERS *//* .toMutableMap().apply {
						put("previous-level", "Qual era o nível do usuário antes dele ter passado de nível")
						put("previous-xp", "Quanta experiência o usuário tinha antes dele ter passado de nível")
						put("level", "O novo nível que o usuário está")
						put("xp", "A nova quantidade de experiência que o usuário tem")
					},
					customTokens = mapOf(
							"previous-level" to "99",
							"previous-xp" to "99987",
							"level" to "100",
							"xp" to "100002"
					), */
					showTemplates = true,
					templates = mapOf(
							locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.default.title"] to locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.default.content", "<a:lori_yay_wobbly:638040459721310238>"],
							locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.embed.title"] to """{
  "content":"{@user}",
    "embed":{
    "color":-12591736,
    "title":" **<a:lori_yay_wobbly:638040459721310238> | LEVEL UP!**",
    "description":" ${locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.default.content", "<:lori_heart:640158506049077280>"]}",
    "footer": { "text": "${locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.embed.footer"]}" }
  }
}"""
					)
			)

			updateDisabledSections()
		}
	}

	fun updateDisabledSections() {
		val announcementTypeSelect = document.select<HTMLSelectElement>("#announcement-type")

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
		}
	}

	fun addRoleToRolesWithCustomRateList(guild: PartialGuildConfiguration, experienceRoleRate: ServerConfig.ExperienceRoleRate) {
		experienceRoleRates.add(experienceRoleRate)

		updateRolesWithCustomRateList(guild)
	}

	fun updateRolesWithCustomRateList(guild: PartialGuildConfiguration) {
		val list = document.select<HTMLDivElement>(".roles-with-custom-rate-list")

		list.clear()

		val invalidEntries = mutableListOf<ServerConfig.ExperienceRoleRate>()

		for (experienceRoleRate in experienceRoleRates.sortedByDescending { it.rate.toLong() }) {
			val theRealRoleId = experienceRoleRate.role

			val guildRole = guild.roles.firstOrNull { it.id == theRealRoleId }

			if (guildRole == null) {
				debug("Role ${theRealRoleId} not found! Removing $experienceRoleRate")
				invalidEntries.add(experienceRoleRate)
				continue
			}

			val color = guildRole.getColor()

			list.append {
				div {
					button(classes = "button-discord button-discord-info pure-button remove-action") {
						i(classes = "fas fa-trash") {}

						onClickFunction = {
							experienceRoleRates.remove(experienceRoleRate)

							updateRolesWithCustomRateList(guild)
						}
					}

					+ " "

					locale.buildAsHtml(locale["$LOCALE_PREFIX.customRoleRate.whenUserHasRoleRate"], { num ->
						if (num == 0) {
							span(classes = "discord-mention") {
								if (color != null)
									style = "color: rgb(${color.red}, ${color.green}, ${color.blue}); background-color: rgba(${color.red}, ${color.green}, ${color.blue}, 0.298039);"
								+ "@${guildRole.name}"
							}
						}

						if (num == 1) {
							strong {
								+ experienceRoleRate.rate.toString()
							}
						}
					}) { str ->
						+ str
					}
				}
			}
		}

		experienceRoleRates.removeAll(invalidEntries)
	}

	fun addRoleToRoleByExperienceList(guild: PartialGuildConfiguration, roleByExperience: ServerConfig.RoleByExperience) {
		rolesByExperience.add(roleByExperience)

		updateRoleByExperienceList(guild)
	}

	fun updateRoleByExperienceList(guild: PartialGuildConfiguration) {
		val list = document.select<HTMLDivElement>(".roles-by-xp-list")

		list.clear()

		val invalidEntries = mutableListOf<ServerConfig.RoleByExperience>()

		for (roleByExperience in rolesByExperience.sortedByDescending { it.requiredExperience.toLong() }) {
			val theRealRoleId = roleByExperience.roles.firstOrNull() ?: continue

			val guildRole = guild.roles.firstOrNull { it.id == theRealRoleId }

			if (guildRole == null) {
				debug("Role ${theRealRoleId} not found! Removing $roleByExperience")
				invalidEntries.add(roleByExperience)
				continue
			}

			val color = guildRole.getColor()

			list.append {
				div {
					button(classes = "button-discord button-discord-info pure-button remove-action") {
						i(classes = "fas fa-trash") {}

						onClickFunction = {
							rolesByExperience.remove(roleByExperience)

							updateRoleByExperienceList(guild)
						}
					}

					+ " "

					locale.buildAsHtml(locale["$LOCALE_PREFIX.roleByXpLevel.whenUserGetsToXp"], { num ->
						if (num == 0) {
							strong {
								+ (roleByExperience.requiredExperience)
							}
						}

						if (num == 1) {
							span {
								+ (roleByExperience.requiredExperience.toLong() / 1000).toString()
							}
						}

						if (num == 2) {
							span(classes = "discord-mention") {
								if (color != null)
									style = "color: rgb(${color.red}, ${color.green}, ${color.blue}); background-color: rgba(${color.red}, ${color.green}, ${color.blue}, 0.298039);"
								+ "@${guildRole.name}"
							}
						}
					}) { str ->
						+ str
					}
				}
			}
		}

		rolesByExperience.removeAll(invalidEntries)
	}

	fun updateNoXpRoleList(guild: PartialGuildConfiguration) {
		val list = document.select<HTMLDivElement>("#choose-role-no-xp-list")

		list.clear()

		list.append {
			noXpRoles.forEach { noXpRoleId ->
				val guildRole = guild.roles.firstOrNull { it.id.toLong() == noXpRoleId } ?: return@forEach

				list.append {
					val color = guildRole.getColor()

					span(classes = "discord-mention") {
						style = "cursor: pointer; margin-right: 4px;"

						if (color != null)
							style = "cursor: pointer; margin-right: 4px; color: rgb(${color.red}, ${color.green}, ${color.blue}); background-color: rgba(${color.red}, ${color.green}, ${color.blue}, 0.298039);"
						else
							style = "cursor: pointer; margin-right: 4px;"
						+"@${guildRole.name}"

						onClickFunction = {
							noXpRoles.remove(noXpRoleId)

							updateNoXpRoleList(guild)
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

	fun updateNoXpChannelsList(guild: PartialGuildConfiguration) {
		val list = document.select<HTMLDivElement>("#choose-channel-no-xp-list")

		list.clear()

		list.append {
			noXpChannels.forEach { noXpChannelId ->
				val guildChannel = guild.textChannels.firstOrNull { it.id.toLong() == noXpChannelId } ?: return@forEach

				list.append {
					span(classes = "discord-mention") {
						style = "cursor: pointer; margin-right: 4px;"

						+ "#${guildChannel.name}"

						onClickFunction = {
							noXpChannels.remove(noXpChannelId)

							updateNoXpChannelsList(guild)
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

	@JsName("prepareSave")
	fun prepareSave() {
		SaveUtils.prepareSave("level", extras = {
			it["roleGiveType"] = document.select<HTMLInputElement>("input[name='role-level-up-style']:checked").value
			it["noXpChannels"] = noXpChannels.map { it.toString() }
			it["noXpRoles"] = noXpRoles.map { it.toString() }

			val announcements = mutableListOf<Json>()

			val announcementType = document.select<HTMLInputElement>("#announcement-type").value
			val announcementMessage = document.select<HTMLInputElement>("#announcement-message").value

			if (announcementType != "DISABLED") {
				val json = json(
						"type" to announcementType,
						"message" to announcementMessage,
						"onlyIfUserReceivedRoles" to document.select<HTMLInputElement>("#only-if-user-received-roles").checked
				)

				if (announcementType == "DIFFERENT_CHANNEL") {
					json["channelId"] = document.select<HTMLSelectElement>("#choose-channel-custom-channel").value
				}

				announcements.add(json)
			}

			it["announcements"] = announcements

			val rolesByExperience = mutableListOf<Json>()
			this.rolesByExperience.forEach {
				val inner = json(
						"requiredExperience" to it.requiredExperience,
						"roles" to it.roles.map { it.toString() }
				)

				rolesByExperience.add(inner)
			}

			it["rolesByExperience"] = rolesByExperience

			val experienceRoleRates = mutableListOf<Json>()
			this.experienceRoleRates.forEach {
				val inner = json(
						"rate" to it.rate.toString(),
						"role" to it.role.toString()
				)

				experienceRoleRates.add(inner)
			}

			it["experienceRoleRates"] = experienceRoleRates
		})
	}

	private fun TagConsumer<HTMLElement>.generateLevelUpAnnouncementSection(guild: PartialGuildConfiguration) {
		val announcement = guild.levelUpConfig.announcements.firstOrNull()

		div {
			h5(classes = "section-title") {
				+locale["$LOCALE_PREFIX.levelUpAnnouncement.title"]
			}

			locale.getList("$LOCALE_PREFIX.levelUpAnnouncement.description").forEach {
				p {
					+it
				}
			}

			div {
				style = "display: flex; flex-direction: column;"

				div {
					style = "display: flex; flex-direction: row;"

					div {
						style = "flex-grow: 1; margin-right: 10px;"
						h5(classes = "section-title") {
							+locale["$LOCALE_PREFIX.levelUpAnnouncement.tellWhere"]
						}

						select {
							style = "width: 100%;"
							id = "announcement-type"

							for (entry in LevelUpAnnouncementType.values()) {
								option {
									+when (entry) {
										LevelUpAnnouncementType.DISABLED -> locale["$LOCALE_PREFIX.channelTypes.disabled"]
										LevelUpAnnouncementType.SAME_CHANNEL -> locale["$LOCALE_PREFIX.channelTypes.sameChannel"]
										LevelUpAnnouncementType.DIRECT_MESSAGE -> locale["$LOCALE_PREFIX.channelTypes.directMessage"]
										LevelUpAnnouncementType.DIFFERENT_CHANNEL -> locale["$LOCALE_PREFIX.channelTypes.differentChannel"]
									}

									value = entry.name

									if (entry.name == announcement?.type) {
										selected = true
									}
								}
							}

							onChangeFunction = {
								updateDisabledSections()
							}
						}
					}

					div(classes = "blurSection") {
						style = "flex-grow: 1; margin-left: 16px;"

						id = "select-custom-channel"

						h5(classes = "section-title") {
							+locale["$LOCALE_PREFIX.levelUpAnnouncement.channel"]
						}

						select {
							style = "width: 100%;"
							id = "choose-channel-custom-channel"
							// style = "width: 320px;"

							for (channel in guild.textChannels) {
								option {
									+("#${channel.name}")

									value = channel.id.toString()

									if (channel.id == announcement?.channelId) {
										selected = true
									}
								}
							}
						}
					}
				}

				div {
					createToggle(
							locale["modules.levelUp.levelUpAnnouncement.onlyIfUserReceivedRoles.title"],
							locale["modules.levelUp.levelUpAnnouncement.onlyIfUserReceivedRoles.subtext"],
							id = "only-if-user-received-roles",
							isChecked = announcement?.onlyIfUserReceivedRoles ?: false
					)
				}

				div(classes = "blurSection") {
					id = "level-up-message"

					style = "flex-grow: 1;"

					h5(classes = "section-title") {
						+locale["$LOCALE_PREFIX.levelUpAnnouncement.theMessage"]
					}

					textArea {
						id = "announcement-message"
						+(announcement?.message
								?: locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.default.content", "<a:lori_yay_wobbly:638040459721310238>"])
					}
				}
			}
		}
	}

	private fun HTMLInputElement.valueOrPlaceholderIfEmpty(newValue: String): String {
		val value = this.value
		if (value.isEmpty())
			return newValue
		return value
	}
}