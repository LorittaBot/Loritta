package net.perfectdreams.spicymorenitta.routes.christmas2019

import net.perfectdreams.spicymorenitta.SpicyMorenitta

fun main() {
	SpicyMorenitta.INSTANCE.routes.add(
			Christmas2019Route(SpicyMorenitta.INSTANCE)
	)
}