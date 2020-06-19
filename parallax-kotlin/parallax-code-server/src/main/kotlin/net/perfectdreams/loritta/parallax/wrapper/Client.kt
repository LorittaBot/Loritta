package net.perfectdreams.loritta.parallax.wrapper

class Client {
	companion object {
		private val fakeTokens = listOf(
				"https://youtu.be/c0_aZcgbMPY",
				"https://youtu.be/LpRakYMQwPk",
				"https://youtu.be/FrxTbjaM7_0",
				"https://youtu.be/dQw4w9WgXcQ"
		)
	}

	val token: String
		get() {
			return fakeTokens.random()
		}
}