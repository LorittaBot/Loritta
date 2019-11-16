package net.perfectdreams.spicymorenitta.routes

import LoriDashboard
import jq
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.parse
import legacyLocale
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.utils.Placeholders
import net.perfectdreams.spicymorenitta.utils.SaveUtils
import net.perfectdreams.spicymorenitta.utils.levelup.LevelUpAnnouncementType
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document
import kotlin.dom.clear
import kotlin.js.Json
import kotlin.js.json

class LevelUpRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/level") {
	val rolesByExperience = mutableListOf<ServerConfig.RoleByExperience>()
	val noXpRoles = mutableListOf<Long>()
	val noXpChannels = mutableListOf<Long>()

    @ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		m.fixLeftSidebarScroll {
			super.onRender(call)
		}

		val optionData = mutableListOf<dynamic>()

		val premiumAsJson = document.getElementById("badge-json")?.innerHTML!!

		val guild = JSON.nonstrict.parse<ServerConfig.Guild>(premiumAsJson)

		val stuff = document.select<HTMLDivElement>("#level-stuff")

		stuff.append {
			val announcement = guild.levelUpConfig.announcements.firstOrNull()

			div {
				h5(classes = "section-title") {
					+ "Mensagens ao Subir de Nível"
				}

				div {
					style = "display: flex; flex-direction: row;"

					div {
						h5(classes = "section-title") {
							+ "Avisar aonde?"
						}

						select {
							id = "announcement-type"

							for (entry in LevelUpAnnouncementType.values()) {
								option {
									+ entry.name

									if (entry.name == announcement?.type) {
										selected = true
									}
								}
							}
						}
					}

					div {
						style = "flex-grow: 1;"

						h5(classes = "section-title") {
							+ "Qual é a mensagem?"
						}

						textArea {
							id = "announcement-message"
							+(announcement?.message ?: "idk lol")
						}
					}
				}
			}

			hr {}

			div {
				h5(classes = "section-title") {
					+ "Cargos por nível de XP"
				}

				div(classes = "add-role") {
					+ "Ao chegar em "
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
					+ " XP (Nível "
					span {
						id = "give-role-level-calc"
						+ "0"
					}

					+ "), dar o cargo "

					select {
						id = "choose-role"
						style = "width: 320px;"
					}

					button(classes = "button-discord button-discord-info pure-button") {
						+ "Adicionar"

						onClickFunction = {
							console.log(document.select<HTMLSelectElement>("#choose-role"))
							console.log(document.select<HTMLInputElement>(".add-role .required-xp").value)
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

				div(classes = "roles-by-xp-list") {}
			}

			hr {}

			div {
				h5(classes = "section-title") {
					+ "Cargos que não irão receber experiência"
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

				div {
					id = "choose-role-no-xp-list"
				}
			}

			hr {}

			div {
				h5(classes = "section-title") {
					+ "Canais que não irão dar experiência"
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

				div {
					id = "choose-channel-no-xp-list"
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

		val roleList = guild.roles

		roleList.forEach {
			addRoleToAutoroleList(it)
		}

		guild.levelUpConfig.rolesByExperience.forEach {
			addRoleToRoleByExperienceList(guild, it)
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
				}
		)
    }


	fun addRoleToRoleByExperienceList(guild: ServerConfig.Guild, roleByExperience: ServerConfig.RoleByExperience) {
		val theRealRoleId = roleByExperience.roles.firstOrNull() ?: return

		val guildRole = guild.roles.firstOrNull { it.id == theRealRoleId } ?: return

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

					+ " Ao chegar em ${roleByExperience.requiredExperience} XP, dar o cargo "
					span(classes = "discord-mention") {
						if (color != null)
							style = "color: rgb(${color.red}, ${color.green}, ${color.blue}); background-color: rgba(${color.red}, ${color.green}, ${color.blue}, 0.298039);"
						+ "@${guildRole.name}"
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
			it["noXpChannels"] = noXpChannels.map { it.toString() }
			it["noXpRoles"] = noXpRoles.map { it.toString() }

			val announcements = mutableListOf<Json>()

			val announcementType = document.select<HTMLInputElement>("#announcement-type").value
			val announcementMessage = document.select<HTMLInputElement>("#announcement-message").value

			if (announcementType != "DISABLED") {
				announcements.add(
						json(
								"type" to announcementType,
								"message" to announcementMessage
						)
				)
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
		})
	}
}