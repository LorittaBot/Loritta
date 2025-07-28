@file:JsExport
package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import SaveStuff
import jQuery
import jq
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.Serializable
import legacyLocale
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.*
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import stripHtmlTagsUsingDom

class AutoroleConfigRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/autorole") {
	override val keepLoadingScreen: Boolean
		get() = true

	@Serializable
	class PartialGuildConfiguration(
			val roles: List<ServerConfig.Role>,
			val autoroleConfig: ServerConfig.AutoroleConfig
	)

	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "roles", "autorole")
			switchContentAndFixLeftSidebarScroll(call)

			val optionData = mutableListOf<dynamic>()

			for (it in guild.roles.filter { !it.isPublicRole }) {
				val option = object {}.asDynamic()
				option.id = it.id.toString()
				var text = "<span style=\"font-weight: 600;\">${stripHtmlTagsUsingDom(it.name)}</span>"

				val color = it.getColor()
				if (color != null) {
					text = "<span style=\"font-weight: 600; color: rgb(${color.red}, ${color.green}, ${color.blue})\">${stripHtmlTagsUsingDom(it.name)}</span>"
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

			jq("#chooseRole").asDynamic().select2(
					options
			)

			val roleList = guild.autoroleConfig.roles.mapNotNull { roleId -> guild.roles.firstOrNull { it.id == roleId } }

			roleList.forEach {
				addRoleToAutoroleList(it)
			}

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-1")

			document.select<HTMLButtonElement>("#add-role-button").onClick {
				addRoleFromSelection(guild)
			}

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}

			/* if (!serverConfig.permissions.contains("ADMINISTRATOR") && !serverConfig.permissions.contains("MANAGE_ROLES")) {
				LoriDashboard.enableBlur("#autoroleConfigurationWrapper")
				jq("#requiresPermission").html(legacyLocale["DASHBOARD_HeyINeedPermission", "<b>${legacyLocale["PERMISSION_MANAGE_ROLES"]}</b>"])
			} */
		}
	}


	fun addRoleToAutoroleList(role: ServerConfig.Role) {
		// <span style="color: rgb(155, 89, 182); background-color: rgba(155, 89, 182, 0.298039);  font-family: Whitney, 'Helvetica Neue', Helvetica, Arial, sans-serif; font-weight: 600;">@💵🌆 Pagadores do Aluguel</span>
		val td = jq("<td>")
				.attr("role-id", role.id.toString())
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

	@JsName("addRoleFromSelection")
	fun addRoleFromSelection(serverConfig: PartialGuildConfiguration) {
		val roleId = jq("#chooseRole option:selected").`val`() as String

		println("Adding role ${roleId} to the selection...")
		val role = serverConfig.roles.firstOrNull { it.id == roleId.toLong() }

		if (role != null) {
			if (role.isManaged || !role.canInteract) {
				val modal = TingleModal(
					jsObject<TingleOptions> {
						footer = true
						cssClass = arrayOf("tingle-modal--overflow")
						closeMethods = arrayOf()
					}
				)

				if (role.isManaged) {
					modal.setContent(
							jq("<div>").append(
									jq("<div>")
											.addClass("category-name")
											.text(legacyLocale["DASHBOARD_RoleByIntegration"])
							).append(jq("<div>").css("text-align", "center").append(
									jq("<p>")
											.text("Cargos criados por integrações (por exemplo: ao adicionar um bot) não podem ser utilizados para dar cargos para outros membros!")
							))
									.html()
					)
				} else {
					modal.setContent(
							jq("<div>").append(
									jq("<div>")
											.addClass("category-name")
											.text(legacyLocale["DASHBOARD_NoPermission"])
							).append(jq("<div>").css("text-align", "center").append(
									jq("<img>")
											.attr("src", "https://mrpowergamerbr.com/uploads/2018-06-16_19-37-17.gif")
							)
							).append(jq("<div>").css("text-align", "center").append(
									jq("<p>")
											.text("Atualmente eu não consigo dar o cargo que você deseja porque eu não tenho permissão para isto... \uD83D\uDE2D")
							).append(
									jq("<p>")
											.text("Para eu conseguir dar este cargo, mova o meu cargo para acima do cargo que você deseja dar na lista de cargos do seu Discord!")
							))
									.html()
					)

					modal.addFooterBtn("<i class=\"far fa-thumbs-up\"></i> Já arrumei!", "button-discord button-discord-info pure-button button-discord-modal") {
						modal.close()
						window.location.reload()
					}
				}

				modal.addFooterBtn("<i class=\"fas fa-times\"></i> Fechar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
					modal.close()
				}
				modal.open()
				return
			}
			addRoleToAutoroleList(role)
		}
	}

	@JsName("prepareSave")
	fun prepareSave() {
		SaveStuff.prepareSave("autorole", { payload ->
			val roles = mutableListOf<String>()

			jq("#roleTable").children().each { index, elem ->
				val el = jQuery(elem)
				val entry = el.find(".role-entry")
				roles.add(entry.attr("role-id"))
			}

			payload["roles"] = roles
			payload["giveRolesAfter"] = (page.getElementById("give-roles-after") as HTMLInputElement).value.toIntOrNull()
		})
	}
}