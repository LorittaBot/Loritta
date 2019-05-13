package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.Loritta
import net.dv8tion.jda.api.JDA

class ParallaxClient(private val jda: JDA) {
	val user: ParallaxUser = ParallaxUser(jda.selfUser)
	val token: String
			get() {
				val rand = Loritta.RANDOM.nextInt(0, 5)
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