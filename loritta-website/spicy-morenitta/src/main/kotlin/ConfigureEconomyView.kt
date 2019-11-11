
import utils.TingleModal
import utils.TingleOptions
import kotlin.browser.document

object ConfigureEconomyView {
	fun start() {
		document.addEventListener("DOMContentLoaded", {
			val serverConfig = LoriDashboard.loadServerConfig()

			LoriDashboard.applyBlur("#hiddenIfDisabled", "#cmn-toggle-1")
			LoriDashboard.applyBlur("#hiddenIfDisabled2", "#cmn-toggle-2")

			val addShopItemObj = jq("#add-new-shop-item")
			val customCurrencyNameObj = jq("#customCurrencyName")
			val exchangeRateObj = jq("#exchangeRate")

			exchangeRateObj.change()

			addShopItemObj.click {
				val modal = TingleModal(
						TingleOptions(
								footer = true,
								cssClass = arrayOf("tingle-modal--overflow")
						)
				)

				modal.addFooterBtn("<i class=\"fas fa-plus\"></i> Adicionar", "button-discord button-discord-info pure-button button-discord-modal") {
					modal.close()
				}

				modal.addFooterBtn("<i class=\"fas fa-times\"></i> Cancelar", "button-discord pure-button button-discord-modal button-discord-modal-secondary-action") {
					modal.close()
				}

				val template = jq("#new-item-modal-template")
						.clone()

				template.find(".sectionHeader")
						.text("Novo Item")

				modal.setContent(template.html())

				modal.open()
			}
		})
	}

	@JsName("prepareSave")
	fun prepareSave() {
		SaveStuff.prepareSave("economy", extras = {
			val enableDreamExchange = it["enableDreamExchange"] as Boolean
			delete(it["enableDreamExchange"])

			if (!enableDreamExchange)
				it["exchangeRate"] = null
		})
	}
}