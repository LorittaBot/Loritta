package net.perfectdreams.loritta.parallax.wrapper

import java.util.*

class Client {
	val token: String
		get() {
			val rand = SplittableRandom().nextInt(0, 5)
			return when (rand) {
				0 -> "https://youtu.be/c0_aZcgbMPY"
				1 -> "https://youtu.be/pPoFQV0jt70"
				2 -> "https://youtu.be/LpRakYMQwPk"
				3 -> "https://youtu.be/FrxTbjaM7_0"
				4 -> "https://cdn.discordapp.com/attachments/393332226881880074/518161189906415636/bolsonaro_tv.png"
				else -> "???"
			}
		}
}