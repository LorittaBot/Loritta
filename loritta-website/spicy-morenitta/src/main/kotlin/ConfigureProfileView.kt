object ConfigureProfileView {
	fun start() {
	}

	@JsName("prepareSave")
	fun prepareSave() {
		println("Preparing save... wow!")

		SaveStuff.prepareSave("profile", endpoint = "${loriUrl}api/v1/users/self-profile")
	}
}