
import kotlin.browser.document

object ConfigureMiscellaneousConfig {
	fun start() {
		LoriDashboard.showLoadingBar("Carregando...")

		document.addEventListener("DOMContentLoaded", {
			val serverConfig = LoriDashboard.loadServerConfig()
		})
	}

	@JsName("prepareSave")
	fun prepareSave() {
		SaveStuff.prepareSave("miscellaneous")
	}
}