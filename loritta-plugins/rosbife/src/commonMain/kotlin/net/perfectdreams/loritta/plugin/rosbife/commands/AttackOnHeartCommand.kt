package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class AttackOnHeartCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("attackonheart"),
		1,
		"commands.images.attackonheart.description",
		"/api/v1/videos/attack-on-heart",
		"attack_on_heart.mp4"
)