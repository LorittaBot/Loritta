package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import LoriDashboard
import SaveStuff
import jQuery
import jq
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import legacyLocale
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.DashboardUtils
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.utils.Placeholders
import net.perfectdreams.spicymorenitta.utils.onClick
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.views.dashboard.ServerConfig
import org.w3c.dom.HTMLButtonElement
import userdata.ModerationConfig
import kotlin.browser.document
import kotlin.js.Json
import kotlin.js.json

class ModerationConfigRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/moderation") {
	override val keepLoadingScreen: Boolean
		get() = true

	@Serializable
	class PartialGuildConfiguration(
			val textChannels: List<ServerConfig.TextChannel>,
			val moderationConfig: ServerConfig.ModerationConfig
	)
	
	@ImplicitReflectionSerializer
	override fun onRender(call: ApplicationCall) {
		launchWithLoadingScreenAndFixContent(call) {
			val guild = DashboardUtils.retrievePartialGuildConfiguration<PartialGuildConfiguration>(call.parameters["guildid"]!!, "textchannels", "moderation")
			switchContentAndFixLeftSidebarScroll(call)

			for (punishment in guild.moderationConfig.punishmentActions) {
				addPunishment(punishment)
			}

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-2")

			LoriDashboard.configureTextChannelSelect(jq("#punishmentLogChannelId"),  guild.textChannels, guild.moderationConfig.punishmentLogChannelId)

			jq(".add-new-action").click {
				addPunishment(
						ServerConfig.WarnAction(
								1,
								ServerConfig.PunishmentAction.BAN,
								null
						)
				)
			}

			LoriDashboard.configureTextArea(
					jq("#punishmentLogMessage"),
					true,
					null,
					true,
					jq("#punishmentLogChannelId"),
					true,
					Placeholders.DEFAULT_PLACEHOLDERS.toMutableMap().apply {
						put("reason", "Motivo da punição, caso nenhum motivo tenha sido especificado, isto estará vazio")
						put("punishment", "Punição aplicada (ban, mute, kick, etc)")
						put("staff", "Mostra o nome do usuário que fez a punição")
						put("@staff", "Menciona o usuário que fez a punição")
						put("staff-discriminator", "Mostra o discriminator do usuário que fez a punição")
						put("staff-id", "Mostra o ID do usuário que fez a punição")
						put("staff-avatar-url", "Mostra a URL do avatar do usuário que fez a punição")
					}
			)

			document.select<HTMLButtonElement>("#save-button").onClick {
				prepareSave()
			}
		}
	}


	fun addPunishment(warnAction: ServerConfig.WarnAction) {
		val action = jq("<div>")
				.append(
						jq("<button>")
								.attr("class", "button-discord button-discord-info pure-button remove-action")
								.html("<i class=\"fas fa-trash\"></i>")
				)
				.append(" Ao chegar em ")
				.append(
						jq("<input>")
								.attr("type", "number")
								.attr("min", 1)
								.`val`(warnAction.warnCount)
								.attr("class", "warnCount")
				).append(" avisos, ")
				.append("<select class='apply-punishment'>")
				.append(" o usuário")
				.append(jq("<div>")
						.css("height", "0px")
						.css("overflow", "hidden")
						.css("transition", "2s")
						.addClass("customMetadata")
						.append("O usuário deverá ser silenciado por ")
						.append(
								jq("<input>")
										.attr("type", "text")
										.attr("placeholder", "30 minutos")
										.`val`(warnAction.customMetadata0)
										.attr("class", "customMetadata0")
						)
				)

		if (warnAction.punishmentAction.toString() == ModerationConfig.PunishmentAction.MUTE.toString()) {
			action.find(".customMetadata")
					.css("height", "48px")
		}

		jq("#warnActions").append(
				action
		)

		action.find(".remove-action").click {
			action.remove()
		}

		val applyPunishment = action.find(".apply-punishment")

		for (punishment in ModerationConfig.PunishmentAction.values()) {
			val option = jq("<option>")
					.attr("name", legacyLocale[punishment.toString().replace("_", "") + "_PunishName"])
					.attr("value", punishment.toString())
					.text(legacyLocale[punishment.toString().replace("_", "") + "_PunishName"])

			if (warnAction.punishmentAction.toString() == punishment.toString()) {
				option.attr("selected", "selected")
			}

			applyPunishment.append(option)
		}

		jq(".apply-punishment").click {
			val punishmentAction = ModerationConfig.PunishmentAction.valueOf(action.find(".apply-punishment").`val`().unsafeCast<String>())

			if (punishmentAction.toString() == ModerationConfig.PunishmentAction.MUTE.toString()) {
				action.find(".customMetadata")
						.css("height", "48px")
			} else {
				action.find(".customMetadata")
						.css("height", "0px")
			}
		}
	}

	@JsName("prepareSave")
	fun prepareSave() {
		SaveStuff.prepareSave("moderation", { payload ->
			val actions = mutableListOf<Json>()

			val warnActions = jq("#warnActions")

			val children = warnActions.children()

			children.each { index, elem ->
				val el = jQuery(elem)
				val json = json()

				val punishmentAction = ModerationConfig.PunishmentAction.valueOf(el.find(".apply-punishment").`val`().unsafeCast<String>())
				json["punishmentAction"] = punishmentAction.toString()
				json["warnCount"] = el.find(".warnCount").`val`().unsafeCast<Int>()

				if (punishmentAction.toString() == ModerationConfig.PunishmentAction.MUTE.toString())
					json["customMetadata0"] = el.find(".customMetadata0").`val`().unsafeCast<String>()

				actions.add(
						json
				)
			}

			payload.set("punishmentActions", actions)
		})
	}
}