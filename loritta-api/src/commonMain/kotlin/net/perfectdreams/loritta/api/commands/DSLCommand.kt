package net.perfectdreams.loritta.api.commands

import net.perfectdreams.loritta.api.LorittaBot

interface DSLCommand<T> {
	fun create(loritta: LorittaBot): T
}