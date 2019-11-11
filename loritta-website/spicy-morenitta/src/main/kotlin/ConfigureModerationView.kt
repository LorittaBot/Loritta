
import net.perfectdreams.spicymorenitta.utils.Placeholders
import userdata.ModerationConfig
import kotlin.browser.document
import kotlin.js.Json
import kotlin.js.json

object ConfigureModerationView {
	fun start() {
		document.addEventListener("DOMContentLoaded", {
			val serverConfig = LoriDashboard.loadServerConfig()

			for (punishment in serverConfig.moderationConfig.punishmentActions) {
				addPunishment(punishment)
			}

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-2")

			LoriDashboard.configureTextChannelSelect(jq("#punishmentLogChannelId"), serverConfig, serverConfig.moderationConfig.punishmentLogChannelId)

			jq(".add-new-action").click {
				addPunishment(
						ModerationConfig.WarnAction(
								1,
								ModerationConfig.PunishmentAction.BAN,
								null
						)
				)
			}

			LoriDashboard.configureTextArea(
					jq("#punishmentLogMessage"),
					true,
					serverConfig,
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
		})
	}

	fun addPunishment(warnAction: ModerationConfig.WarnAction) {
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