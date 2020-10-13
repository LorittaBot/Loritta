package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageCommandBase

class PetPetCommand(m: RosbifePlugin) : GabrielaImageCommandBase(
		m.loritta,
		listOf("petpet"),
		"commands.images.petpet.description",
		"/api/images/pet-pet",
		"petpet.gif"
)
