package net.perfectdreams.spicymorenitta.routes

import LoriDashboard
import jq
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.stream.createHTML
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import legacyLocale
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.locale
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.levelup.LevelUpAnnouncementType
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import net.perfectdreams.spicymorenitta.views.dashboard.Stuff
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.dom.addClass
import kotlin.dom.clear
import kotlin.dom.removeClass
import kotlin.js.Json
import kotlin.js.json

class LevelUpRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/level") {
	companion object {
		private const val LOCALE_PREFIX = "modules.levelUp"
	}

	val rolesByExperience = mutableListOf<ServerConfig.RoleByExperience>()
	val experienceRoleRates = mutableListOf<ServerConfig.ExperienceRoleRate>()
	val noXpRoles = mutableListOf<Long>()
	val noXpChannels = mutableListOf<Long>()

	@ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		rolesByExperience.clear()
		noXpRoles.clear()
		noXpChannels.clear()
		experienceRoleRates.clear()

		m.fixLeftSidebarScroll {
			super.onRender(call)
		}

		document.select<HTMLButtonElement>("#save-button").onClick {
			prepareSave()
		}

		val optionData = mutableListOf<dynamic>()

		val premiumAsJson = document.getElementById("badge-json")?.innerHTML!!

		val guild = JSON.nonstrict.parse<ServerConfig.Guild>(premiumAsJson)

		val stuff = document.select<HTMLDivElement>("#level-stuff")

		stuff.append {
			val announcement = guild.levelUpConfig.announcements.firstOrNull()

			div {
				h5(classes = "section-title") {
					+ locale["$LOCALE_PREFIX.levelUpAnnouncement.title"]
				}

				locale.getList("$LOCALE_PREFIX.levelUpAnnouncement.description").forEach {
					p {
						+ it
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
										+ when (entry) {
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
								id = "choose-channel-no-xp"
								// style = "width: 320px;"

								for (channel in guild.textChannels) {
									option {
										+ ("#${channel.name}")

										value = channel.id

										if (channel.id == announcement?.channelId) {
											selected = true
										}
									}
								}
							}
						}
					}

					div(classes = "blurSection") {
						id = "level-up-message"

						style = "flex-grow: 1;"

						h5(classes = "section-title") {
							+ locale["$LOCALE_PREFIX.levelUpAnnouncement.theMessage"]
						}

						textArea {
							id = "announcement-message"
							+(announcement?.message ?: locale["$LOCALE_PREFIX.levelUpAnnouncement.defaultMessage", "<a:lori_yay_wobbly:638040459721310238>"])
						}
					}
				}
			}

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
											.value.toLong() / 1000).toString())
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
						+ "Adicionar"

						onClickFunction = {
							if (rolesByExperience.size >= 15) {
								Stuff.showPremiumFeatureModal()
							} else {
								addRoleToRoleByExperienceList(
										guild,
										ServerConfig.RoleByExperience(
												document.select<HTMLInputElement>(".add-role .required-xp")
														.value,
												listOf(
														document.select<HTMLSelectElement>("#choose-role").value
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
						+ "Adicionar"

						onClickFunction = {
							addRoleToRolesWithCustomRateList(
									guild,
									ServerConfig.ExperienceRoleRate(
											document.select<HTMLSelectElement>("#choose-role-custom-rate").value.toLong(),
											document.select<HTMLInputElement>(".add-custom-rate-role .xp-rate")
													.value.toDouble()
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
					+ "Adicionar"

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

							value = channel.id
						}
					}
				}

				button(classes = "button-discord button-discord-info pure-button") {
					+"Adicionar"

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
								TingleOptions(
										footer = true,
										cssClass = arrayOf("tingle-modal--overflow")
								)
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

										img(src = "https://loritta.website/assets/img/fanarts/l6.png") {
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

		for (it in guild.roles/* .filter { !it.isPublicRole } */) {
			val option = object {}.asDynamic()
			option.id = it.id
			var text = "<span style=\"font-weight: 600;\">${it.name}</span>"

			val color = it.getColor()

			if (color != null) {
				text = "<span style=\"font-weight: 600; color: rgb(${color.red}, ${color.green}, ${color.blue})\">${it.name}</span>"
			}

			option.text = text

			/* if (serverConfig.autoroleConfig.roles.contains(it.id))
			continue */

			if (!it.canInteract || it.isManaged) {
				if (it.isManaged) {
					option.text = "${text} <span class=\"keyword\" style=\"background-color: rgb(225, 149, 23);\">${legacyLocale["DASHBOARD_RoleByIntegration"]}</span>"
				} else {
					option.text = "${text} <span class=\"keyword\" style=\"background-color: rgb(231, 76, 60);\">${legacyLocale["DASHBOARD_NoPermission"]}</span>"
				}
			}

			optionData.add(option)
		}

		val options = object {}.asDynamic()

		options.data = optionData.toTypedArray()
		options.escapeMarkup = { str: dynamic ->
			str
		}

		jq("#choose-role-no-xp").asDynamic().select2(
				options
		)

		jq("#choose-role").asDynamic().select2(
				options
		)

		jq("#choose-role-custom-rate").asDynamic().select2(
				options
		)

		val roleList = guild.roles

		roleList.forEach {
			addRoleToAutoroleList(it)
		}

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
				null,
				false,
				null,
				true,
				Placeholders.DEFAULT_PLACEHOLDERS.toMutableMap().apply {
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
				),
				showTemplates = true,
				templates = mapOf(
						locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.default.title"] to locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.default.content"],
						locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.embed.title"] to """{
  "content":"{@user}",
    "embed":{
    "color":-12591736,
    "title":" **<a:lori_yay_wobbly:638040459721310238> | LEVEL UP!**",
    "description":" **${locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.default.content", "<:lori_heart:640158506049077280>"]}",
    "footer": ${locale["$LOCALE_PREFIX.levelUpAnnouncement.templates.embed.footer"]}
  }
}"""
				)
		)

		updateDisabledSections()
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

	fun addRoleToRolesWithCustomRateList(guild: ServerConfig.Guild, experienceRoleRate: ServerConfig.ExperienceRoleRate) {
		experienceRoleRates.add(experienceRoleRate)

		updateRolesWithCustomRateList(guild)
	}

	fun updateRolesWithCustomRateList(guild: ServerConfig.Guild) {
		val list = document.select<HTMLDivElement>(".roles-with-custom-rate-list")

		list.clear()

		for (experienceRoleRate in experienceRoleRates.sortedByDescending { it.rate.toLong() }) {
			val theRealRoleId = experienceRoleRate.role

			val guildRole = guild.roles.firstOrNull { it.id == theRealRoleId.toString() } ?: continue

			val color = guildRole.getColor()

			list.append {
				div {
					button(classes = "button-discord button-discord-info pure-button remove-action") {
						i(classes = "fas fa-trash") {}

						onClickFunction = {
							experienceRoleRates.remove(experienceRoleRate)

							updateRoleByExperienceList(guild)
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
	}

	fun addRoleToRoleByExperienceList(guild: ServerConfig.Guild, roleByExperience: ServerConfig.RoleByExperience) {
		rolesByExperience.add(roleByExperience)

		updateRoleByExperienceList(guild)
	}

	fun updateRoleByExperienceList(guild: ServerConfig.Guild) {
		val list = document.select<HTMLDivElement>(".roles-by-xp-list")

		list.clear()

		for (roleByExperience in rolesByExperience.sortedByDescending { it.requiredExperience.toLong() }) {
			val theRealRoleId = roleByExperience.roles.firstOrNull() ?: continue

			val guildRole = guild.roles.firstOrNull { it.id == theRealRoleId } ?: continue

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
	}

	fun updateNoXpRoleList(guild: ServerConfig.Guild) {
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

	fun updateNoXpChannelsList(guild: ServerConfig.Guild) {
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

	fun addRoleToAutoroleList(role: ServerConfig.Role) {
		// <span style="color: rgb(155, 89, 182); background-color: rgba(155, 89, 182, 0.298039);  font-family: Whitney, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-weight: 600;">@💵🌆 Pagadores do Aluguel</span>
		val td = jq("<td>")
				.attr("role-id", role.id)
				.addClass("role-entry")

		val roleSpan = jq("<span>")
				.text("@" + role.name)
				.addClass("discord-mention")

		val color = role.getColor()

		if (color != null) {
			roleSpan.css("color", "rgb(${color.red}, ${color.green}, ${color.blue})")
			roleSpan.css("background-color", "rgba(${color.red}, ${color.green}, ${color.blue}, 0.298039)")
		}

		td.append(roleSpan)

		val tr = jq("<tr>")
				.append(td)

		tr.click {
			tr.remove()
		}

		jq("#roleTable").append(
				tr
		)
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
						"message" to announcementMessage
				)

				if (announcementType == "DIFFERENT_CHANNEL") {
					json["channelId"] = document.select<HTMLSelectElement>("#choose-channel-no-xp").value
				}

				announcements.add(json)
			}

			it["announcements"] = announcements

			val rolesByExperience = mutableListOf<Json>()
			this.rolesByExperience.forEach {
				val inner = json(
						"requiredExperience" to it.requiredExperience,
						"roles" to it.roles
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
}